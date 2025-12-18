package net.zamasoft.pdfg2d.resolver.util;

import java.io.IOException;
import java.net.URI;
import net.zamasoft.pdfg2d.resolver.Source;

/**
 * Abstract implementation of Source.
 */
public abstract class AbstractSource implements Source {
	protected final URI uri;

	public AbstractSource(URI uri) {
		this.uri = uri;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public boolean isInputStream() throws IOException {
		return false;
	}

	@Override
	public boolean isFile() throws IOException {
		return false;
	}

	@Override
	public boolean isReader() throws IOException {
		return false;
	}

	@Override
	public void close() throws IOException {
		// Default implementation does nothing
	}

	@Override
	public String toString() {
		return this.uri.toString();
	}
}

