package net.zamasoft.pdfg2d.resolver.protocol.http;

import java.io.IOException;
import java.net.URI;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * SourceResolver that retrieves data using HttpClient.
 */
public class HTTPSourceResolver implements SourceResolver {
	private CloseableHttpClient sharedClient;

	protected synchronized CloseableHttpClient getSharedClient() {
		if (this.sharedClient == null) {
			final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(20);
			cm.setDefaultMaxPerRoute(2);
			this.sharedClient = HttpClientBuilder.create().setConnectionManager(cm).build();
		}
		return this.sharedClient;
	}

	@Override
	public Source resolve(final URI uri) throws IOException {
		return new HTTPSource(uri, this.getSharedClient(), false);
	}

	@Override
	public void release(final Source source) {
		if (source instanceof final HTTPSource httpSource) {
			httpSource.close();
		}
	}
}
