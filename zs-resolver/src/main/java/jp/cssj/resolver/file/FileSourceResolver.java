package jp.cssj.resolver.file;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * fileスキーマを解決するSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FileSourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
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