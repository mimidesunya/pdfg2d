package net.zamasoft.pdfg2d.resolver;

import java.io.IOException;
import java.net.URI;

/**
 * Metadata for a data source.
 */
public interface MetaSource {
	/**
	 * Returns the URI of the data.
	 * 
	 * @return The URI indicating the location of the data.
	 */
	URI getURI();

	/**
	 * Returns the MIME type of the data.
	 * 
	 * @return The MIME type of this data, or null if uncertain.
	 * @throws IOException If an I/O error occurs.
	 */
	String getMimeType() throws IOException;

	/**
	 * Returns the character encoding.
	 * 
	 * @return The character encoding of this data, or null if undetermined.
	 * @throws IOException If an I/O error occurs.
	 */
	String getEncoding() throws IOException;

	/**
	 * Returns the size of the data.
	 * 
	 * @return The number of bytes of the data, or -1 if unknown.
	 * @throws IOException If an I/O error occurs.
	 */
	long getLength() throws IOException;
}

