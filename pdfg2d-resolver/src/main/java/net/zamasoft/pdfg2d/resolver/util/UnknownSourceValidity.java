package net.zamasoft.pdfg2d.resolver.util;

import net.zamasoft.pdfg2d.resolver.SourceValidity;

/**
 * SourceValidity that always returns UNKNOWN.
 */
public class UnknownSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	public static final UnknownSourceValidity SHARED_INSTANCE = new UnknownSourceValidity();

	private UnknownSourceValidity() {
	}

	@Override
	public Validity getValid() {
		return Validity.UNKNOWN;
	}

	@Override
	public Validity getValid(SourceValidity validity) {
		return Validity.UNKNOWN;
	}
}

