package net.zamasoft.pdfg2d.gc.text.breaking.impl;

/**
 * Represents a set of characters.
 */
@FunctionalInterface
public interface CharacterSet {
	/**
	 * A character set representing all characters.
	 */
	public static final CharacterSet ALL = c -> true;

	/**
	 * An empty character set.
	 */
	public static final CharacterSet NOTHING = c -> false;

	/**
	 * Returns true if the character is contained in this set.
	 * 
	 * @param c the character to check
	 * @return true if contained
	 */
	public boolean contains(char c);
}
