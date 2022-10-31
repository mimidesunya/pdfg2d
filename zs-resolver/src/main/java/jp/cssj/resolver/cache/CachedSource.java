package jp.cssj.resolver.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.UnknownSourceValidity;

/**
 * 仮想的なURIと保存されたファイルを結びつけたデータです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CachedSource implements Source {
	private static final Logger LOG = Logger.getLogger(CachedSource.class
			.getName());

	private final URI uri;

	private final String mimeType, encoding;

	private final File file;

	private InputStream in = null;

	public CachedSource(URI uri, String mimeType, String encoding, File file) {
		this.uri = uri;
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.file = file;
		if (uri == null) {
			throw new NullPointerException("uri");
		}
		if (file == null) {
			throw new NullPointerException("file");
		}
	}

	public URI getURI() {
		return this.uri;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public InputStream getInputStream() throws IOException {
		if (this.in != null) {
			this.close();
		}
		return this.in = new FileInputStream(this.file);
	}

	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public boolean isFile() {
		return true;
	}

	public File getFile() {
		return this.file;
	}

	public void close() {
		if (this.in != null) {
			try {
				this.in.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "リソースへの接続を中断した際に例外が発生しました", e);
			} finally {
				this.in = null;
			}
		}
	}

	public boolean exists() throws IOException {
		return true;
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public long getLength() throws IOException {
		return this.file.length();
	}

	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	public SourceValidity getValidity() {
		return UnknownSourceValidity.SHARED_INSTANCE;
	}
}