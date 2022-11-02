package jp.cssj.resolver.http;

import java.io.IOException;
import java.net.URI;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * HttpClientを利用してデータを所得するSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class HttpSourceResolver implements SourceResolver {
	protected CloseableHttpClient createHttpClient() {
		final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		final CloseableHttpClient client = HttpClientBuilder.create().setConnectionManager(cm).build();
		return client;
	}

	public Source resolve(final URI uri) throws IOException {
		final CloseableHttpClient client = this.createHttpClient();
		final HttpSource source = new HttpSource(uri, client);
		return source;
	}

	public void release(final Source source) {
		((HttpSource) source).close();
	}
}