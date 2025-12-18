package net.zamasoft.pdfg2d.resolver.protocol.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringTokenizer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

import net.zamasoft.pdfg2d.resolver.SourceValidity;
import net.zamasoft.pdfg2d.resolver.util.AbstractSource;
import net.zamasoft.pdfg2d.resolver.util.ValidSourceValidity;

/**
 * A Source that retrieves data from RFC2397 data: scheme.
 */
public class DataSource extends AbstractSource {
	private String mimeType = null;
	private byte[] data = null;
	private String encoding = null;
	private boolean parsed = false;

	public DataSource(URI uri) {
		super(uri);
	}

	private void parse() throws IOException {
		if (!this.parsed) {
			this.parsed = true;
			try {
				String spec = this.uri.getRawSchemeSpecificPart();
				int comma = spec.indexOf(',');
				if (comma != -1) {
					String type = spec.substring(0, comma);
					String dataStr = spec.substring(comma + 1);
					boolean base64 = false;
					for (StringTokenizer st = new StringTokenizer(type, ";"); st.hasMoreElements();) {
						String token = st.nextToken();
						if (this.mimeType == null) {
							if (token.indexOf('/') != -1) {
								this.mimeType = token;
								continue;
							} else {
								this.mimeType = "text/plain";
								this.encoding = "US-ASCII";
							}
						}
						int equal = token.indexOf('=');
						if (equal != -1) {
							String name = token.substring(0, equal);
							if (name.equalsIgnoreCase("charset")) {
								this.encoding = token.substring(equal + 1);
							}
						} else {
							if (token.equalsIgnoreCase("base64")) {
								base64 = true;
							}
						}
					}
					if (base64) {
						byte[] bytes;
						if (dataStr.indexOf('%') != -1) {
							// If base64 is further URL encoded
							// prevent + from being replaced by space
							dataStr = dataStr.replaceAll("\\+", "%2B");
							bytes = dataStr.getBytes(StandardCharsets.ISO_8859_1);
							bytes = URLCodec.decodeUrl(bytes);
						} else {
							bytes = dataStr.getBytes(StandardCharsets.ISO_8859_1);
						}
						this.data = Base64.getMimeDecoder().decode(bytes);
					} else {
						this.data = URLCodec.decodeUrl(dataStr.getBytes(StandardCharsets.ISO_8859_1));
					}
				} else {
					throw new IOException("No data in data: scheme");
				}
			} catch (DecoderException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	public String getMimeType() throws IOException {
		this.parse();
		return this.mimeType;
	}

	@Override
	public String getEncoding() throws IOException {
		this.parse();
		return this.encoding;
	}

	@Override
	public boolean exists() throws IOException {
		this.parse();
		return this.data != null;
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
		this.parse();
		return this.encoding != null;
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		this.parse();
		return new ByteArrayInputStream(this.data);
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
		this.parse();
		return this.data.length;
	}

	@Override
	public SourceValidity getValidity() throws IOException {
		return ValidSourceValidity.SHARED_INSTANCE;
	}
}
