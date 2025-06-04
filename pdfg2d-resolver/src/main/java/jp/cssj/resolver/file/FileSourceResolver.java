package jp.cssj.resolver.file;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * fileスキーマを解決するSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FileSourceResolver implements SourceResolver {
	public Source resolve(URI uri) throws IOException {
		FileSource source = new FileSource(uri);
		return source;
	}

	public void release(Source source) {
		// ignore
	}
}