package net.zamasoft.pdfg2d.pdf;

/**
 * Interface for PDF cross-reference table operations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface XRef {
	/**
	 * Returns the next object reference.
	 * 
	 * @return the object reference
	 */
	ObjectRef nextObjectRef();

	/**
	 * Sets an attribute.
	 * 
	 * @param key   the attribute key
	 * @param value the attribute value
	 */
	void setAttribute(String key, Object value);

	/**
	 * Returns an attribute.
	 * 
	 * @param key the attribute key
	 * @return the attribute value
	 */
	Object getAttribute(String key);
}
