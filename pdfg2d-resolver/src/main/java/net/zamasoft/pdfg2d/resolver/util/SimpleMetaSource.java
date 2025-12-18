package net.zamasoft.pdfg2d.resolver.util;

import java.io.IOException;
import java.net.URI;
import net.zamasoft.pdfg2d.resolver.MetaSource;
import net.zamasoft.pdfg2d.resolver.Source;

/**
 * A simple immutable implementation of MetaSource.
 */
public record SimpleMetaSource(URI uri, String mimeType, String encoding, long length) implements MetaSource {
    private static final URI CURRENT_URI = URI.create(".");

    public SimpleMetaSource {
        if (uri == null) {
            uri = CURRENT_URI;
        }
    }

    public SimpleMetaSource() {
        this(null, null, null, -1L);
    }

    public SimpleMetaSource(URI uri) {
        this(uri, null, null, -1L);
    }

    public SimpleMetaSource(URI uri, String mimeType) {
        this(uri, mimeType, null, -1L);
    }

    public SimpleMetaSource(URI uri, String mimeType, String encoding) {
        this(uri, mimeType, encoding, -1L);
    }

    public SimpleMetaSource(Source source) throws IOException {
        this(source.getURI(), source.getMimeType(), source.getEncoding(), source.getLength());
    }

    @Override
    public URI getURI() {
        return uri();
    }

    @Override
    public String getMimeType() {
        return mimeType();
    }

    @Override
    public String getEncoding() {
        return encoding();
    }

    @Override
    public long getLength() {
        return length();
    }
}

