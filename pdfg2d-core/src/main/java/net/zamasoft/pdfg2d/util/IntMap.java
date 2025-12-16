package net.zamasoft.pdfg2d.util;

/**
 * A map capable of storing int values.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface IntMap {
	/**
	 * Sets the value for the specified key.
	 * 
	 * @param key   the key
	 * @param value the value
	 */
	void set(int key, int value);

	/**
	 * Returns the value associated with the specified key.
	 * 
	 * @param key the key
	 * @return the value
	 */
	int get(int key);

	/**
	 * Returns true if this map contains a mapping for the specified key.
	 * 
	 * @param key the key
	 * @return true if mapping exists
	 */
	boolean contains(int key);

	/**
	 * Returns an iterator for this map.
	 * 
	 * @return an iterator
	 */
	IntMapIterator getIterator();
}
