package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.io.FragmentedOutput;
import net.zamasoft.pdfg2d.io.util.IntList;

/**
 * Abstract base implementation for fragmented output using a temporary file.
 * <p>
 * This class provides a hybrid memory/disk storage strategy for building
 * fragmented data. Small fragments are kept in memory for fast access,
 * while larger fragments are stored in a temporary random access file.
 * </p>
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Fragments can be inserted in any order</li>
 * <li>Memory usage is controlled by buffer size limits</li>
 * <li>Automatic spillover to disk when memory limits are exceeded</li>
 * <li>Efficient final assembly using linked list ordering</li>
 * </ul>
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractRandomAccessFileBuilder.java 758 2011-11-13 14:05:46Z
 *          miyabe $
 */
public abstract class AbstractTempFileOutput implements FragmentedOutput {
	private static final Logger LOG = Logger.getLogger(AbstractTempFileOutput.class.getName());

	/**
	 * Configuration for buffer management.
	 * <p>
	 * Controls how data is buffered in memory versus stored on disk during
	 * fragmented output construction.
	 * </p>
	 * 
	 * @param fragmentBufferSize maximum buffer size per fragment in bytes.
	 *                           Fragments smaller than this are kept in memory.
	 * @param totalBufferSize    maximum total buffer size across all fragments in
	 *                           bytes. When exceeded, fragments are spilled to
	 *                           disk.
	 * @param threshold          threshold in bytes below which fragments stay in
	 *                           memory even when closed.
	 * @param segmentSize        size of each segment in the temporary file in
	 *                           bytes.
	 */
	public record Config(int fragmentBufferSize, int totalBufferSize, int threshold, int segmentSize) {
		/**
		 * Default configuration: 32KB per fragment, 8MB total, 4KB threshold, 8KB
		 * segment.
		 */
		public static final Config DEFAULT = new Config(32768, 1024 * 1024 * 8, 4096, 8192);

		/**
		 * Creates a new configuration with validation.
		 * 
		 * @throws IllegalArgumentException if any value is not positive.
		 */
		public Config {
			if (fragmentBufferSize <= 0) {
				throw new IllegalArgumentException("fragmentBufferSize must be positive: " + fragmentBufferSize);
			}
			if (totalBufferSize <= 0) {
				throw new IllegalArgumentException("totalBufferSize must be positive: " + totalBufferSize);
			}
			if (threshold <= 0) {
				throw new IllegalArgumentException("threshold must be positive: " + threshold);
			}
			if (segmentSize <= 0) {
				throw new IllegalArgumentException("segmentSize must be positive: " + segmentSize);
			}
		}
	}

	/** Size of each segment in the temporary file. */
	private final int segmentSize;

	/**
	 * Maximum buffer size for a single fragment in memory.
	 * Maximum total buffer size across all fragments.
	 * Threshold below which fragments stay in memory on close.
	 */
	private final int fragmentBufferSize, totalBufferSize, threshold;

	/** Random access file for disk-based storage; created lazily. */
	protected RandomAccessFile raf = null;

	/** Temporary file for disk-based storage; created lazily. */
	protected File file = null;

	/** List of all fragments indexed by ID. */
	protected List<Fragment> fragments = null;

	/** Linked list pointers for fragment ordering. */
	protected Fragment first = null, last = null;

	/** Total data length and current in-memory buffer usage. */
	protected long length = 0, onMemory = 0;

	/** Next segment index for disk storage allocation. */
	protected int lastSegment = 0;

	/** Shared buffer for reading segments during final assembly. */
	private byte[] sharedBuffer = null;

	/**
	 * Represents a single fragment of data.
	 * <p>
	 * Each fragment maintains a doubly-linked list for ordering and can store
	 * data either in memory or on disk. Data is automatically spilled to disk
	 * when memory limits are exceeded.
	 * </p>
	 */
	protected class Fragment {
		/** Previous and next fragments in the linked list. */
		public Fragment prev = null, next = null;

		/** Unique identifier for this fragment. */
		private final int id;

		/** Total byte length of data in this fragment. */
		private int fragmentLength = 0;

		/** In-memory buffer; null if data is stored on disk. */
		private byte[] memoryBuffer = null;

		/** List of segment indices in the temp file. */
		private IntList segments;

