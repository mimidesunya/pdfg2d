package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * An OutputStream adapter for SequentialOutput.
 * <p>
 * This class allows code that expects an OutputStream to write to a
 * SequentialOutput. All write operations are delegated to the wrapped
 * SequentialOutput's write method.
 * </p>
 * <p>
 * Closing this adapter closes the underlying SequentialOutput.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SequentialOutputAdapter extends OutputStream {
	/** The wrapped SequentialOutput. */
	private final SequentialOutput out;

	/**
	 * Creates a new adapter for the specified SequentialOutput.
	 * 
	 * @param out the SequentialOutput to wrap; must not be null.
	 * @throws NullPointerException if out is null.
	 */
	public SequentialOutputAdapter(final SequentialOutput out) {
		this.out = Objects.requireNonNull(out);
	}

	/**
	 * Writes a single byte to the output.
	 * 
	 * @param b the byte to write (only the lower 8 bits are used).
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final int b) throws IOException {
		final byte[] buff = { (byte) b };
		this.out.write(buff, 0, 1);
	}

	/**
	 * Writes a portion of a byte array to the output.
	 * 
	 * @param b   the byte array.
	 * @param off the start offset.
	 * @param len the number of bytes to write.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.out.write(b, off, len);
	}

	/**
	 * Writes the entire byte array to the output.
	 * 
	 * @param b the byte array.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		this.out.write(b, 0, b.length);
	}

	/**
	 * Closes the underlying SequentialOutput.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void close() throws IOException {
		this.out.close();
	}
}