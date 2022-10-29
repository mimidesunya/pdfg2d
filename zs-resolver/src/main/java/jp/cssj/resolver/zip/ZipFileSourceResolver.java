package jp.cssj.resolver.zip;

import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipFile;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * ZIPファイルをデータ源とするSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ZipFileSourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class ZipFileSourceResolver implements SourceResolver {
	protected final ZipFile zip;

	public ZipFileSourceResolver(ZipFile zip) {
		this.zip = zip;
	}

	public Source resolve(URI uri) throws IOException {
		ZipFileSource source = new ZipFileSource(this.zip, uri);
		return source;
	}

	public void release(Source source) {
		((ZipFileSource) source).close();
	}
}