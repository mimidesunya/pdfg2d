package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * An expandable char array that allows setting values at arbitrary positions.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CharList implements Serializable {
	private static final long serialVersionUID = 0;

	private static final char[] ZERO = new char[0];

	private char[] array = ZERO;

	private final char defaultValue;

	private int length = 0;

	/**
	 * Constructs a new CharList with default value 0.
	 */
	public CharList() {
		this((char) 0);
	}

	/**
	 * Constructs a new CharList with the specified default value.
	 * 
	 * @param defaultValue the default value for new elements
	 */
	public CharList(final char defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Sets the value at the specified position.
	 * 
	 * @param pos   the index
	 * @param value the value to set
	 */
	public void set(final int pos, final char value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			if (this.array.length <= pos) {
				final char[] array = new char[Math.max(this.length + 10, this.array.length * 3 / 2)];
				for (int i = this.array.length; i < array.length; ++i) {
					array[i] = this.defaultValue;
				}
				System.arraycopy(this.array, 0, array, 0, this.array.length);
				this.array = array;
			}
		}
		this.array[pos] = value;
	}

	/**
	 * Returns the array as a packed char array.
	 * 
	 * @return the array
	 */
	public char[] toArray() {
		this.pack();
		return this.array;
	}

	/**
	 * Returns the value at the specified position.
	 * 
	 * @param i the index
	 * @return the value
	 */
	public char get(final int i) {
		if (i >= this.array.length) {
			return this.defaultValue;
		}
		return this.array[i];
	}

	/**
	 * Returns the size of the list.
	 * 
	 * @return the size
	 */
	public int size() {
		return this.length;
	}

	/**
	 * Packs the array to fit the current size.
	 */
	public void pack() {
		if (this.length != this.array.length) {
			final char[] array = new char[this.length];
			System.arraycopy(this.array, 0, array, 0, this.length);
			this.array = array;
		}
	}

	/**
	 * Checks if the list is empty.
	 * 
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return this.length == 0;
	}
}
