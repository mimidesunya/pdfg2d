package net.zamasoft.pdfg2d.resolver.protocol.url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import net.zamasoft.pdfg2d.resolver.SourceValidity;

record URLSourceValidity(long timestamp, URL url) implements SourceValidity {
	@Override
	public Validity getValid() {
		return checkValidity(this.url);
	}

	@Override
	public Validity getValid(SourceValidity validity) {
		if (validity instanceof URLSourceValidity other) {
			return checkValidity(other.url);
		}
		return Validity.UNKNOWN;
	}

	private Validity checkValidity(final URL u) {
		try {
			final URLConnection conn = u.openConnection();
			if (conn.getLastModified() != this.timestamp) {
				return Validity.INVALID;
			}
			return Validity.VALID;
		} catch (IOException e) {
			return Validity.UNKNOWN;
		}
	}
}
