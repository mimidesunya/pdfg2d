package net.zamasoft.pdfg2d.util;

/**
 * An iterator for values mapped to short keys, or short values.
 * <p>
 * Specifically this seems to be an iterator where values are shorts, based on
 * the method signature <code>short value()</code>.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface ShortMapIterator {
	/**
	 * Moves to the next entry.
	 * 
	 * @return true if there is a next entry, false otherwise.
	 */
	boolean next();

	/**
	 * Returns the key of the current entry.
	 * 
	 * @return the key
	 */
	int key();

	/**
	 * Returns the value of the current entry.
	 * 
	 * @return the value
	 */
	short value();
}
