package jp.cssj.resolver.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.cssj.resolver.MetaSource;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;

import org.apache.commons.io.IOUtils;

/**
 * 一時ファイルとしてデータをキャッシュし、アクセスできるようにします。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CachedSourceResolver implements SourceResolver {
	protected static class CachedSourceInfo {
		public final URI uri;
		public final String mimeType, encoding;
		public final File file;

		public CachedSourceInfo(URI uri, String mimeType, String encoding,
				File file) {
			this.uri = uri;
			this.mimeType = mimeType;
			this.encoding = encoding;
			this.file = file;
		}
	}

	private final Map<String, CachedSourceInfo> uriToSource = new HashMap<String, CachedSourceInfo>();
	private final File tmpDir;

	public CachedSourceResolver(File tmpDir) {
		this.tmpDir = tmpDir;
	}

	public CachedSourceResolver() {
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
		if (scheme == null) {
			return key;
		}
		if (scheme.equals("http") || scheme.equals("https")) {
			// ?&=#以外をデコードする
			String exclude = "?#";
			char[] ch = key.toCharArray();
			int len = ch.length;
			int ix = 0;
			int ox = 0;
			while (ix < len) {
				char b = ch[ix++];
				if (b == '?') {
					exclude = "&=#";
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
	 * 与えられた属性を持つデータをファイルとしてキャッシュします。 アプリケーションは戻り値のファイルにデータを書き込んでください。
	 * 
	 * @param metaSource
	 *            データの属性。
	 * @return データが格納されるファイル。
	 * @throws IOException
	 */
	public File putFile(MetaSource metaSource) throws IOException {
		URI uri = metaSource.getURI().normalize();
		String key = toKey(uri);
		CachedSourceInfo info = (CachedSourceInfo) this.uriToSource.get(key);
		if (info != null) {
			info.file.delete();
		}

		String mimeType = metaSource.getMimeType();
		String encoding = metaSource.getEncoding();
		File file = File.createTempFile("cssj-cache-", ".dat", this.tmpDir);
		file.deleteOnExit();
		info = new CachedSourceInfo(uri, mimeType, encoding, file);
		this.uriToSource.put(key, info);
		return file;
	}

	/**
	 * 他のソースをキャッシュします。
	 * 
	 * @param source
	 *            データをキャッシュするソース。
	 * @throws IOException
	 */
	public void putSource(Source source) throws IOException {
		File file = this.putFile(source);
		try(InputStream in = source.getInputStream();OutputStream out = new FileOutputStream(file)){
				IOUtils.copy(in, out);
			} 
	}

	public Source resolve(URI uri) throws IOException, SecurityException {
		uri = uri.normalize();
		String key = toKey(uri);
		CachedSourceInfo info = (CachedSourceInfo) this.uriToSource.get(key);
		if (info != null) {
			Source source = new CachedSource(info.uri, info.mimeType,
					info.encoding, info.file);
			return source;
		}
		throw new FileNotFoundException(uri.toString());
	}

	public void release(Source source) {
		((CachedSource) source).close();
		return;
	}

	/**
	 * キャッシュをクリアします。
	 */
	public void reset() {
		for (Iterator<CachedSourceInfo> i = this.uriToSource.values()
				.iterator(); i.hasNext();) {
			CachedSourceInfo info = i.next();
			info.file.delete();
		}
		this.uriToSource.clear();
	}

	public void dispose() {
		this.reset();
	}
}
