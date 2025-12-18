package net.zamasoft.pdfg2d.resolver.composite;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.resolver.SourceResolver;
import net.zamasoft.pdfg2d.resolver.protocol.data.DataSourceResolver;
import net.zamasoft.pdfg2d.resolver.protocol.file.FileSourceResolver;
import net.zamasoft.pdfg2d.resolver.protocol.http.HttpSourceResolver;
import net.zamasoft.pdfg2d.resolver.protocol.url.URLSourceResolver;

/**
 * A SourceResolver that aggregates multiple SourceResolvers to handle multiple
 * schemes.
 */
public class CompositeSourceResolver implements SourceResolver {
	private final Map<String, SourceResolver> schemeToResolver = new HashMap<>();
	private SourceResolver defaultResolver = new URLSourceResolver();
	private String defaultScheme = "file";

	/**
	 * Creates a SourceResolver that supports file, http, https, and data schemes.
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

	public void addSourceResolver(String scheme, SourceResolver resolver) {
		this.schemeToResolver.put(scheme.trim().toLowerCase(), resolver);
	}

	public void removeSourceResolver(String scheme) {
		this.schemeToResolver.remove(scheme.trim().toLowerCase());
	}

	public SourceResolver getSourceResolver(String scheme) {
		return this.schemeToResolver.get(scheme.trim().toLowerCase());
	}

	public Collection<String> getSchemata() {
		return this.schemeToResolver.keySet();
	}

	public void setDefaultSourceResolver(SourceResolver defaultResolver) {
		this.defaultResolver = defaultResolver;
	}

	public SourceResolver getDefaultSourceResolver() {
		return this.defaultResolver;
	}

	public void setDefaultScheme(String defaultScheme) {
		this.defaultScheme = defaultScheme;
	}

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

	@Override
	public Source resolve(URI uri) throws IOException {
		SourceResolver resolver = this.getSourceResolver(uri);
		return resolver.resolve(uri);
	}

	@Override
	public void release(Source source) {
		SourceResolver resolver = this.getSourceResolver(source.getURI());
		if (resolver != null) {
			resolver.release(source);
		}
	}

	@Override
	public String toString() {
		return super.toString() + "[defaultSchema=" + this.defaultScheme
				+ ",defaultResolver=" + this.defaultResolver + ",map="
				+ this.schemeToResolver + "]";
	}
}
