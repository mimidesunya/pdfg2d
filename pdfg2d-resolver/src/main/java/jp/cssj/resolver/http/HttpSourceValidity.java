package jp.cssj.resolver.http;

import jp.cssj.resolver.SourceValidity;

class HttpSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	private final long lastModified;

	public HttpSourceValidity(final long lastModified) {
		this.lastModified = lastModified;
	}

	public Validity getValid() {
		return Validity.UNKNOWN;
	}

	public Validity getValid(final SourceValidity validity) {
		if (this.lastModified == -1) {
			return Validity.UNKNOWN;
		}
		return this.lastModified == ((HttpSourceValidity) validity).lastModified ? Validity.VALID
				: Validity.INVALID;
	}
}
