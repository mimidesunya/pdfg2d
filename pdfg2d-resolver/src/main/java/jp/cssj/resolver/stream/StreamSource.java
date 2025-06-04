package jp.cssj.resolver.stream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;
import jp.cssj.resolver.helpers.UnknownSourceValidity;

/**
 * ストリームからデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamSource extends AbstractSource {
	private static final int MARK_LIMIT = 8192;

	private final String mimeType, encoding;

	private final BufferedInputStream in;

	private final BufferedReader reader;

	private final long length;

	public StreamSource(URI uri, InputStream in, String mimeType,
			String encoding, long length) throws UnsupportedEncodingException {
		super(uri);
		if (in == null) {
			throw new NullPointerException();
		}
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.in = new BufferedInputStream(in) {
			public void close() throws IOException {
				// ignore
			}
		};
		this.in.mark(MARK_LIMIT);
		this.reader = null;
		this.length = length;
	}

	public StreamSource(URI uri, InputStream in, String mimeType, long length) {
		super(uri);
		if (in == null) {
			throw new NullPointerException();
		}
		this.mimeType = mimeType;
		this.in = new BufferedInputStream(in) {
			public void close() throws IOException {
				// ignore
			}
		};
		this.in.mark(MARK_LIMIT);
		this.encoding = null;
		this.reader = null;
		this.length = length;
	}

	public StreamSource(URI uri, Reader reader, String mimeType,
			String encoding, long length) throws IOException {
		super(uri);
		if (reader == null) {
			throw new NullPointerException();
		}
		this.mimeType = mimeType;
		this.in = null;
		this.encoding = encoding;
		this.reader = new BufferedReader(reader) {
			public void close() throws IOException {
				// ignore
			}
		};
		this.reader.mark(MARK_LIMIT);
		this.length = length;
	}

	public StreamSource(URI uri, InputStream in, String mimeType,
			String encoding) throws UnsupportedEncodingException {
		this(uri, in, mimeType, encoding, -1L);
	}

	public StreamSource(URI uri, InputStream in, String mimeType) {
		this(uri, in, mimeType, -1L);
	}

	public StreamSource(URI uri, InputStream in) {
		this(uri, in, null, -1L);
	}

	public StreamSource(URI uri, Reader reader, String mimeType, String encoding)
			throws IOException {
		this(uri, reader, mimeType, encoding, -1L);
	}

	public StreamSource(URI uri, Reader reader, String mimeType)
			throws IOException {
		this(uri, reader, mimeType, null, -1L);
	}

	public StreamSource(URI uri, Reader reader) throws IOException {
		this(uri, reader, null, null, -1L);
	}

	public URI getURI() {
		return this.uri;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public boolean exists() {
		return true;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean isInputStream() {
		return this.in != null;
	}

	public boolean isReader() {
		return this.reader != null || this.encoding != null;
	}

	public InputStream getInputStream() throws IOException {
		if (this.in == null) {
			throw new UnsupportedOperationException();
		}
		this.in.reset();
		this.in.mark(MARK_LIMIT);
		return this.in;
	}

	public Reader getReader() throws IOException {
		if (this.reader == null) {
			if (this.encoding == null) {
				throw new UnsupportedOperationException();
			}
			return new InputStreamReader(this.getInputStream(), this.encoding);
		}
		this.reader.reset();
		this.reader.mark(MARK_LIMIT);
		return this.reader;
	}

	public File getFile() {
		throw new UnsupportedOperationException();
	}

	public long getLength() throws IOException {
		return this.length;
	}

	public SourceValidity getValidity() {
		return UnknownSourceValidity.SHARED_INSTANCE;
	}
}