package jp.cssj.resolver.data;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * RFC2397,data:スキーマを解決するSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: DataSourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
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