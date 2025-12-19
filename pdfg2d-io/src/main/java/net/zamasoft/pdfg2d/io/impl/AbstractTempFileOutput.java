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
 * Base implementation for fragmented output that uses a temporary random access
 * file.
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractRandomAccessFileBuilder.java 758 2011-11-13 14:05:46Z
 *          miyabe $
 */
public abstract class AbstractTempFileOutput implements FragmentedOutput {
	private static final Logger LOG = Logger.getLogger(AbstractTempFileOutput.class.getName());

	/**
	 * Segment size for disk storage.
	 */
	private static final int SEGMENT_SIZE = 8192;

	/**
	 * Fragment buffer max size, total buffer max size, and threshold to flush to
	 * disk on close.
	 */
	private final int fragmentBufferSize, totalBufferSize, threshold;

	protected RandomAccessFile raf = null;

	protected File file = null;

	protected List<Fragment> fragments = null;

	protected Fragment first = null, last = null;

	protected long length = 0, onMemory = 0;

	protected int lastSegment = 0;

	private byte[] sharedBuffer = null;

	protected class Fragment {
		public Fragment prev = null, next = null;

		private final int id;

		private int fragmentLength = 0;

		private byte[] memoryBuffer = null;

		private IntList segments;

		private int lastSegmentLength = 0;

		public Fragment(final int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public int getLength() {
			return this.fragmentLength;
		}

		public void write(final byte[] buff, final int pos, final int len) throws IOException {
			if (this.segments == null && (this.fragmentLength + len) < fragmentBufferSize
					&& (AbstractTempFileOutput.this.onMemory + fragmentBufferSize) <= totalBufferSize) {
				if (this.memoryBuffer == null) {
					this.memoryBuffer = new byte[fragmentBufferSize];
					AbstractTempFileOutput.this.onMemory += fragmentBufferSize;
				}
				System.arraycopy(buff, pos, this.memoryBuffer, this.fragmentLength, len);
			} else {
				if (this.memoryBuffer != null) {
					this.writeToRaf(this.memoryBuffer, 0, this.fragmentLength);
					AbstractTempFileOutput.this.onMemory -= fragmentBufferSize;
					this.memoryBuffer = null;
				}
				this.writeToRaf(buff, pos, len);
			}
			this.fragmentLength += len;
		}

		private void writeToRaf(final byte[] buff, int off, int len) throws IOException {
			if (this.segments == null) {
				this.segments = new IntList(10);
				this.segments.add(AbstractTempFileOutput.this.lastSegment++);
			}
			while (len > 0) {
				if (this.lastSegmentLength == AbstractTempFileOutput.SEGMENT_SIZE) {
					this.segments.add(AbstractTempFileOutput.this.lastSegment++);
					this.lastSegmentLength = 0;
				}
				final int seg = this.segments.get(this.segments.size() - 1);
				final int wlen = Math.min(len, AbstractTempFileOutput.SEGMENT_SIZE - this.lastSegmentLength);
				final long wpos = (long) seg * (long) AbstractTempFileOutput.SEGMENT_SIZE
						+ (long) this.lastSegmentLength;
				AbstractTempFileOutput.this.raf.seek(wpos);
				AbstractTempFileOutput.this.raf.write(buff, off, wlen);
				this.lastSegmentLength += wlen;
				off += wlen;
				len -= wlen;
			}
		}

		public void close() throws IOException {
			if (this.memoryBuffer != null) {
				if (this.fragmentLength >= AbstractTempFileOutput.this.threshold) {
					this.writeToRaf(this.memoryBuffer, 0, this.fragmentLength);
					AbstractTempFileOutput.this.onMemory -= fragmentBufferSize;
					this.memoryBuffer = null;
				} else if (this.fragmentLength < this.memoryBuffer.length) {
					final byte[] temp = new byte[this.fragmentLength];
					System.arraycopy(this.memoryBuffer, 0, temp, 0, temp.length);
					AbstractTempFileOutput.this.onMemory -= (this.memoryBuffer.length - this.fragmentLength);
					this.memoryBuffer = temp;
				}
			}
		}

		public void writeTo(final OutputStream out) throws IOException {
			if (this.segments == null) {
				if (this.memoryBuffer != null) {
					out.write(this.memoryBuffer, 0, this.fragmentLength);
				}
			} else {
				if (AbstractTempFileOutput.this.sharedBuffer == null) {
					AbstractTempFileOutput.this.sharedBuffer = new byte[AbstractTempFileOutput.SEGMENT_SIZE];
				}
				final byte[] buff = AbstractTempFileOutput.this.sharedBuffer;
				for (int i = 0; i < this.segments.size() - 1; ++i) {
					final int seg = this.segments.get(i);
					final long rpos = (long) seg * (long) AbstractTempFileOutput.SEGMENT_SIZE;
					AbstractTempFileOutput.this.raf.seek(rpos);
					AbstractTempFileOutput.this.raf.readFully(buff);
					out.write(buff);
				}
				final int seg = this.segments.get(this.segments.size() - 1);
				final long rpos = (long) seg * (long) AbstractTempFileOutput.SEGMENT_SIZE;
				AbstractTempFileOutput.this.raf.seek(rpos);
				AbstractTempFileOutput.this.raf.readFully(buff, 0, this.lastSegmentLength);
				out.write(buff, 0, this.lastSegmentLength);
			}
		}

		public void dispose() {
			this.memoryBuffer = null;
		}
	}

