package net.zamasoft.pdfg2d.resolver.protocol.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Objects;
import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;
import net.zamasoft.pdfg2d.resolver.util.URIHelper;

/**
 * A Source that retrieves data from a file.
 */
public class FileSource extends AbstractSource {
	private final File file;
	private final String encoding;
	private String mimeType = null;

	public FileSource(final File file, final URI uri, final String mimeType, final String encoding) {
		super(uri);
		this.file = Objects.requireNonNull(file);
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public FileSource(final URI uri) throws IOException {
		super(uri);
		String path = uri.getSchemeSpecificPart();
		path = URIHelper.decode(path);
		this.file = new File(path);
		this.mimeType = null;
		this.encoding = null;
	}

	public FileSource(final File file, final String mimeType, final String encoding) {
		this(file, file.toURI(), mimeType, encoding);
	}

	public FileSource(final File file, final String mimeType) {
		this(file, mimeType, null);
	}

	public FileSource(final File file) {
		this(file, null);
	}

	@Override
	public String getMimeType() throws IOException {
		if (this.mimeType == null) {
			final String filename = this.file.getName();
			final int dot = filename.lastIndexOf('.');
			if (dot != -1) {
				final String suffix = filename.substring(dot).toLowerCase();
				this.mimeType = switch (suffix) {
					case ".html", ".htm" -> "text/html";
					case ".xml", ".xhtml", ".xht" -> "text/xml";
					default -> null;
				};
			}
		}
		return this.mimeType;
	}

	@Override
	public String getEncoding() {
		return this.encoding;
	}

	@Override
	public boolean exists() throws IOException {
		return this.file.exists();
	}

	@Override
	public boolean isFile() throws IOException {
		return true;
	}

	@Override
	public boolean isInputStream() throws IOException {
		return true;
	}

	@Override
	public boolean isReader() throws IOException {
		return this.encoding != null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this.file);
	}

	@Override
	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public long getLength() throws IOException {
		return this.file.length();
	}

	@Override
	public SourceValidity getValidity() throws IOException {
		final long timestamp = this.file.lastModified();
		return new FileSourceValidity(timestamp, this.file);
	}
}
