package net.zamasoft.pdfg2d.resolver.protocol.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;

/**
 * A Source that retrieves data from an HTTP/HTTPS connection.
 */
public class HTTPSource extends AbstractSource {
	private final CloseableHttpClient httpClient;
	private final boolean closeClient;

	private String mimeType;
	private String encoding;

	private HttpUriRequest req;
	private HttpResponse res;
	private InputStream in;

	private boolean exists;
	private long lastModified = -1;
	private long contentLength = -1;

	public HTTPSource(final URI uri, final CloseableHttpClient httpClient) {
		this(uri, httpClient, true);
	}

	public HTTPSource(final URI uri, final CloseableHttpClient httpClient, final boolean closeClient) {
		super(uri);
		this.httpClient = httpClient;
		this.closeClient = closeClient;
	}

	public CloseableHttpClient getHttpClient() {
		return this.httpClient;
	}

	@Override
	public String getMimeType() throws IOException {
		this.tryConnect();
		return this.mimeType;
	}

	@Override
	public String getEncoding() throws IOException {
		this.tryConnect();
		return this.encoding;
	}

	@Override
	public boolean exists() throws IOException {
		this.tryConnect();
		return this.exists;
	}

	@Override
	public boolean isInputStream() throws IOException {
		return true;
	}

	@Override
	public boolean isReader() throws IOException {
		this.tryConnect();
		return this.encoding != null;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if (this.in != null) {
			if (this.res != null) {
				try {
					final HttpEntity e = this.res.getEntity();
					if (e != null) {
						final InputStream is = e.getContent(); // avoid name clash with field 'in'
						if (is != null) {
							is.close();
						}
					}
				} catch (IOException e) {
					// ignore
				}
				this.res = null;
			}
			this.req = null;
			this.in = null;
		}
		this.tryConnect();
		final HttpEntity entity = this.res.getEntity();
		if (entity == null) {
			throw new FileNotFoundException();
		}
		this.in = entity.getContent();
		return this.in;
	}

	protected void tryConnect() throws IOException {
		if (this.req != null) {
			return;
		}
		this.req = this.createHttpRequest();
		final int status;
		try {
			this.res = this.httpClient.execute(this.req);
			status = this.res.getStatusLine().getStatusCode();
		} catch (Exception e) {
			throw new IOException(e);
		}
		this.exists = status != 404;
		final HttpEntity entity = this.res.getEntity();
		this.encoding = null;
		if (entity != null) {
			final Header encodingHeader = entity.getContentEncoding();
			if (encodingHeader != null) {
				this.encoding = encodingHeader.getValue();
				try {
					if (this.encoding.equalsIgnoreCase("ISO-8859-1") || !Charset.isSupported(this.encoding)) {
						this.encoding = null;
					}
				} catch (Exception e) {
					// ignore
				}
			}
			final Header mimeTypeHeader = entity.getContentType();
			if (mimeTypeHeader != null) {
				this.mimeType = mimeTypeHeader.getValue();
			}
		}
		final Header lastModifiedHeader = this.res.getLastHeader("Last-Modified");
		if (lastModifiedHeader != null) {
			final Date date = DateUtils.parseDate(lastModifiedHeader.getValue());
			if (date != null) {
				this.lastModified = date.getTime();
			}
		}
		final Header contentLengthHeader = this.res.getLastHeader("Content-Length");
		if (contentLengthHeader != null) {
			try {
				this.contentLength = Long.parseLong(contentLengthHeader.getValue());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

	protected HttpUriRequest createHttpRequest() {
		return new HttpGet(this.uri);
	}

	@Override
	public Reader getReader() throws IOException {
		if (this.encoding == null) {
			throw new UnsupportedOperationException("Encoding not set");
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLength() throws IOException {
		this.tryConnect();
		return this.contentLength;
	}

	@Override
	public SourceValidity getValidity() {
		return new HTTPSourceValidity(this.lastModified);
	}

	@Override
	public void close() {
		if (this.req != null) {
			try {
				if (this.in != null) {
					try {
						this.in.close();
					} catch (IOException e) {
						// ignore
					}
				}
			} finally {
				this.res = null;
				this.req = null;
				this.in = null;
				if (this.closeClient) {
					try {
						this.httpClient.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}
}
