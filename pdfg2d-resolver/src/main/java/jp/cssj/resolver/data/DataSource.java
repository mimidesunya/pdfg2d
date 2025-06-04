package jp.cssj.resolver.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.StringTokenizer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;

import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.helpers.AbstractSource;
import jp.cssj.resolver.helpers.ValidSourceValidity;

/**
 * RFC2397,data:スキーマからデータを取得するSourceです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
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
					String data = spec.substring(comma + 1);
					boolean base64 = false;
					for (StringTokenizer st = new StringTokenizer(type, ";"); st
							.hasMoreElements();) {
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
						if (data.indexOf('%') != -1) {
							// BASE64がさらにURLエンコードされている場合
							// +記号がスペースに変えられないようにする
							data = data.replaceAll("\\+", "%2B");
							bytes = data.getBytes("iso-8859-1");
							bytes = URLCodec.decodeUrl(bytes);
						} else {
							bytes = data.getBytes("iso-8859-1");
						}
						this.data = Base64.decodeBase64(bytes);
					} else {
						this.data = URLCodec.decodeUrl(data
								.getBytes("iso-8859-1"));
					}
				} else {
					throw new IOException("data:スキーマのデータがありません");
				}
			} catch (DecoderException e) {
				IOException ioe = new IOException(e.getMessage());
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	public String getMimeType() throws IOException {
		this.parse();
		return this.mimeType;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean exists() throws IOException {
		this.parse();
		return this.data != null;
	}

	public boolean isFile() throws IOException {
		return false;
	}

	public boolean isInputStream() throws IOException {
		return true;
	}

	public boolean isReader() throws IOException {
		this.parse();
		return this.encoding != null;
	}

	public synchronized InputStream getInputStream() throws IOException {
		this.parse();
		return new ByteArrayInputStream(this.data);
	}

	public Reader getReader() throws IOException {
		if (!this.isReader()) {
			throw new UnsupportedOperationException();
		}
		return new InputStreamReader(this.getInputStream(), this.encoding);
	}

	public File getFile() {
		throw new UnsupportedOperationException();
	}

	public long getLength() throws IOException {
		this.parse();
		return this.data.length;
	}

	public SourceValidity getValidity() throws IOException {
		return ValidSourceValidity.SHARED_INSTANCE;
	}
}