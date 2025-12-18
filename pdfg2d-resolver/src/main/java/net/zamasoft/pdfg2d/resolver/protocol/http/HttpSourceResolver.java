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
public class HttpSourceResolver implements SourceResolver {
	protected CloseableHttpClient createHttpClient() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		return HttpClientBuilder.create().setConnectionManager(cm).build();
	}

	@Override
	public Source resolve(final URI uri) throws IOException {
		final CloseableHttpClient client = this.createHttpClient();
		return new HttpSource(uri, client);
	}

	@Override
	public void release(final Source source) {
		if (source instanceof HttpSource httpSource) {
			httpSource.close();
		}
	}
}
