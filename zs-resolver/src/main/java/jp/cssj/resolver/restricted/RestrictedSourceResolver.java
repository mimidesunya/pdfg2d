package jp.cssj.resolver.restricted;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

/**
 * SourcResolverをラップし、ワイルドカードパターンでアクセスを許可・禁止します。 デフォルトでは全てのURIへのアクセスが禁止された状態です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RestrictedSourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class RestrictedSourceResolver implements SourceResolver {
	private SourceResolver enclosedSourceResolver;

	private final List<Pattern> acl = new ArrayList<Pattern>();

	protected static class Pattern {
		public final boolean permit;

		public final int[] pattern;

		public Pattern(boolean permit, int[] pattern) {
			this.permit = permit;
			this.pattern = pattern;
		}
	}

	/**
	 * 与えられたSourceResolverをラップします。
	 * 
	 * @param enclosedSourceResolver
	 *            ラップするSourceResolver。
	 */
	public RestrictedSourceResolver(SourceResolver enclosedSourceResolver) {
		this.enclosedSourceResolver = enclosedSourceResolver;
	}

	public RestrictedSourceResolver() {
		this(null);
	}

	private static char convertHexDigit(char b) {
		if ((b >= '0') && (b <= '9'))
			return (char) (b - '0');
		if ((b >= 'a') && (b <= 'f'))
			return (char) (b - 'a' + 10);
		if ((b >= 'A') && (b <= 'F'))
			return (char) (b - 'A' + 10);
		return 0;
	}

	public static String toKey(URI uri) {
		String key = uri.toString();
		String scheme = uri.getScheme();
		if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
			// *?&=#以外をデコードする
			String exclude = "*?#";
			char[] ch = key.toCharArray();
			int len = ch.length;
			int ix = 0;
			int ox = 0;
			while (ix < len) {
				char b = ch[ix++];
				if (b == '?') {
					exclude = "*&=#";
				} else if (b == '+') {
					b = ' ';
				} else if (b == '%') {
					char c = (char) ((convertHexDigit(ch[ix]) << 4) + convertHexDigit(ch[ix + 1]));
					if (exclude.indexOf(c) == -1) {
						b = c;
						ix += 2;
					} else {
						ch[ix] = Character.toUpperCase(ch[ix]);
						ch[ix + 1] = Character.toUpperCase(ch[ix + 1]);
					}
				}
				ch[ox++] = b;
			}
			key = new String(ch, 0, ox);
		}
		return key;
	}

	/**
	 * 許可するURIパターンを設定します。
	 * 
	 * @param uriPattern
	 *            URIパターン文字列。
	 */
	public void include(URI uriPattern) {
		// *?&=#以外をデコードする
		String key = toKey(uriPattern.normalize());
		this.acl.add(new Pattern(true, WildcardHelper.compilePattern(key)));
	}

	/**
	 * 除外するURIパターンを設定します。
	 * 
	 * @param uriPattern
	 *            URIパターン文字列。
	 */
	public void exclude(URI uriPattern) {
		// *?&=#以外をデコードする
		String key = toKey(uriPattern.normalize());
		this.acl.add(new Pattern(false, WildcardHelper.compilePattern(key)));
	}

	public Source resolve(URI uri) throws IOException, SecurityException {
		return this.resolve(uri, false);
	}

	public Source resolve(URI uri, boolean force) throws IOException,
			SecurityException {
		uri = uri.normalize();
		// *?&#以外をデコードし、*をエンコードする
		String key = toKey(uri);
		key = key.replaceAll("\\*", "%2A");
		if (force) {
			if (this.enclosedSourceResolver == null) {
				throw new FileNotFoundException(key);
			}
			return this.enclosedSourceResolver.resolve(uri);
		}
		for (int i = 0; i < this.acl.size(); ++i) {
			Pattern pattern = (Pattern) this.acl.get(i);
			if (WildcardHelper.match(key, pattern.pattern)) {
				if (pattern.permit) {
					if (this.enclosedSourceResolver == null) {
						throw new FileNotFoundException(key);
					}
					return this.enclosedSourceResolver.resolve(uri);
				}
				throw new SecurityException(key);
			}
		}
		// dataスキームはデフォルトで許可する
		if ("data".equals(uri.getScheme())) {
			return this.enclosedSourceResolver.resolve(uri);
		}
		throw new SecurityException(key);
	}

	public void release(Source source) {
		assert this.enclosedSourceResolver != null;
		this.enclosedSourceResolver.release(source);
	}

	/**
	 * ラップされたSourceResolverを返します。
	 * 
	 * @return ラップされたSourcveResolver。
	 */
	public SourceResolver getEnclosedSourceResolver() {
		return this.enclosedSourceResolver;
	}

	/**
	 * ラップするSourceResolverを設定します。
	 * 
	 * @param enclosedSourceResolver
	 *            ラップするSourceResolver。
	 */
	public void setEnclosedSourceResolver(SourceResolver enclosedSourceResolver) {
		this.enclosedSourceResolver = enclosedSourceResolver;
	}

	/**
	 * ラップするSourceResolverを消去して、制約を全て解除します。
	 */
	public void reset() {
		this.enclosedSourceResolver = null;
		this.acl.clear();
	}
}
