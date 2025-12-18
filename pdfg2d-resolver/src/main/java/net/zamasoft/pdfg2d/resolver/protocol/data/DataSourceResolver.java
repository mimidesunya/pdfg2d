package net.zamasoft.pdfg2d.resolver.protocol.data;

import java.io.IOException;
import java.net.URI;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;

/**
 * SourceResolver that resolves RFC2397 data: scheme.
 */
public class DataSourceResolver implements SourceResolver {
	@Override
	public Source resolve(URI uri) throws IOException {
		return new DataSource(uri);
	}

	@Override
	public void release(Source source) {
		if (source instanceof DataSource dataSource) {
			try {
				dataSource.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
