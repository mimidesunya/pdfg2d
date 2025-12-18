package net.zamasoft.pdfg2d.resolver.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceValidity;

/**
 * A wrapper class for Source.
 */
public class SourceWrapper implements Source {
	protected final Source source;

	public SourceWrapper(Source source) {
		this.source = source;
	}

	@Override
	public boolean exists() throws IOException {
		return this.source.exists();
	}

	@Override
	public String getEncoding() throws IOException {
		return this.source.getEncoding();
	}

	@Override
	public File getFile() {
		return this.source.getFile();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.source.getInputStream();
	}

	@Override
	public long getLength() throws IOException {
		return this.source.getLength();
	}

	@Override
	public String getMimeType() throws IOException {
		return this.source.getMimeType();
	}

	@Override
	public Reader getReader() throws IOException {
		return this.source.getReader();
	}

	@Override
	public URI getURI() {
		return this.source.getURI();
	}

	@Override
	public SourceValidity getValidity() throws IOException {
		return this.source.getValidity();
	}

	@Override
	public boolean isFile() throws IOException {
		return this.source.isFile();
	}

	@Override
	public boolean isInputStream() throws IOException {
		return this.source.isInputStream();
	}

	@Override
	public boolean isReader() throws IOException {
		return this.source.isReader();
	}

	@Override
	public void close() throws IOException {
		this.source.close();
	}

	@Override
	public String toString() {
		return this.source.toString();
	}
}

