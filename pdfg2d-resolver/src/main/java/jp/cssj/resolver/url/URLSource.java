package jp.cssj.resolver.url;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;

/**
 * java.net.URLからデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class URLSource extends AbstractSource {
	private static final Logger LOG = Logger.getLogger(URLSource.class
			.getName());

	private final URL url;

	private final String encoding;

	private String mimeType = null;

	private transient URLConnection conn = null;

	private transient InputStream in = null;

	private long timestamp = -1L;

	public URLSource(URI uri, URL url, String mimeType, String encoding) {
		super(uri);
		if (url == null) {
			throw new NullPointerException();
		}
		this.url = url;
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public URLSource(URL url, String mimeType, String encoding)
			throws URISyntaxException {
		// JDK 1.4.xではtoURIが使えないため
		this(new URI(url.toString()), url, mimeType, encoding);
	}

	public URLSource(URL url, String mimeType) throws URISyntaxException {
		this(url, mimeType, null);
	}

	public URLSource(URL url) throws URISyntaxException {
		this(url, null);
	}

	public URLSource(URI uri, String mimeType, String encoding)
			throws MalformedURLException {
		this(uri, uri.toURL(), mimeType, encoding);
	}

	public URLSource(URI uri, String mimeType) throws MalformedURLException {
		this(uri, uri.toURL(), mimeType, null);
	}

	public URLSource(URI uri) throws MalformedURLException {
		this(uri, uri.toURL(), null, null);
	}

	public String getMimeType() throws IOException {
		if (this.mimeType == null) {
			if (this.isFile()) {
				String filename = this.getFile().getName();
				int dot = filename.indexOf('.');
				if (dot != -1) {
					String suffix = filename.substring(dot, filename.length());
					if (suffix.equalsIgnoreCase(".html")
							|| suffix.equalsIgnoreCase(".htm")) {
						this.mimeType = "text/html";
					} else if (suffix.equalsIgnoreCase(".xml")
							|| suffix.equalsIgnoreCase(".xhtml")
							|| suffix.equalsIgnoreCase(".xht")) {
						this.mimeType = "text/xml";
					}
					if (this.mimeType != null) {
						return this.mimeType;
					}
				}
			}
			try {
				if (this.conn == null) {
					this.connect();
				}
				return this.mimeType = this.conn.getContentType();
			} catch (IOException e) {
				this.conn = null;
			}
		}
		return this.mimeType;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean exists() throws IOException {
		if (this.isFile()) {
			return this.getFile().exists();
		}
		return true;
	}

	public boolean isFile() throws IOException {
		return "file".equals(this.uri.getScheme());
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	public synchronized InputStream getInputStream() throws IOException {
		if (this.in != null) {
			this.in = null;
			this.conn = null;
			this.timestamp = -1L;
		}
		if (this.isFile()) {
			return this.in = new FileInputStream(this.getFile());
		}
		if (this.conn == null) {
			this.connect();
		}
		return this.in = this.conn.getInputStream();
	}

	public Reader getReader() throws IOException {
		if (this.encoding == null) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public synchronized void close() {
		if (this.in != null) {
			try {
				this.in.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "URLへの接続を中断した際に例外が発生しました", e);
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

	public File getFile() {
		String path = this.uri.getPath();
		if (path == null) {
			path = this.uri.getSchemeSpecificPart();
		}
		return new File(path);
	}

	public long getLength() throws IOException {
		if (this.isFile()) {
			return this.getFile().length();
		}
		if (this.conn == null) {
			this.connect();
		}
		return this.conn.getContentLength();
	}

	public SourceValidity getValidity() throws IOException {
		this.connect();
		return new URLSourceValidity(this.timestamp, this.url);
	}
}