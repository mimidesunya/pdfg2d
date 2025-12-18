package net.zamasoft.pdfg2d.resolver;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * A source of data, such as a file or web document.
 */
public interface Source extends MetaSource, Closeable {
	/**
	 * Returns true if the data exists.
	 * 
	 * @return true if data exists, false otherwise.
	 * @throws IOException If an I/O error occurs.
	 */
	boolean exists() throws IOException;

	/**
	 * Returns true if the data can be obtained as a binary stream.
	 * 
	 * @return true if binary data is available.
	 * @throws IOException If an I/O error occurs.
	 */
	boolean isInputStream() throws IOException;

	/**
	 * Returns the binary stream of the data.
	 * 
	 * @return The binary stream of the data.
	 * @throws IOException If an I/O error occurs.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns true if the data can be obtained as a text stream.
	 * 
	 * @return true if text data is available.
	 * @throws IOException If an I/O error occurs.
	 */
	boolean isReader() throws IOException;

	/**
	 * Returns the text stream of the data.
	 * 
	 * @return The text stream of the data.
	 * @throws IOException If an I/O error occurs.
	 */
	Reader getReader() throws IOException;

	/**
	 * Returns true if the data is a file.
	 * 
	 * @return true if the data is a file.
	 * @throws IOException If an I/O error occurs.
	 */
	boolean isFile() throws IOException;

	/**
	 * Returns the data as a file.
	 * 
	 * @return The file containing the data.
	 * @throws UnsupportedOperationException If the data is not a file.
	 */
	File getFile();

	/**
	 * Returns validity information about the data.
	 * 
	 * @return The validity status of the data.
	 * @throws IOException If an I/O error occurs.
	 */
	SourceValidity getValidity() throws IOException;
}

