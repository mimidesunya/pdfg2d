package net.zamasoft.pdfg2d.resolver.protocol.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;
import net.zamasoft.pdfg2d.resolver.util.URIHelper;

/**
 * A Source that retrieves data from a ZIP file.
 */
public class ZIPFileSource extends AbstractSource {
	private static final Logger LOG = Logger.getLogger(ZIPFileSource.class.getName());

	private final ZipFile zip;
	private final ZipEntry entry;
	private final String encoding;
	private String mimeType = null;

	public ZIPFileSource(final ZipFile zip, final String path, final URI uri, final String mimeType,
			final String encoding) {
		super(uri);
		this.zip = Objects.requireNonNull(zip, "ZipFile must not be null");

		String entryPath = path;
		if (entryPath == null) {
			entryPath = uri.getSchemeSpecificPart();
			try {
				entryPath = URIHelper.decode(entryPath);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Cannot decode URI.", e);
			}
		}

		this.entry = zip.getEntry(entryPath);
		this.mimeType = mimeType;
		this.encoding = encoding;
	}

	public ZIPFileSource(final ZipFile zip, final URI uri, final String mimeType, final String encoding) {
		this(zip, null, uri, mimeType, encoding);
	}

	public ZIPFileSource(final ZipFile zip, final URI uri, final String mimeType) {
		this(zip, uri, mimeType, null);
	}

	public ZIPFileSource(final ZipFile zip, final URI uri) throws IOException {
		this(zip, uri, null);
	}

	public ZIPFileSource(final ZipFile zip, final String path, final URI uri, final String mimeType) {
		this(zip, path, uri, mimeType, null);
	}

	public ZIPFileSource(final ZipFile zip, final String path, final URI uri) {
		this(zip, path, uri, null, null);
	}

	@Override
	public String getMimeType() throws IOException {
		if (this.mimeType == null && this.entry != null) {
			final String name = this.entry.getName();
			final int dot = name.lastIndexOf('.');
			if (dot != -1) {
				final String suffix = name.substring(dot).toLowerCase();
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
		return this.entry != null;
	}

	@Override
	public boolean isFile() throws IOException {
		return false;
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
		if (this.entry == null) {
			throw new FileNotFoundException(this.uri.toString());
		}
		return this.zip.getInputStream(this.entry);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLength() throws IOException {
		if (this.exists()) {
			final long size = this.entry.getSize();
			return size != -1 ? size : -1;
		}
		return 0;
	}

	@Override
	public SourceValidity getValidity() throws IOException {
		final File file = new File(this.zip.getName());
		final long timestamp = file.lastModified();
		return new ZIPFileSourceValidity(timestamp, file);
	}
}
