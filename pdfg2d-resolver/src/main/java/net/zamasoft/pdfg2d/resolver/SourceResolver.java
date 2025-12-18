package net.zamasoft.pdfg2d.resolver;

import java.io.IOException;
import java.net.URI;

/**
 * Resolves a Source from a URI.
 */
public interface SourceResolver {
	/**
	 * Returns the source corresponding to the URI.
	 * 
	 * @param uri The URI indicating the location of the source.
	 * @return The source corresponding to the URI.
	 * @throws IOException If an I/O error occurs.
	 */
	Source resolve(URI uri) throws IOException;

	/**
	 * Releases the acquired source.
	 * 
	 * @param source The source acquired by resolve method of this SourceResolver.
	 */
	void release(Source source);
}