		/** Byte length of data in the last segment. */
		private int lastSegmentLength = 0;

		/**
		 * Creates a new fragment with the given ID.
		 * 
		 * @param id unique fragment identifier.
		 */
		public Fragment(final int id) {
			this.id = id;
		}

		/**
		 * Returns the fragment ID.
		 * 
		 * @return fragment ID.
		 */
		public int getId() {
			return this.id;
		}

		/**
		 * Returns the total byte length of this fragment.
		 * 
		 * @return fragment length in bytes.
		 */
		public int getLength() {
			return this.fragmentLength;
		}

		/**
		 * Writes data to this fragment.
		 * <p>
		 * Data is buffered in memory if possible. When memory limits are exceeded,
		 * data is automatically spilled to the temporary file.
		 * </p>
		 * 
		 * @param buff source byte array.
		 * @param pos  start offset in the source array.
		 * @param len  number of bytes to write.
		 * @throws IOException if an I/O error occurs.
		 */
		public void write(final byte[] buff, final int pos, final int len) throws IOException {
			// Check if data fits in memory buffer
			if (this.segments == null && (this.fragmentLength + len) < fragmentBufferSize
					&& (AbstractTempFileOutput.this.onMemory + fragmentBufferSize) <= totalBufferSize) {
				// Buffer in memory
				if (this.memoryBuffer == null) {
					this.memoryBuffer = new byte[fragmentBufferSize];
					AbstractTempFileOutput.this.onMemory += fragmentBufferSize;
				}
				System.arraycopy(buff, pos, this.memoryBuffer, this.fragmentLength, len);
			} else {
				// Spill to disk
				if (this.memoryBuffer != null) {
					this.writeToRaf(this.memoryBuffer, 0, this.fragmentLength);
					AbstractTempFileOutput.this.onMemory -= fragmentBufferSize;
					this.memoryBuffer = null;
				}
				this.writeToRaf(buff, pos, len);
			}
			this.fragmentLength += len;
		}

		/**
		 * Writes data to the random access file.
		 * <p>
		 * Data is written in fixed-size segments. Multiple segments are
		 * allocated as needed.
		 * </p>
		 * 
		 * @param buff source byte array.
		 * @param off  start offset.
		 * @param len  number of bytes to write.
		 * @throws IOException if an I/O error occurs.
		 */
		private void writeToRaf(final byte[] buff, int off, int len) throws IOException {
			// Allocate first segment if needed
			if (this.segments == null) {
				this.segments = new IntList(10);
				this.segments.add(AbstractTempFileOutput.this.lastSegment++);
			}
			// Write data across segments
			while (len > 0) {
				// Allocate new segment if current is full
				if (this.lastSegmentLength == AbstractTempFileOutput.this.segmentSize) {
					this.segments.add(AbstractTempFileOutput.this.lastSegment++);
					this.lastSegmentLength = 0;
				}
				final int seg = this.segments.get(this.segments.size() - 1);
				final int wlen = Math.min(len, AbstractTempFileOutput.this.segmentSize - this.lastSegmentLength);
				final long wpos = (long) seg * (long) AbstractTempFileOutput.this.segmentSize
						+ (long) this.lastSegmentLength;
				AbstractTempFileOutput.this.raf.seek(wpos);
				AbstractTempFileOutput.this.raf.write(buff, off, wlen);
				this.lastSegmentLength += wlen;
				off += wlen;
				len -= wlen;
			}
		}

		/**
		 * Finishes writing to this fragment.
		 * <p>
		 * Optimizes memory usage by shrinking buffers or flushing to disk
		 * based on the threshold setting.
		 * </p>
		 * 
		 * @throws IOException if an I/O error occurs.
		 */
		public void close() throws IOException {
			if (this.memoryBuffer != null) {
				// Flush to disk if above threshold
				if (this.fragmentLength >= AbstractTempFileOutput.this.threshold) {
					this.writeToRaf(this.memoryBuffer, 0, this.fragmentLength);
					AbstractTempFileOutput.this.onMemory -= fragmentBufferSize;
					this.memoryBuffer = null;
				} else if (this.fragmentLength < this.memoryBuffer.length) {
					// Shrink buffer to actual size
					final byte[] temp = new byte[this.fragmentLength];
					System.arraycopy(this.memoryBuffer, 0, temp, 0, temp.length);
					AbstractTempFileOutput.this.onMemory -= (this.memoryBuffer.length - this.fragmentLength);
					this.memoryBuffer = temp;
				}
			}
		}

