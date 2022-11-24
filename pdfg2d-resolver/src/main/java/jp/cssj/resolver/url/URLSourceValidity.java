package jp.cssj.resolver.url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import jp.cssj.resolver.SourceValidity;

class URLSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	private final long timestamp;

	private final URL url;

	public URLSourceValidity(long timestamp, URL url) {
		this.timestamp = timestamp;
		this.url = url;
	}

	public Validity getValid() {
		return this.getValid(url);
	}

	public Validity getValid(SourceValidity validity) {
		return this.getValid(((URLSourceValidity) validity).url);
	}

	private Validity getValid(URL url) {
		try {
			URLConnection conn = url.openConnection();
			if (conn.getLastModified() != this.timestamp) {
				return Validity.INVALID;
			}
			return Validity.VALID;
		} catch (IOException e) {
			return Validity.UNKNOWN;
		}
	}

}
