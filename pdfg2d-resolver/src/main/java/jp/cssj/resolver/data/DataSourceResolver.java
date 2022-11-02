package jp.cssj.resolver.data;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * RFC2397,data:スキーマを解決するSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class DataSourceResolver implements SourceResolver {
	public Source resolve(URI uri) throws IOException {
		DataSource source = new DataSource(uri);
		return source;
	}

	public void release(Source source) {
		((DataSource) source).close();
	}
}