		/**
		 * Writes all fragment data to the given output stream.
		 * <p>
		 * Reads from memory buffer or disk segments as appropriate.
		 * </p>
		 * 
		 * @param out target output stream.
		 * @throws IOException if an I/O error occurs.
		 */
		public void writeTo(final OutputStream out) throws IOException {
			if (this.segments == null) {
				// Write from memory
				if (this.memoryBuffer != null) {
					out.write(this.memoryBuffer, 0, this.fragmentLength);
				}
			} else {
				// Read from disk and write to output
				if (AbstractTempFileOutput.this.sharedBuffer == null) {
					AbstractTempFileOutput.this.sharedBuffer = new byte[AbstractTempFileOutput.this.segmentSize];
				}
				final byte[] buff = AbstractTempFileOutput.this.sharedBuffer;
				// Write all complete segments
				for (int i = 0; i < this.segments.size() - 1; ++i) {
					final int seg = this.segments.get(i);
					final long rpos = (long) seg * (long) AbstractTempFileOutput.this.segmentSize;
					AbstractTempFileOutput.this.raf.seek(rpos);
					AbstractTempFileOutput.this.raf.readFully(buff);
					out.write(buff);
				}
				// Write final partial segment
				final int seg = this.segments.get(this.segments.size() - 1);
				final long rpos = (long) seg * (long) AbstractTempFileOutput.this.segmentSize;
				AbstractTempFileOutput.this.raf.seek(rpos);
				AbstractTempFileOutput.this.raf.readFully(buff, 0, this.lastSegmentLength);
				out.write(buff, 0, this.lastSegmentLength);
			}
		}

		/**
		 * Releases memory used by this fragment.
		 */
		public void dispose() {
			this.memoryBuffer = null;
		}
	}

	/**
	 * Creates a new temp file output with custom buffer settings.
	 * 
	 * @param config buffer configuration.
	 */
	public AbstractTempFileOutput(final Config config) {
		this.fragmentBufferSize = config.fragmentBufferSize();
		this.totalBufferSize = config.totalBufferSize();
		this.threshold = config.threshold();
		this.segmentSize = config.segmentSize();
	}

	/**
	 * Creates a new temp file output with custom buffer settings.
	 * 
	 * @param fragmentBufferSize maximum buffer size per fragment.
	 * @param totalBufferSize    maximum total buffer size in memory.
	 * @param threshold          threshold below which fragments stay in memory.
	 * @deprecated Use {@link #AbstractTempFileOutput(Config)} instead.
	 */
	@Deprecated
	public AbstractTempFileOutput(final int fragmentBufferSize, final int totalBufferSize, final int threshold) {
		this(new Config(fragmentBufferSize, totalBufferSize, threshold, Config.DEFAULT.segmentSize()));
	}

	/**
	 * Creates a new temp file output with default settings.
	 * 
	 * @see Config#DEFAULT
	 */
	public AbstractTempFileOutput() {
		this(Config.DEFAULT);
	}

	/**
	 * Generates the next fragment ID and initializes storage if needed.
	 * <p>
	 * On first call, creates the fragment list and temporary file.
	 * </p>
	 * 
	 * @return the next available fragment ID.
	 * @throws IOException if an I/O error occurs creating the temp file.
	 */
	protected int nextId() throws IOException {
		if (this.fragments == null) {
			this.fragments = new ArrayList<>();
			this.file = File.createTempFile("pdfg2d-io-", ".fragments");
			this.file.deleteOnExit();
			this.raf = new RandomAccessFile(this.file, "rw");
		}
		return this.fragments.size();
	}

	/**
	 * Retrieves a fragment by ID.
	 * 
	 * @param id fragment ID.
	 * @return the fragment with the given ID.
	 * @throws IOException if an I/O error occurs.
	 */
	protected Fragment getFragment(final int id) throws IOException {
		return this.fragments.get(id);
	}

