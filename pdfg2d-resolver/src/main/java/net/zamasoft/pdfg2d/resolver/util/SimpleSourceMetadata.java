package net.zamasoft.pdfg2d.resolver.util;

import java.net.URI;

import net.zamasoft.pdfg2d.resolver.SourceMetadata;

/**
 * Simple implementation of SourceMetadata.
 * 
 * @param uri      The URI of the data.
 * @param mimeType The MIME type of the data.
 * @param encoding The character encoding of the data.
 * @param length   The size of the data in bytes.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record SimpleSourceMetadata(URI uri, String mimeType, String encoding, long length) implements SourceMetadata {
    public SimpleSourceMetadata(final URI uri) {
        this(uri, null, null, -1);
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public long getLength() {
        return this.length;
    }
}
