package net.zamasoft.pdfg2d.resolver.protocol.stream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Objects;
import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;
import net.zamasoft.pdfg2d.resolver.util.UnknownSourceValidity;

/**
 * A Source that retrieves data from a stream.
 */
public class StreamSource extends AbstractSource {
	private static final int MARK_LIMIT = 8192;

	private final String mimeType;
	private final String encoding;
	private final BufferedInputStream in;
	private final BufferedReader reader;
	private final long length;

	public StreamSource(URI uri, InputStream in, String mimeType, String encoding, long length)
			throws UnsupportedEncodingException {
		super(uri);
		Objects.requireNonNull(in, "InputStream must not be null");
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.in = new BufferedInputStream(in) {
			@Override
			public void close() throws IOException {
				// Ignore close to prevent closing the underlying stream unexpectedly
			}
		};
		this.in.mark(MARK_LIMIT);
		this.reader = null;
		this.length = length;
	}

	public StreamSource(URI uri, InputStream in, String mimeType, long length) {
		super(uri);
		Objects.requireNonNull(in, "InputStream must not be null");
		this.mimeType = mimeType;
		this.in = new BufferedInputStream(in) {
			@Override
			public void close() throws IOException {
				// ignore
			}
		};
		this.in.mark(MARK_LIMIT);
		this.encoding = null;
		this.reader = null;
		this.length = length;
	}

	public StreamSource(URI uri, Reader reader, String mimeType, String encoding, long length) throws IOException {
		super(uri);
		Objects.requireNonNull(reader, "Reader must not be null");
		this.mimeType = mimeType;
		this.in = null;
		this.encoding = encoding;
		this.reader = new BufferedReader(reader) {
			@Override
			public void close() throws IOException {
				// ignore
			}
		};
		this.reader.mark(MARK_LIMIT);
		this.length = length;
	}

	public StreamSource(URI uri, InputStream in, String mimeType, String encoding) throws UnsupportedEncodingException {
		this(uri, in, mimeType, encoding, -1L);
	}

	public StreamSource(URI uri, InputStream in, String mimeType) {
		this(uri, in, mimeType, -1L);
	}

	public StreamSource(URI uri, InputStream in) {
		this(uri, in, null, -1L);
	}

	public StreamSource(URI uri, Reader reader, String mimeType, String encoding) throws IOException {
		this(uri, reader, mimeType, encoding, -1L);
	}

	public StreamSource(URI uri, Reader reader, String mimeType) throws IOException {
		this(uri, reader, mimeType, null, -1L);
	}

	public StreamSource(URI uri, Reader reader) throws IOException {
		this(uri, reader, null, null, -1L);
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public String getMimeType() {
		return this.mimeType;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public boolean isInputStream() {
		return this.in != null;
	}

	@Override
	public boolean isReader() {
		return this.reader != null || this.encoding != null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (this.in == null) {
			throw new UnsupportedOperationException();
		}
		this.in.reset();
		this.in.mark(MARK_LIMIT);
		return this.in;
	}

	@Override
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

	@Override
	public File getFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLength() throws IOException {
		return this.length;
	}

	@Override
	public SourceValidity getValidity() {
		return UnknownSourceValidity.SHARED_INSTANCE;
	}
}
