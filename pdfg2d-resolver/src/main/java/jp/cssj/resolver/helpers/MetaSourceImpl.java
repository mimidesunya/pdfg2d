package jp.cssj.resolver.helpers;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.MetaSource;
import jp.cssj.resolver.Source;

/**
 * デフォルトのデータのメタ情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class MetaSourceImpl implements MetaSource {
	private static final URI CURRENT_URI = URI.create(".");

	private URI uri;
	private String encoding;
	private String mimeType;
	private long length;

	public MetaSourceImpl() {
		this((URI) null);
	}

	public MetaSourceImpl(URI uri) {
		this(uri, null);
	}

	public MetaSourceImpl(URI uri, String mimeType) {
		this(uri, mimeType, null);
	}

	public MetaSourceImpl(URI uri, String mimeType, String encoding) {
		this(uri, mimeType, encoding, -1L);
	}

	public MetaSourceImpl(URI uri, String mimeType, String encoding, long length) {
		if (uri == null) {
			uri = CURRENT_URI;
		}
		this.uri = uri;
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.length = length;
	}

	public MetaSourceImpl(Source source) throws IOException {
		this(source.getURI(), source.getMimeType(), source.getEncoding(),
				source.getLength());
	}

	public URI getURI() {
		return this.uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getLength() {
		return this.length;
	}

	public void setLength(long length) {
		this.length = length;
	}
}
