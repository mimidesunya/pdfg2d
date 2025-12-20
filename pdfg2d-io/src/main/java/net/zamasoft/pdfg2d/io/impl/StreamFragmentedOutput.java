package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * Sequential output implementation that writes fragmented data to an
 * OutputStream.
 * <p>
 * This class extends {@link AbstractTempFileOutput} and implements
 * {@link SequentialOutput} to provide stream-based output for PDF generation.
 * </p>
 * <p>
 * When used in sequential mode, data is written directly to the target stream.
 * When fragments are inserted out of order, data is buffered in memory or
 * a temporary file and assembled in the correct order when closed.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamFragmentedOutput extends AbstractTempFileOutput implements SequentialOutput {
	/** Target output stream. */
	private final OutputStream out;

	/**
	 * Creates a new stream output with custom buffer settings.
	 * 
	 * @param out    target output stream.
	 * @param config buffer configuration.
	 */
	public StreamFragmentedOutput(final OutputStream out, final Config config) {
		super(config);
		this.out = out;
	}

	/**
	 * Creates a new stream output with custom buffer settings.
	 * 
	 * @param out                target output stream.
	 * @param fragmentBufferSize maximum buffer size per fragment.
	 * @param totalBufferSize    maximum total buffer size in memory.
	 * @param threshold          threshold to flush fragment data to disk.
	 * @deprecated Use {@link #StreamFragmentedOutput(OutputStream, Config)}
	 *             instead.
	 */
	@Deprecated
	public StreamFragmentedOutput(final OutputStream out, final int fragmentBufferSize, final int totalBufferSize,
			final int threshold) {
		this(out, new Config(fragmentBufferSize, totalBufferSize, threshold, Config.DEFAULT.segmentSize()));
	}

	/**
	 * Creates a new stream output with default buffer settings.
	 * 
	 * @param out target output stream.
	 * @see Config#DEFAULT
	 */
	public StreamFragmentedOutput(final OutputStream out) {
		super();
		this.out = out;
	}

	/**
	 * Writes data directly to the output stream.
	 * <p>
	 * This method is used for sequential output mode.
	 * </p>
	 * 
	 * @param b   byte array containing data.
	 * @param off start offset in the array.
	 * @param len number of bytes to write.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.out.write(b, off, len);
	}

	/**
	 * Closes the output and writes all data to the stream.
	 * <p>
	 * Assembles all fragments in order, writes to the stream,
	 * and closes the underlying stream.
	 * </p>
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		try {
			this.finish(this.out);
			this.out.close();
		} finally {
			super.close();
		}
	}
}