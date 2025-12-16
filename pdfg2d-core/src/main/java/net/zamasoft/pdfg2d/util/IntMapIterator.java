package net.zamasoft.pdfg2d.util;

/**
 * An iterator for {@link IntMap}.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface IntMapIterator {
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
	int value();
}
