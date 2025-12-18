package net.zamasoft.pdfg2d.resolver.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.UnknownSourceValidity;

/**
 * A data source backed by a cached file, tied to a virtual URI.
 */
public class CachedSource implements Source {
	private static final Logger LOG = Logger.getLogger(CachedSource.class.getName());

	private final URI uri;
	private final String mimeType;
	private final String encoding;
	private final File file;
	private InputStream in = null;

	public CachedSource(URI uri, String mimeType, String encoding, File file) {
		this.uri = Objects.requireNonNull(uri, "uri must not be null");
		this.file = Objects.requireNonNull(file, "file must not be null");
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public String getMimeType() {
		return this.mimeType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (this.in != null) {
			this.close();
		}
		this.in = new FileInputStream(this.file);
		return this.in;
	}

	@Override
	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public void close() {
		if (this.in != null) {
			try {
				this.in.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Exception occurred while interrupting connection to resource", e);
			} finally {
				this.in = null;
			}
		}
	}

	@Override
	public boolean exists() throws IOException {
		return true; // File assumed to exist in cache
	}

	@Override
	public boolean isInputStream() throws IOException {
		return true;
	}

	@Override
	public long getLength() throws IOException {
		return this.file.length();
	}

	@Override
	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	@Override
	public SourceValidity getValidity() {
		return UnknownSourceValidity.SHARED_INSTANCE;
	}
}
