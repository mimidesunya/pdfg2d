package jp.cssj.resolver.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;
import jp.cssj.resolver.helpers.URIHelper;

/**
 * ZIPファイルからデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ZipFileSource extends AbstractSource {
	private static final Logger LOG = Logger.getLogger(ZipFileSource.class
			.getName());

	private final ZipFile zip;

	private final ZipEntry entry;

	private final String encoding;

	private String mimeType = null;

	public ZipFileSource(ZipFile zip, String path, URI uri, String mimeType,
			String encoding) {
		super(uri);
		if (zip == null) {
			throw new NullPointerException();
		}
		if (path == null) {
			path = uri.getSchemeSpecificPart();
			try {
				path = URIHelper.decode(path);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "URIをデコードできません。", e);
			}
		}
		this.entry = zip.getEntry(path);
		this.zip = zip;
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public ZipFileSource(ZipFile zip, URI uri, String mimeType, String encoding) {
		this(zip, null, uri, mimeType, encoding);
	}

	public ZipFileSource(ZipFile zip, URI uri, String mimeType) {
		this(zip, uri, mimeType, null);
	}

	public ZipFileSource(ZipFile zip, URI uri) throws IOException {
		this(zip, uri, null);
	}

	public ZipFileSource(ZipFile zip, String path, URI uri, String mimeType) {
		this(zip, path, uri, mimeType, null);
	}

	public ZipFileSource(ZipFile zip, String path, URI uri) {
		this(zip, path, uri, null, null);
	}

	public String getMimeType() throws IOException {
		if (this.mimeType == null) {
			String name = this.entry.getName();
			int dot = name.indexOf('.');
			if (dot != -1) {
				String suffix = name.substring(dot, name.length());
				if (suffix.equalsIgnoreCase(".html")
						|| suffix.equalsIgnoreCase(".htm")) {
					this.mimeType = "text/html";
				} else if (suffix.equalsIgnoreCase(".xml")
						|| suffix.equalsIgnoreCase(".xhtml")
						|| suffix.equalsIgnoreCase(".xht")) {
					this.mimeType = "text/xml";
				}
			}
		}
		return this.mimeType;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean exists() throws IOException {
		return this.entry != null;
	}

	public boolean isFile() throws IOException {
		return false;
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	public InputStream getInputStream() throws IOException {
		if (this.entry == null) {
			throw new FileNotFoundException(this.uri.toString());
		}
		return this.zip.getInputStream(this.entry);
	}

	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public File getFile() {
		throw new UnsupportedOperationException();
	}

	public long getLength() throws IOException {
		if (this.exists()) {
			return -1;
		}
		return 0;
	}

	public SourceValidity getValidity() throws IOException {
		File file = new File(this.zip.getName());
		long timestamp = file.lastModified();
		return new ZipFileSourceValidity(timestamp, file);
	}
}