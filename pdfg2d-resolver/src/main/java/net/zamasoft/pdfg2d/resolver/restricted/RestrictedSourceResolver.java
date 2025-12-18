package net.zamasoft.pdfg2d.resolver.restricted;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * Wraps a SourceResolver to restrict access using wildcard patterns.
 * Access is denied by default.
 */
public class RestrictedSourceResolver implements SourceResolver {
	private SourceResolver enclosedSourceResolver;
	private final List<Pattern> acl = new ArrayList<>();

	protected record Pattern(boolean permit, int[] pattern) {
	}

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
					if (ix + 1 < len) {
						char c = (char) ((convertHexDigit(ch[ix]) << 4) + convertHexDigit(ch[ix + 1]));
						if (exclude.indexOf(c) == -1) {
							b = c;
							ix += 2;
						} else {
							ch[ix] = Character.toUpperCase(ch[ix]);
							ch[ix + 1] = Character.toUpperCase(ch[ix + 1]);
						}
					}
				}
				ch[ox++] = b;
			}
			key = new String(ch, 0, ox);
		}
		return key;
	}

	public void include(URI uriPattern) {
		String key = toKey(uriPattern.normalize());
		this.acl.add(new Pattern(true, WildcardHelper.compilePattern(key)));
	}

	public void exclude(URI uriPattern) {
		String key = toKey(uriPattern.normalize());
		this.acl.add(new Pattern(false, WildcardHelper.compilePattern(key)));
	}

	@Override
	public Source resolve(URI uri) throws IOException {
		return this.resolve(uri, false);
	}

	public Source resolve(URI uri, boolean force) throws IOException {
		uri = uri.normalize();
		String key = toKey(uri);
		key = key.replaceAll("\\*", "%2A");
		if (force) {
			if (this.enclosedSourceResolver == null) {
				throw new FileNotFoundException(key);
			}
			return this.enclosedSourceResolver.resolve(uri);
		}
		for (Pattern pattern : this.acl) {
			if (WildcardHelper.match(key, pattern.pattern)) {
				if (pattern.permit) {
					if (this.enclosedSourceResolver == null) {
						throw new FileNotFoundException(key);
					}
					return this.enclosedSourceResolver.resolve(uri);
				}
				throw new SecurityException("Access denied: " + key);
			}
		}
		if ("data".equals(uri.getScheme())) {
			if (this.enclosedSourceResolver == null) {
				throw new FileNotFoundException(key);
			}
			return this.enclosedSourceResolver.resolve(uri);
		}
		throw new SecurityException("Access denied: " + key);
	}

	@Override
	public void release(Source source) {
		if (this.enclosedSourceResolver != null) {
			this.enclosedSourceResolver.release(source);
		}
	}

	public SourceResolver getEnclosedSourceResolver() {
		return this.enclosedSourceResolver;
	}

	public void setEnclosedSourceResolver(SourceResolver enclosedSourceResolver) {
		this.enclosedSourceResolver = enclosedSourceResolver;
	}

	public void reset() {
		this.enclosedSourceResolver = null;
		this.acl.clear();
	}
}

