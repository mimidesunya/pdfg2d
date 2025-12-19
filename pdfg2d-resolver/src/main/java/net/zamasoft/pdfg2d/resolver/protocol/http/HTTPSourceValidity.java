package net.zamasoft.pdfg2d.resolver.protocol.http;

import net.zamasoft.pdfg2d.resolver.SourceValidity;

record HTTPSourceValidity(long lastModified) implements SourceValidity {
	@Override
	public Validity getValid() {
		return Validity.UNKNOWN;
	}

	@Override
	public Validity getValid(final SourceValidity validity) {
		if (this.lastModified == -1) {
			return Validity.UNKNOWN;
		}
		if (validity instanceof HTTPSourceValidity other) {
			return this.lastModified == other.lastModified ? Validity.VALID : Validity.INVALID;
		}
		return Validity.UNKNOWN;
	}
}
