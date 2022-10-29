package jp.cssj.resolver.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;

/**
 * HTTP/HTTPS接続からデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: HttpSource.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class HttpSource extends AbstractSource {
	private final CloseableHttpClient httpClient;

	private String mimeType, encoding;

	private HttpUriRequest req;

	private HttpResponse res;

	private InputStream in;

	private boolean exists;

	private long lastModified = -1;

	private long contentLength = -1;

	public HttpSource(URI uri, CloseableHttpClient httpClient) {
		super(uri);
		this.httpClient = httpClient;
	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public String getMimeType() throws IOException {
		this.tryConnect();
		return this.mimeType;
	}

	public String getEncoding() throws IOException {
		this.tryConnect();
		return this.encoding;
	}

	public boolean exists() throws IOException {
		this.tryConnect();
		return this.exists;
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public boolean isReader() throws IOException {
		this.tryConnect();
		return this.encoding != null;
	}

	public synchronized InputStream getInputStream() throws IOException {
		if (this.in != null) {
			if (this.res != null) {
				try {
					// EntityUtils.consume()と同じ
					final HttpEntity e = this.res.getEntity();
					if (e != null) {
						final InputStream in = e.getContent();
						if (in != null) {
							in.close();
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
		HttpEntity entity = this.res.getEntity();
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
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
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
			final Header mimeType = entity.getContentType();
			if (mimeType != null) {
				this.mimeType = mimeType.getValue();
			}
		}
		final Header lastModified = this.res.getLastHeader("Last-Modified");
		if (lastModified != null) {
			this.lastModified = DateUtils.parseDate(lastModified.getValue()).getTime();
		}
		final Header contentLength = this.res.getLastHeader("Content-Length");
		if (contentLength != null) {
			try {
				this.contentLength = Long.parseLong(contentLength.getValue());
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

	protected HttpUriRequest createHttpRequest() {
		HttpGet method = new HttpGet(this.uri);
		return method;
	}

	public Reader getReader() throws IOException {
		if (this.encoding == null) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public File getFile() {
		throw new UnsupportedOperationException();
	}

	public long getLength() throws IOException {
		this.tryConnect();
		return this.contentLength;
	}

	public SourceValidity getValidity() {
		return new HttpSourceValidity(this.lastModified);
	}

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
				try {
					this.httpClient.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}