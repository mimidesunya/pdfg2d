package net.zamasoft.pdfg2d.resolver.protocol.url;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * Resolves resources using java.net.URL.
 */
public class URLSourceResolver implements SourceResolver {
	@Override
	public Source resolve(final URI uri) throws IOException {
		try {
			final URL url = uri.toURL();
			try {
				return new URLSource(url, null, null);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void release(final Source source) {
		if (source instanceof final URLSource urlSource) {
			urlSource.close();
		}
	}
}
