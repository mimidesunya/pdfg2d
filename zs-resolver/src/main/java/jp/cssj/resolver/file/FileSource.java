package jp.cssj.resolver.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;
import jp.cssj.resolver.helpers.URIHelper;

/**
 * ファイルからデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FileSource.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class FileSource extends AbstractSource {
	private final File file;

	private final String encoding;

	private String mimeType = null;

	public FileSource(File file, URI uri, String mimeType, String encoding) {
		super(uri);
		if (file == null) {
			throw new NullPointerException();
		}
		this.file = file;
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public FileSource(URI uri) throws IOException {
		super(uri);
		String path = uri.getSchemeSpecificPart();
		path = URIHelper.decode(path);
		this.file = new File(path);
		this.mimeType = null;
		this.encoding = null;
	}

	public FileSource(File file, String mimeType, String encoding) {
		this(file, file.toURI(), mimeType, encoding);
	}

	public FileSource(File file, String mimeType) {
		this(file, mimeType, null);
	}

	public FileSource(File file) {
		this(file, null);
	}

	public String getMimeType() throws IOException {
		if (this.mimeType == null) {
			String filename = this.file.getName();
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
			}
		}
		return this.mimeType;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean exists() throws IOException {
		return this.file.exists();
	}

	public boolean isFile() throws IOException {
		return true;
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public File getFile() {
		return this.file;
	}

	public long getLength() throws IOException {
		return this.file.length();
	}

	public SourceValidity getValidity() throws IOException {
		long timestamp = this.file.lastModified();
		return new FileSourceValidity(timestamp, this.file);
	}
}