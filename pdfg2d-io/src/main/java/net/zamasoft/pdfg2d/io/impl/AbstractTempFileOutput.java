package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * High-performance implementation of FragmentedOutput.
 * <p>
 * This class optimizes for speed and memory efficiency by buffering data in
 * memory chunks and intelligently spilling to disk only when global memory
 * limit is
 * reached. It uses {@link FileChannel} for efficient file I/O and avoids strict
 * segmentation for better flexibility.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 */
public abstract class AbstractTempFileOutput implements FragmentedOutput {
	private static final Logger LOG = Logger.getLogger(AbstractTempFileOutput.class.getName());

	/**
	 * Configuration for buffer management.
	 * 
	 * @param chunkSize maximum size of each memory chunk (e.g., 64KB).
	 * @param maxMemory maximum total memory to use before spilling to disk.
	 */
	public record Config(int chunkSize, long maxMemory) {
		/**
		 * Default configuration: 64KB chunks, 64MB max memory.
		 */
		public static final Config DEFAULT = new Config(64 * 1024, 64 * 1024 * 1024);

		/**
		 * Configuration for purely in-memory processing.
		 * <p>
		 * Max memory is set to {@link Long#MAX_VALUE}, effectively disabling disk
		 * spilling.
		 * </p>
		 */
		public static final Config ON_MEMORY = new Config(64 * 1024, Long.MAX_VALUE);

		public Config {
			if (chunkSize <= 0)
				throw new IllegalArgumentException("chunkSize must be positive");
			if (maxMemory <= 0)
				throw new IllegalArgumentException("maxMemory must be positive");
		}
	}

	// Configuration
	protected final int chunkSize;
	protected final long maxMemory;

	// Global State
	protected long currentMemoryUsage = 0;
	protected long totalLength = 0;

	// Storage
	protected File tempFile;
	protected RandomAccessFile raf;
	protected FileChannel fileChannel;

	// Data Structure
	protected final List<Fragment> fragments = new ArrayList<>();
	protected Fragment first = null;
	protected Fragment last = null;

	// Spill Management
	// We scan for the largest fragment only when spilling is required.
	// This avoids overhead during normal writes.

	/**
	 * Represents a piece of data, either in memory or on disk.
	 */
	private sealed interface Chunk permits MemoryChunk, FileChunk {
		long getLength();

		void writeTo(OutputStream out, FileChannel channel, byte[] buffer) throws IOException;
	}

	private final class MemoryChunk implements Chunk {
		final byte[] data;
		int length;

		MemoryChunk(int capacity) {
			this.data = new byte[capacity];
			this.length = 0;
		}

		@Override
		public long getLength() {
			return length;
		}

		@Override
		public void writeTo(OutputStream out, FileChannel channel, byte[] buffer) throws IOException {
			out.write(data, 0, length);
		}
	}

	private final class FileChunk implements Chunk {
		final long position;
		final long length;

		FileChunk(long position, long length) {
			this.position = position;
			this.length = length;
		}

		@Override
		public long getLength() {
			return length;
		}

		@Override
		public void writeTo(OutputStream out, FileChannel channel, byte[] buffer) throws IOException {
			long remaining = length;
			long currentPos = position;
			while (remaining > 0) {
				int readSize = (int) Math.min(remaining, buffer.length);
				// We need to synchronize if we were using multiple threads,
				// but this class is not thread-safe by design (inherited).
				// Use channel.read(dst, position) for thread safety if needed in future.
				ByteBuffer buf = ByteBuffer.wrap(buffer, 0, readSize);
				int read = channel.read(buf, currentPos);
				if (read == -1)
					break; // Should not happen
				out.write(buffer, 0, read);
				currentPos += read;
				remaining -= read;
			}
		}
	}

	protected class Fragment {
		final int id;
		Fragment prev, next;

		final List<Chunk> chunks = new ArrayList<>();
		long totalLength = 0;
		long memoryUsage = 0;

		// Optimization: keep the last chunk handy if it's a memory chunk for quick
		// appending
		MemoryChunk activeChunk = null;

		Fragment(int id) {
			this.id = id;
		}

		int getId() {
			return id;
		}

		long getLength() {
			return totalLength;
		}

		void write(byte[] b, int off, int len) throws IOException {
			int remaining = len;
			int offset = off;

			while (remaining > 0) {
				ensureActiveChunk();

				int space = activeChunk.data.length - activeChunk.length;
				int toWrite = Math.min(remaining, space);

				System.arraycopy(b, offset, activeChunk.data, activeChunk.length, toWrite);

				activeChunk.length += toWrite;
				this.totalLength += toWrite;
				offset += toWrite;
				remaining -= toWrite;

				if (activeChunk.length == activeChunk.data.length) {
					// Current chunk is full
					activeChunk = null;
				}
			}
		}

		private void ensureActiveChunk() throws IOException {
			if (activeChunk == null) {
				checkSpill(); // Check memory limit before allocation

				activeChunk = new MemoryChunk(chunkSize);
				chunks.add(activeChunk);

				this.memoryUsage += chunkSize;
				updateGlobalMemory(chunkSize);
			}
		}

