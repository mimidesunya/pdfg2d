package jp.cssj.resolver.composite;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;
import jp.cssj.resolver.data.DataSourceResolver;
import jp.cssj.resolver.file.FileSourceResolver;
import jp.cssj.resolver.http.HttpSourceResolver;
import jp.cssj.resolver.url.URLSourceResolver;

/**
 * 複数のSourceResolverを合わせ、複数のスキーマを処理できるようにしたSourceResolverです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: CompositeSourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class CompositeSourceResolver implements SourceResolver {
	private Map<String, SourceResolver> schemeToResolver = new HashMap<String, SourceResolver>();

	private SourceResolver defaultResolver = new URLSourceResolver();

	private String defaultScheme = "file";

	/**
	 * file, http, https, dataスキーマをサポートするSourceResolverを返します。
	 * 
	 * @return SourceResolver
	 */
	public static CompositeSourceResolver createGenericCompositeSourceResolver() {
		CompositeSourceResolver resolver = new CompositeSourceResolver();
		resolver.addSourceResolver("file", new FileSourceResolver());
		try {
			HttpSourceResolver httpSourceResolver = new HttpSourceResolver();
			resolver.addSourceResolver("http", httpSourceResolver);
			resolver.addSourceResolver("https", httpSourceResolver);
		} catch (Throwable e) {
			// ignore
		}
		resolver.addSourceResolver("data", new DataSourceResolver());
		return resolver;
	}

	/**
	 * 指定したスキーマを処理するSourceResolverを追加します。
	 * 
	 * @param scheme
	 *            スキーマ名。
	 * @param resolver
	 *            スキーマを処理するSourceResolver。
	 */
	public void addSourceResolver(String scheme, SourceResolver resolver) {
		this.schemeToResolver.put(scheme.trim().toLowerCase(), resolver);
	}

	/**
	 * 指定したスキーマを処理するSourceResolverを除去します。
	 * 
	 * @param scheme
	 *            スキーマ名。
	 */
	public void removeSourceResolver(String scheme) {
		this.schemeToResolver.remove(scheme.trim().toLowerCase());
	}

	/**
	 * 指定したスキーマを処理するSourceResolverを追加返します。
	 * 
	 * @param scheme
	 *            スキーマ名。
	 * @return スキーマに対応するSourceResolver。
	 */
	public SourceResolver getSourceResolver(String scheme) {
		return (SourceResolver) this.schemeToResolver.get(scheme.trim()
				.toLowerCase());
	}

	/**
	 * サポートするスキーマを全て返します。
	 * 
	 * @return スキーマ名の文字列が入ったコレクション。
	 */
	public Collection<String> getSchemata() {
		return this.schemeToResolver.keySet();
	}

	/**
	 * デフォルトのSourceResolverを設定します。
	 * 
	 * @param defaultResolver
	 *            　デフォルトのSourceResolver。
	 */
	public void setDefaultSourceResolver(SourceResolver defaultResolver) {
		this.defaultResolver = defaultResolver;
	}

	/**
	 * デフォルトのSourceResolverを返します。
	 * 
	 * @return デフォルトのSourceResolver。
	 */
	public SourceResolver getDefaultSourceResolver() {
		return this.defaultResolver;
	}

	/**
	 * デフォルトのスキーマを設定します。
	 * 
	 * @param defaultScheme
	 *            スキーマ名。
	 */
	public void setDefaultScheme(String defaultScheme) {
		this.defaultScheme = defaultScheme;
	}

	/**
	 * デフォルトのスキーマを返します。
	 * 
	 * @return スキーマ名。
	 */
	public String getDefaultSchema() {
		return this.defaultScheme;
	}

	protected SourceResolver getSourceResolver(URI uri) {
		String scheme = uri.getScheme();
		if (scheme == null) {
			scheme = this.defaultScheme;
		}
		SourceResolver resolver = this.getSourceResolver(scheme);
		if (resolver == null) {
			return this.defaultResolver;
		}
		return resolver;
	}

	public Source resolve(URI uri) throws IOException {
		SourceResolver resolver = this.getSourceResolver(uri);
		return resolver.resolve(uri);
	}

	public void release(Source source) {
		SourceResolver resolver = this.getSourceResolver(source.getURI());
		resolver.release(source);
	}

	public String toString() {
		return super.toString() + "[defaultSchema=" + this.defaultScheme
				+ ",defaultResolver=" + this.defaultResolver + ",map="
				+ this.schemeToResolver + "]";
	}
}