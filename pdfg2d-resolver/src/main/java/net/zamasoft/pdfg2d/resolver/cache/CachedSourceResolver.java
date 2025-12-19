package net.zamasoft.pdfg2d.resolver.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import net.zamasoft.pdfg2d.resolver.SourceMetadata;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * Caches data as temporary files to make them accessible.
 */
public class CachedSourceResolver implements SourceResolver {
	protected record CachedSourceInfo(URI uri, String mimeType, String encoding, File file) {
	}

	private final Map<String, CachedSourceInfo> uriToSource = new HashMap<>();
	private final File tmpDir;

	public CachedSourceResolver(final File tmpDir) {
		this.tmpDir = tmpDir;
	}

	public CachedSourceResolver() {
		this(null);
	}

	private static char convertHexDigit(final char b) {
		if ((b >= '0') && (b <= '9')) {
			return (char) (b - '0');
		}
		if ((b >= 'a') && (b <= 'f')) {
			return (char) (b - 'a' + 10);
		}
		if ((b >= 'A') && (b <= 'F')) {
			return (char) (b - 'A' + 10);
		}
		return 0;
	}

	public static String toKey(final URI uri) {
		var key = uri.toString();
		final var scheme = uri.getScheme();
		if (scheme == null) {
			return key;
		}
		if (scheme.equals("http") || scheme.equals("https")) {
			// Decode except ?&=#
			var exclude = "?#";
			final var ch = key.toCharArray();
			final int len = ch.length;
			var ix = 0;
			var ox = 0;
			while (ix < len) {
				var b = ch[ix++];
				if (b == '?') {
					exclude = "&=#";
				} else if (b == '+') {
					b = ' ';
				} else if (b == '%') {
					if (ix + 1 < len) {
						final var c = (char) ((convertHexDigit(ch[ix]) << 4) + convertHexDigit(ch[ix + 1]));
						if (exclude.indexOf(c) == -1) {
							b = c;
							ix += 2;
						} else {
							// Normalize uppercase
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

	/**
	 * Caches data with the given attributes as a file.
	 * 
	 * @param sourceMetadata Attributes of the data.
	 * @return The file where data is stored.
	 * @throws IOException If I/O error occurs.
	 */
	public File putFile(final SourceMetadata sourceMetadata) throws IOException {
		final var uri = sourceMetadata.getURI().normalize();
		final var key = toKey(uri);
		var info = this.uriToSource.get(key);
		if (info != null) {
			if (!info.file.delete()) {
				// log warning?
			}
		}

		final var mimeType = sourceMetadata.getMimeType();
		final var encoding = sourceMetadata.getEncoding();
		final var file = File.createTempFile("cssj-cache-", ".dat", this.tmpDir);
		file.deleteOnExit();
		info = new CachedSourceInfo(uri, mimeType, encoding, file);
		this.uriToSource.put(key, info);
		return file;
	}

	/**
	 * Caches another source.
	 * 
	 * @param source The source to cache.
	 * @throws IOException If I/O error occurs.
	 */
	public void putSource(final Source source) throws IOException {
		final var file = this.putFile(source);
		try (final var in = source.getInputStream(); final var out = new FileOutputStream(file)) {
			IOUtils.copy(in, out);
		}
	}

	@Override
	public Source resolve(URI uri) throws IOException {
		uri = uri.normalize();
		String key = toKey(uri);
		CachedSourceInfo info = this.uriToSource.get(key);
		if (info != null) {
			return new CachedSource(info.uri, info.mimeType, info.encoding, info.file);
		}
		throw new FileNotFoundException(uri.toString());
	}

	@Override
	public void release(Source source) {
		if (source instanceof CachedSource cachedSource) {
			cachedSource.close();
		}
	}

	/**
	 * Clears the cache.
	 */
	public void reset() {
		for (CachedSourceInfo info : this.uriToSource.values()) {
			if (!info.file.delete()) {
				// ignore
			}
		}
		this.uriToSource.clear();
	}

	public void dispose() {
		this.reset();
	}
}
