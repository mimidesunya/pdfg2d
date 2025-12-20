package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * A wrapper that measures the total size of data being written.
 * <p>
 * This class extends {@link FragmentedOutputWrapper} to track the total
 * number of bytes written across all fragments. Useful for calculating
 * content sizes before finalizing output.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OutputMeasurer extends FragmentedOutputWrapper {
	/** Accumulated length of all written data. */
	protected long length;

	/**
	 * Creates a new measurer wrapping the specified output.
	 * 
	 * @param builder the FragmentedOutput to wrap.
	 */
	public OutputMeasurer(final FragmentedOutput builder) {
		super(builder);
	}

	/**
	 * Writes data to the fragment and accumulates the length.
	 * 
	 * @param id  fragment ID.
	 * @param b   byte array.
	 * @param off start offset.
	 * @param len number of bytes to write.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		super.write(id, b, off, len);
		this.length += len;
	}

	/**
	 * Returns the total size of all written data.
	 * 
	 * @return total bytes written.
	 */
	public long getLength() {
		return this.length;
	}
}
