package net.zamasoft.pdfg2d.resolver.protocol.url;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;

/**
 * A Source that retrieves data from a java.net.URL.
 */
public class URLSource extends AbstractSource {
	private static final Logger LOG = Logger.getLogger(URLSource.class.getName());

	private final URL url;
	private final String encoding;
	private String mimeType = null;

	// Transient in case of serialization, though this class doesn't explicitly
	// implement Serializable.
	private transient URLConnection conn = null;
	private transient InputStream in = null;
	private long timestamp = -1L;

	public URLSource(final URI uri, final URL url, final String mimeType, final String encoding) {
		super(uri);
		this.url = Objects.requireNonNull(url, "URL must not be null");
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public URLSource(final URL url, final String mimeType, final String encoding) throws URISyntaxException {
		this(url.toURI(), url, mimeType, encoding);
	}

	public URLSource(final URL url, final String mimeType) throws URISyntaxException {
		this(url, mimeType, null);
	}

	public URLSource(final URL url) throws URISyntaxException {
		this(url, null);
	}

	public URLSource(final URI uri, final String mimeType, final String encoding) throws MalformedURLException {
		this(uri, uri.toURL(), mimeType, encoding);
	}

	public URLSource(final URI uri, final String mimeType) throws MalformedURLException {
		this(uri, uri.toURL(), mimeType, null);
	}

	public URLSource(final URI uri) throws MalformedURLException {
		this(uri, uri.toURL(), null, null);
	}

	@Override
	public String getMimeType() throws IOException {
		if (this.mimeType == null) {
			if (this.isFile()) {
				final String filename = this.getFile().getName();
				final int dot = filename.lastIndexOf('.');
				if (dot != -1) {
					final String suffix = filename.substring(dot).toLowerCase();
					this.mimeType = switch (suffix) {
						case ".html", ".htm" -> "text/html";
						case ".xml", ".xhtml", ".xht" -> "text/xml";
						default -> null;
					};
					if (this.mimeType != null) {
						return this.mimeType;
					}
				}
			}
			try {
				if (this.conn == null) {
					this.connect();
				}
				this.mimeType = this.conn.getContentType();
			} catch (IOException e) {
				this.conn = null;
			}
		}
		return this.mimeType;
	}

	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public boolean exists() throws IOException {
		if (this.isFile()) {
			return this.getFile().exists();
		}
		return true;
	}

	@Override
	public boolean isFile() throws IOException {
		return "file".equals(this.uri.getScheme());
	}

	@Override
	public boolean isInputStream() throws IOException {
		return true;
	}

	@Override
	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (this.in != null) {
			this.in = null;
			this.conn = null;
			this.timestamp = -1L;
		}
		if (this.isFile()) {
			this.in = new FileInputStream(this.getFile());
			return this.in;
		}
		if (this.conn == null) {
			this.connect();
		}
		this.in = this.conn.getInputStream();
		return this.in;
	}

	@Override
	public Reader getReader() throws IOException {
		if (this.encoding == null) {
			throw new UnsupportedOperationException("Encoding not set");
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	@Override
	public synchronized void close() {
		if (this.in != null) {
			try {
				this.in.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "Exception while closing URL connection", e);
			} finally {
				this.in = null;
				this.conn = null;
				this.timestamp = -1L;
			}
		}
	}

	private void connect() throws IOException {
		this.conn = this.url.openConnection();
		this.timestamp = this.conn.getLastModified();
	}

	@Override
	public File getFile() {
		if ("file".equals(this.uri.getScheme())) {
			return new File(this.uri);
		}
		// Fallback for non-file UI but getFile called?
		String path = this.uri.getPath();
		if (path == null) {
			path = this.uri.getSchemeSpecificPart();
		}
		return new File(path);
	}

	@Override
	public long getLength() throws IOException {
		if (this.isFile()) {
			return this.getFile().length();
		}
		if (this.conn == null) {
			this.connect();
		}
		return this.conn.getContentLengthLong();
	}

	@Override
	public SourceValidity getValidity() throws IOException {
		this.connect();
		return new URLSourceValidity(this.timestamp, this.url);
	}
}
