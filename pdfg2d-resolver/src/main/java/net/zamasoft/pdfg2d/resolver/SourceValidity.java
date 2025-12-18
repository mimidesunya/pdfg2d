package net.zamasoft.pdfg2d.resolver;

import java.io.Serializable;

/**
 * Information about data validity/updates.
 */
public interface SourceValidity extends Serializable {
	enum Validity {
		/** Data has been updated. */
		INVALID,
		/** Cannot determine if data has been updated. */
		UNKNOWN,
		/** Data has not been updated. */
		VALID;
	}

	/**
	 * Returns whether the data has been updated since this SourceValidity was
	 * obtained.
	 * 
	 * @return INVALID, UNKNOWN, or VALID.
	 */
	Validity getValid();

	/**
	 * Verifies if this SourceValidity differs from the given SourceValidity.
	 * 
	 * @param validity Another SourceValidity.
	 * @return INVALID, UNKNOWN, or VALID.
	 */
	Validity getValid(SourceValidity validity);
}

