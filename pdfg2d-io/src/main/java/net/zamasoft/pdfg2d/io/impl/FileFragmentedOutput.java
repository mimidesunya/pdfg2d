package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * Sequential output implementation that writes fragmented data to a file.
 * <p>
 * This class extends {@link AbstractTempFileOutput} and implements
 * {@link SequentialOutput} to provide file-based output for PDF generation.
 * </p>
 * <p>
 * When used in sequential mode, data is written directly to the target file.
 * When fragments are inserted out of order, data is buffered in memory or
 * a temporary file and assembled in the correct order when closed.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FileFragmentedOutput extends AbstractTempFileOutput implements SequentialOutput {
	/** Target output file. */
	protected final File file;

	/** Direct output stream for sequential writes; null if using fragments. */
	protected OutputStream out = null;

	/**
	 * Creates a new file output with custom buffer settings.
	 * 
	 * @param file   target output file.
	 * @param config buffer configuration.
	 */
	public FileFragmentedOutput(final File file, final Config config) {
		super(config);
		this.file = file;
	}

	/**
	 * Creates a new file output with custom buffer settings.
	 * 
	 * @param file               target output file.
	 * @param fragmentBufferSize maximum buffer size per fragment.
	 * @param totalBufferSize    maximum total buffer size in memory.
	 * @param threshold          threshold to flush fragment data to disk.
	 * @deprecated Use {@link #FileFragmentedOutput(File, Config)} instead.
	 */
	@Deprecated
	public FileFragmentedOutput(final File file, final int fragmentBufferSize, final int totalBufferSize,
			final int threshold) {
		this(file, new Config(fragmentBufferSize, totalBufferSize, threshold, Config.DEFAULT.segmentSize()));
	}

	/**
	 * Creates a new file output with default buffer settings.
	 * 
	 * @param file target output file.
	 * @see Config#DEFAULT
	 */
	public FileFragmentedOutput(final File file) {
		super();
		this.file = file;
	}

	/**
	 * Writes data directly to the output file.
	 * <p>
	 * This method is used for sequential output mode. On first call,
	 * creates the output stream to the target file.
	 * </p>
	 * 
	 * @param b   byte array containing data.
	 * @param off start offset in the array.
	 * @param len number of bytes to write.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (this.out == null) {
			this.out = new FileOutputStream(this.file);
		}
		this.out.write(b, off, len);
	}

	/**
	 * Closes the output and writes all data to the file.
	 * <p>
	 * If sequential output was used, simply closes the stream.
	 * If fragments were used, assembles them in order and writes to file.
	 * </p>
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		try {
			// If sequential mode was used, just close the stream
			if (this.out != null) {
				this.out.close();
				this.out = null;
				return;
			}
			// Otherwise, assemble fragments and write to file
			try (final var os = new FileOutputStream(this.file)) {
				this.finish(os);
			}
		} finally {
			super.close();
		}
	}
}