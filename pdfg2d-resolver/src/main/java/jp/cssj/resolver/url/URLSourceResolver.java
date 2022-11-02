package jp.cssj.resolver.url;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * java.net.URLを利用してリソースを所得します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class URLSourceResolver implements SourceResolver {
	public Source resolve(URI uri) throws IOException {
		try {
			URL url = uri.toURL();
			URLSource source;
			try {
				source = new URLSource(url, null, null);
			} catch (URISyntaxException e) {
				IOException ioe = new IOException(e.getMessage());
				ioe.initCause(e);
				throw ioe;
			}
			return source;
		} catch (IllegalArgumentException e) {
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	public void release(Source source) {
		((URLSource) source).close();
	}
}