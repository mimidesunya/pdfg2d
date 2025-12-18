package net.zamasoft.pdfg2d.resolver.util;

import net.zamasoft.pdfg2d.resolver.SourceValidity;

/**
 * SourceValidity that always returns VALID.
 */
public class ValidSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	public static final ValidSourceValidity SHARED_INSTANCE = new ValidSourceValidity();

	private ValidSourceValidity() {
	}

	@Override
	public Validity getValid() {
		return Validity.VALID;
	}

	@Override
	public Validity getValid(SourceValidity validity) {
		return Validity.VALID;
	}
}

