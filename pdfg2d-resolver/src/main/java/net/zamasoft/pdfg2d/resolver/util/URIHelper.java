package net.zamasoft.pdfg2d.resolver.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * Helper class for URI parsing. Parses more flexibly than Java's URI class.
 */
public final class URIHelper {
	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };
	private static final BitSet SAFE_CHARS = new BitSet(256);
	static {
		// 'lowalpha' rule
		for (int i = 'a'; i <= 'z'; i++) {
			SAFE_CHARS.set(i);
		}
		// 'hialpha' rule
		for (int i = 'A'; i <= 'Z'; i++) {
			SAFE_CHARS.set(i);
		}
		// 'digit' rule
		for (int i = '0'; i <= '9'; i++) {
			SAFE_CHARS.set(i);
		}

		// 'safe' rule
		SAFE_CHARS.set('$');
		SAFE_CHARS.set('-');
		SAFE_CHARS.set('_');
		SAFE_CHARS.set('.');
		SAFE_CHARS.set('+');
		SAFE_CHARS.set('%');

		// 'extra' rule
		SAFE_CHARS.set('!');
		SAFE_CHARS.set('*');
		SAFE_CHARS.set('\'');
		SAFE_CHARS.set('(');
		SAFE_CHARS.set(')');
		SAFE_CHARS.set(',');

		// special characters common to http: file: and ftp: URLs
		SAFE_CHARS.set('/');
		SAFE_CHARS.set(':');
		SAFE_CHARS.set(';');
		SAFE_CHARS.set('@');
		SAFE_CHARS.set('&');
		SAFE_CHARS.set('=');
		SAFE_CHARS.set('?');
		SAFE_CHARS.set('#');
	}
	private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

	public static final URI CURRENT_URI = URI.create(".");

	private URIHelper() {
		// unused
	}

	private static String filter(String encoding, String uri) {
		String trimmedUri = uri.trim();

		Charset cs;
		try {
			cs = Charset.forName(encoding);
		} catch (Exception e) {
			cs = StandardCharsets.UTF_8;
		}

		if (cs.name().startsWith("UTF-")) {
			cs = StandardCharsets.UTF_8;
		}

		CharsetEncoder enc = cs.canEncode() ? cs.newEncoder() : StandardCharsets.UTF_8.newEncoder();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (Writer writer = new OutputStreamWriter(out, enc)) {
			for (int i = 0; i < trimmedUri.length(); ++i) {
				char c = trimmedUri.charAt(i);
				if (!Character.isISOControl(c)) {
					writer.write(c);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] bytes = out.toByteArray();
		StringBuilder buff = new StringBuilder(bytes.length);
		for (byte bByte : bytes) {
			int b = bByte & 0xFF;
			if (SAFE_CHARS.get(b)) {
				buff.append((char) b);
			} else {
				buff.append('%');
				buff.append(HEX[b >> 4]);
				buff.append(HEX[b & 0x0F]);
			}
		}
		return buff.toString();
	}

	private static URI filter(URI uri) throws URISyntaxException {
		String path = uri.getPath();
		if (path == null) {
			return uri;
		}
		while (path.startsWith("/../")) {
			path = path.substring(3);
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
					uri.getFragment());
		}
		String scheme = uri.getScheme();
		if ("file".equals(scheme)) {
			String ssp = uri.getSchemeSpecificPart();
			if (ssp != null && ssp.length() >= 2 && ssp.charAt(0) == '/' && ssp.charAt(1) != '/') {
				uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "//" + path,
						uri.getQuery(), uri.getFragment());
			}
		}
		return uri;
	}

	/**
	 * Creates a URI.
	 * 
	 * @param encoding The encoding for multibyte characters.
	 * @param href     The URI string.
	 * @return The generated URI.
	 * @throws URISyntaxException If the URI is invalid.
	 */
	public static URI create(String encoding, String href) throws URISyntaxException {
		String filteredHref = filter(encoding, href);
		URI uri = new URI(filteredHref);
		return filter(uri);
	}

	/**
	 * Resolves a relative URI.
	 * 
	 * @param encoding The encoding for multibyte characters.
	 * @param baseURI  The base URI.
	 * @param href     The relative URI string.
	 * @return The resolved URI.
	 * @throws URISyntaxException If the URI is invalid.
	 */
	public static URI resolve(final String encoding, final URI baseURI, String href) throws URISyntaxException {
		final String scheme = baseURI.getScheme();
		String currentHref = href;
		if (("http".equals(scheme) || "https".equals(scheme)) && !currentHref.startsWith("file:")) {
			currentHref = filter(encoding, currentHref);
		}
		if (isWindowsDrive(currentHref)) {
			currentHref = "file:///" + currentHref;
		}
		URI hrefURI = new URI(currentHref);
		if ("jar".equals(scheme) && !hrefURI.isAbsolute()) {
			URI uri = new URI(baseURI.getSchemeSpecificPart()).resolve(hrefURI);
			return new URI("jar:" + uri);
		}
		URI uri = baseURI.resolve(hrefURI);
		if (uri.getScheme() == null && scheme != null) {
			uri = new URI(scheme, uri.getSchemeSpecificPart(), uri.getFragment());
		}
		if ((hasWindowsDrive(baseURI) && !hrefURI.isAbsolute()) || hasWindowsDrive(hrefURI)) {
			// Workaround for Windows file path issue where relative resolution removes
			// colon
			String path = uri.getSchemeSpecificPart();
			int driveLetterIndex = 0;
			while (driveLetterIndex < path.length() && path.charAt(driveLetterIndex) == '/') {
				driveLetterIndex++;
			}

			if (driveLetterIndex < path.length()) {
				int colonIndex = driveLetterIndex + 1;
				if (colonIndex >= path.length() || path.charAt(colonIndex) != ':') {
					// Colon is missing
					String newPath = path.substring(0, colonIndex) + ":"
							+ (colonIndex < path.length() ? path.substring(colonIndex) : "");
					uri = new URI(uri.getScheme(), newPath, uri.getFragment());
				}
			}
		}

		uri = filter(uri);
		return uri;
	}

	public static URI resolve(String encoding, String baseURI, String href) throws URISyntaxException {
		return resolve(encoding, create(encoding, baseURI), href);
	}

	private static boolean hasWindowsDrive(URI uri) {
		if (!WINDOWS) {
			return false;
		}
		if (!"file".equalsIgnoreCase(uri.getScheme())) {
			return false;
		}
		String path = uri.getSchemeSpecificPart();
		int start = 0;
		while (start < path.length() && path.charAt(start) == '/') {
			start++;
		}
		return start > 0 && path.length() >= start + 2 && path.charAt(start + 1) == ':'
				&& ((path.charAt(start) >= 'A' && path.charAt(start) <= 'Z')
						|| (path.charAt(start) >= 'a' && path.charAt(start) <= 'z'));
	}

	private static boolean isWindowsDrive(String href) {
		if (!WINDOWS) {
			return false;
		}
		return href.length() >= 2 && ((href.charAt(0) >= 'A' && href.charAt(0) <= 'Z')
				|| (href.charAt(0) >= 'a' && href.charAt(0) <= 'z')) && href.charAt(1) == ':';
	}

	public static String decode(String uri) {
		// Do not convert + to space
		String safeUri = uri.replace("+", "%2B");
		try {
			return URLDecoder.decode(safeUri, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