		void spill() throws IOException {
			if (memoryUsage == 0)
				return;

			// Prepare file
			ensureFileOpen();

			// Allocate a buffer for bulk writing if needed, but here we can just write
			// chunks directly
			// FileChannel.write(ByteBuffer[]) is perfect for this.

			List<ByteBuffer> buffersToWrite = new ArrayList<>();
			long bytesToSpill = 0;

			// We only spill MemoryChunks that are currently in the list
			// We replace them with FileChunks.

			int firstMemoryIndex = -1;

			for (int i = 0; i < chunks.size(); i++) {
				Chunk c = chunks.get(i);
				if (c instanceof MemoryChunk mc) {
					if (firstMemoryIndex == -1)
						firstMemoryIndex = i;
					buffersToWrite.add(ByteBuffer.wrap(mc.data, 0, mc.length));
					bytesToSpill += mc.length;
				} else if (firstMemoryIndex != -1) {
					// End of a contiguous block of memory chunks -> write them
					flushMemoryBlock(firstMemoryIndex, i, buffersToWrite, bytesToSpill);
					firstMemoryIndex = -1;
					buffersToWrite.clear();
					bytesToSpill = 0;
				}
			}

			// Handle trailing memory chunks
			if (firstMemoryIndex != -1) {
				flushMemoryBlock(firstMemoryIndex, chunks.size(), buffersToWrite, bytesToSpill);
			}

			// Reset memory usage for this fragment
			updateGlobalMemory(-this.memoryUsage);
			this.memoryUsage = 0;
			this.activeChunk = null; // Forces new chunk on next write
		}

		private void flushMemoryBlock(int startIndex, int endIndex, List<ByteBuffer> buffers, long length)
				throws IOException {
			if (length == 0)
				return;

			long filePos = fileChannel.position(); // Append to end

			// Write all buffers
			ByteBuffer[] bufArray = buffers.toArray(new ByteBuffer[0]);
			long written = 0;
			while (written < length) {
				written += fileChannel.write(bufArray);
			}

			// Replace chunks in list with a single FileChunk
			chunks.subList(startIndex, endIndex).clear();
			chunks.add(startIndex, new FileChunk(filePos, length));
		}
	}

	public AbstractTempFileOutput(Config config) {
		this.chunkSize = config.chunkSize();
		this.maxMemory = config.maxMemory();
	}

	@Deprecated
	public AbstractTempFileOutput(int fragmentBufferSize, int totalBufferSize, int threshold) {
		this(new Config(fragmentBufferSize, totalBufferSize)); // Map legacy params
	}

	public AbstractTempFileOutput() {
		this(Config.DEFAULT);
	}

	// --- FragmentedOutput Implementation ---

	@Override
	public void addFragment() throws IOException {
		Fragment f = new Fragment(fragments.size());
		fragments.add(f);

		if (first == null) {
			first = f;
		} else {
			last.next = f;
			f.prev = last;
		}
		last = f;
	}

	@Override
	public void insertFragmentBefore(int anchorId) throws IOException {
		Fragment anchor = fragments.get(anchorId);
		Fragment f = new Fragment(fragments.size());
		fragments.add(f);

		f.prev = anchor.prev;
		f.next = anchor;
		if (anchor.prev != null) {
			anchor.prev.next = f;
		} else {
			first = f;
		}
		anchor.prev = f;
	}

	@Override
	public void write(int id, byte[] b, int off, int len) throws IOException {
		Fragment f = fragments.get(id);
		f.write(b, off, len);
		this.totalLength += len;
	}

	@Override
	public void finishFragment(int id) throws IOException {
		// No-op in this strategy
	}

	@Override
	public PositionInfo getPositionInfo() {
		return new PositionInfo() {
			private final long[] positions;
			{
				// Calculate all positions strictly by linked list order
				positions = new long[fragments.size()];
				long pos = 0;
				Fragment curr = first;
				while (curr != null) {
					positions[curr.getId()] = pos;
					pos += curr.getLength();
					curr = curr.next;
				}
			}

			@Override
			public long getPosition(int id) {
				return positions[id];
			}
		};
	}

	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

	/**
	 * Returns the total length of all data written.
	 * 
	 * @return total bytes written across all fragments.
	 */
	public long getLength() {
		return totalLength;
	}

	@Override
	public void close() throws IOException {
		clean();
	}

	protected void finish(OutputStream out) throws IOException {
		if (first == null) {
			clean();
			return;
		}

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Finishing output. Total length: " + totalLength + ", Memory usage: " + currentMemoryUsage);
		}

		byte[] copyBuffer = new byte[8192]; // Buffer for disk-to-stream copies

		Fragment curr = first;
		while (curr != null) {
			for (Chunk chunk : curr.chunks) {
				chunk.writeTo(out, fileChannel, copyBuffer);
			}
			curr = curr.next;
		}

		out.flush();
		clean();
	}

	// --- Internal Helpers ---

	private void updateGlobalMemory(long delta) {
		this.currentMemoryUsage += delta;
	}

	private void checkSpill() throws IOException {
		while (currentMemoryUsage >= maxMemory) {
			// Strategy: Find the fragment with MAX memory usage.
			Fragment candidate = findMaxMemoryFragment();

			if (candidate == null || candidate.memoryUsage == 0) {
				// Should not happen if usage > 0, but safety break
				break;
			}

			candidate.spill();
		}
	}

	private Fragment findMaxMemoryFragment() {
		Fragment max = null;
		long maxUsage = -1;
		for (Fragment f : fragments) {
			if (f.memoryUsage > maxUsage) {
				maxUsage = f.memoryUsage;
				max = f;
			}
		}
		return max;
	}

	private void ensureFileOpen() throws IOException {
		if (fileChannel == null) {
			tempFile = File.createTempFile("pdfg2d-io-fast-", ".tmp");
			tempFile.deleteOnExit();
			raf = new RandomAccessFile(tempFile, "rw");
			fileChannel = raf.getChannel();
		}
	}

	private void clean() {
		try {
			if (fileChannel != null)
				fileChannel.close();
			if (raf != null)
				raf.close();
			if (tempFile != null)
				tempFile.delete();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to clean up temp resources", e);
		} finally {
			fileChannel = null;
			raf = null;
			tempFile = null;
			fragments.clear();
			first = last = null;
			currentMemoryUsage = 0;
			totalLength = 0;
		}
	}
}