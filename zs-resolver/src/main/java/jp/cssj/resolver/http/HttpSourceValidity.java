package jp.cssj.resolver.http;

import jp.cssj.resolver.SourceValidity;

class HttpSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	private final long lastModified;

	public HttpSourceValidity(final long lastModified) {
		this.lastModified = lastModified;
	}

	public int getValid() {
		return SourceValidity.UNKNOWN;
	}

	public int getValid(final SourceValidity validity) {
		if (this.lastModified == -1) {
			return SourceValidity.UNKNOWN;
		}
		return this.lastModified == ((HttpSourceValidity) validity).lastModified ? SourceValidity.VALID
				: SourceValidity.INVALID;
	}
}