	public AbstractTempFileOutput(final int fragmentBufferSize, final int totalBufferSize, final int threshold) {
		this.fragmentBufferSize = fragmentBufferSize;
		this.totalBufferSize = totalBufferSize;
		this.threshold = threshold;
	}

	public AbstractTempFileOutput() {
		this(8192, 1024 * 1024 * 2, 1024);
	}

	protected int nextId() throws IOException {
		if (this.fragments == null) {
			this.fragments = new ArrayList<>();
			this.file = File.createTempFile("pdfg2d-io-", ".fragments");
			this.file.deleteOnExit();
			this.raf = new RandomAccessFile(this.file, "rw");
		}
		return this.fragments.size();
	}

	protected Fragment getFragment(final int id) throws IOException {
		return this.fragments.get(id);
	}

	protected void putFragment(final int id, final Fragment fragment) {
		assert (id == this.fragments.size());
		this.fragments.add(fragment);
	}

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

	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

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

	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		final int id = this.nextId();
		final var anchor = this.getFragment(anchorId);
		final var fragment = new Fragment(id);
		this.putFragment(id, fragment);
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

	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		final var fragment = this.getFragment(id);
		fragment.write(b, off, len);
		this.length += len;
	}

	@Override
	public void finishFragment(final int id) throws IOException {
		final var fragment = this.getFragment(id);
		fragment.close();
	}

	protected void finish(final OutputStream out) throws IOException {
		if (this.first == null) {
			// Empty
			this.clean();
			return;
		}
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

		var fragment = this.first;
		while (fragment != null) {
			fragment.writeTo(out);
			fragment.dispose();
			fragment = fragment.next;
		}
		this.clean();
	}

	public long getLength() {
		return this.length;
	}

	private void clean() {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Cleaning resources.");
		}
		if (this.raf != null) {
			try {
				this.raf.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Failed to close temporary file.", e);
			}
			this.raf = null;
		}
		if (this.file != null) {
			try {
				this.file.delete();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Failed to delete temporary file.", e);
			}
			this.file = null;
		}
		this.first = null;
		this.last = null;
		this.fragments = null;
		this.length = 0;
		this.onMemory = 0;
		this.lastSegment = 0;
	}

	@Override
	public void close() throws IOException {
		if (this.first != null || this.raf != null || this.file != null) {
			this.clean();
		}
	}
}