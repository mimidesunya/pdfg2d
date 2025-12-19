package net.zamasoft.pdfg2d.resolver.protocol.zip;

import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipFile;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * SourceResolver that retrieves data from a ZIP file.
 */
public class ZIPFileSourceResolver implements SourceResolver {
	protected final ZipFile zip;

	public ZIPFileSourceResolver(ZipFile zip) {
		this.zip = zip;
	}

	@Override
	public Source resolve(URI uri) throws IOException {
		return new ZIPFileSource(this.zip, uri);
	}

	@Override
	public void release(Source source) {
		try {
			source.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
