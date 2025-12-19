package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream for PDF fragments.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFFragmentOutput extends PDFOutput {
	public enum Mode {
		/** Writes data output to the stream directly to PDF. */
		RAW,
		/** Performs compression suitable for binary data. */
		BINARY,
		/** Performs compression suitable for text data. */
		ASCII;
	}

	protected PDFFragmentOutput(final OutputStream out, final String nameEncoding) throws IOException {
		super(out, nameEncoding);
	}

	/**
	 * Writes the start of an object.
	 * 
	 * @param ref the object reference
	 * @throws IOException in case of I/O error
	 */
	public abstract void startObject(ObjectRef ref) throws IOException;

	/**
	 * Writes the end of an object.
	 * 
	 * @throws IOException in case of I/O error
	 */
	public abstract void endObject() throws IOException;

	/**
	 * Writes the start of a stream.
	 * 
	 * @param mode the compression mode
	 * @return the output stream for the stream content
	 * @throws IOException in case of I/O error
	 */
	public abstract OutputStream startStream(Mode mode) throws IOException;

	/**
	 * Writes the start of a stream from within a dictionary (hash). This method
	 * closes the dictionary.
	 * 
	 * @param mode the compression mode
	 * @return the output stream for the stream content
	 * @throws IOException in case of I/O error
	 */
	public abstract OutputStream startStreamFromHash(Mode mode) throws IOException;
}