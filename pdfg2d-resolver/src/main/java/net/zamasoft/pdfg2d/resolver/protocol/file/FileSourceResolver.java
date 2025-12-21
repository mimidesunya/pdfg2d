package net.zamasoft.pdfg2d.resolver.protocol.file;

import java.io.IOException;
import java.net.URI;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * SourceResolver that resolves file scheme.
 */
public class FileSourceResolver implements SourceResolver {
	@Override
	public Source resolve(final URI uri) throws IOException {
		return new FileSource(uri);
	}

	@Override
	public void release(final Source source) {
		// ignore
	}
}