	/**
	 * Stores a fragment in the fragment list.
	 * 
	 * @param id       fragment ID (must equal current list size).
	 * @param fragment the fragment to store.
	 */
	protected void putFragment(final int id, final Fragment fragment) {
		assert (id == this.fragments.size());
		this.fragments.add(fragment);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calculates positions by traversing the linked list in order and
	 * accumulating fragment lengths.
	 * </p>
	 */
	@Override
	public PositionInfo getPositionInfo() {
		final long[] idToPosition = new long[this.fragments.size()];
		long position = 0;
		var fragment = this.first;
		while (fragment != null) {
			idToPosition[fragment.getId()] = position;
			position += fragment.getLength();
			fragment = fragment.next;
		}
		return id -> idToPosition[id];
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return always true; this implementation supports position info.
	 */
	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a new fragment and appends it to the end of the linked list.
	 * </p>
	 */
	@Override
	public void addFragment() throws IOException {
		final int id = this.nextId();
		final var fragment = new Fragment(id);
		if (this.first == null) {
			this.first = fragment;
		} else {
			this.last.next = fragment;
			fragment.prev = this.last;
		}
		this.putFragment(id, fragment);
		this.last = fragment;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a new fragment and inserts it before the anchor fragment
	 * in the linked list.
	 * </p>
	 */
	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		final int id = this.nextId();
		final var anchor = this.getFragment(anchorId);
		final var fragment = new Fragment(id);
		this.putFragment(id, fragment);
		// Insert into linked list before anchor
		fragment.prev = anchor.prev;
		fragment.next = anchor;
		if (anchor.prev != null) {
			anchor.prev.next = fragment;
		}
		anchor.prev = fragment;
		if (this.first == anchor) {
			this.first = fragment;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Writes data to the specified fragment and updates the total length.
	 * </p>
	 */
	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		final var fragment = this.getFragment(id);
		fragment.write(b, off, len);
		this.length += len;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Finishes the fragment by optimizing its memory usage.
	 * </p>
	 */
	@Override
	public void finishFragment(final int id) throws IOException {
		final var fragment = this.getFragment(id);
		fragment.close();
	}

	/**
	 * Assembles all fragments in order and writes to the output stream.
	 * <p>
	 * Traverses the linked list from first to last, writing each fragment's
	 * data to the output stream, then releases all resources.
	 * </p>
	 * 
	 * @param out target output stream.
	 * @throws IOException if an I/O error occurs.
	 */
	protected void finish(final OutputStream out) throws IOException {
		if (this.first == null) {
			// Empty output
			this.clean();
			return;
		}
		// Log statistics
		if (LOG.isLoggable(Level.FINE)) {
			final int total = this.fragments.size();
			int memoryCount = 0;
			for (int i = 0; i < total; ++i) {
				final var f = this.fragments.get(i);
				if (f.segments == null) {
					++memoryCount;
				}
			}
			LOG.fine(total + " fragments were generated.");
			LOG.fine(memoryCount + " fragments are on memory, " + (total - memoryCount) + " fragments are on disk.");
		}

		// Write all fragments in order
		var fragment = this.first;
		while (fragment != null) {
			fragment.writeTo(out);
			fragment.dispose();
			fragment = fragment.next;
		}
		this.clean();
	}

	/**
	 * Returns the total length of all data written.
	 * 
	 * @return total bytes written across all fragments.
	 */
	public long getLength() {
		return this.length;
	}

	/**
	 * Releases all resources including the temporary file.
	 * <p>
	 * Closes and deletes the random access file, then resets all state.
	 * </p>
	 */
	private void clean() {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Cleaning resources.");
		}
		// Close random access file
		if (this.raf != null) {
			try {
				this.raf.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Failed to close temporary file.", e);
			}
			this.raf = null;
		}
		// Delete temporary file
		if (this.file != null) {
			try {
				this.file.delete();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Failed to delete temporary file.", e);
			}
			this.file = null;
		}
		// Reset state
		this.first = null;
		this.last = null;
		this.fragments = null;
		this.length = 0;
		this.onMemory = 0;
		this.lastSegment = 0;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Releases all resources without writing output values.
	 * Call this to abort output construction.
	 * </p>
	 */
	@Override
	public void close() throws IOException {
		if (this.first != null || this.raf != null || this.file != null) {
			this.clean();
		}
	}
}