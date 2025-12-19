package net.zamasoft.pdfg2d.io;

import java.io.IOException;

/**
 * Sequential data output. An implementation of this interface does not
 * necessarily require adding fragments manually.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface SequentialOutput extends FragmentedOutput {
	/**
	 * Adds data.
	 * 
	 * @param b   byte array.
	 * @param off start position in the byte array.
	 * @param len length of data in the byte array.
	 * @throws IOException if an I/O error occurs.
	 */
	void write(byte[] b, int off, int len) throws IOException;
}
