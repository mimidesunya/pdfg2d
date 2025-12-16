package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * An expandable short array that allows setting values at arbitrary positions.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class ShortList implements Serializable {
	private static final long serialVersionUID = 0;

	private static final short[] ZERO = new short[0];
	private short[] array = ZERO;
	private final short defaultValue;
	private int length = 0;

	/**
	 * Constructs a new ShortList with default value 0.
	 */
	public ShortList() {
		this((short) 0);
	}

	/**
	 * Constructs a new ShortList with the specified default value.
	 * 
	 * @param defaultValue the default value for new elements
	 */
	public ShortList(final short defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Sets the value at the specified position.
	 * 
	 * @param pos   the index
	 * @param value the value to set
	 */
	public void set(final int pos, final short value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			if (this.array.length <= pos) {
				final var newArray = new short[Math.max(this.length + 10, this.array.length * 3 / 2)];
				for (int i = this.array.length; i < newArray.length; ++i) {
					newArray[i] = this.defaultValue;
				}
				System.arraycopy(this.array, 0, newArray, 0, this.array.length);
				this.array = newArray;
			}
		}
		this.array[pos] = value;
	}

	/**
	 * Returns the array as a packed short array.
	 * 
	 * @return the array
	 */
	public short[] toArray() {
		this.pack();
		return this.array;
	}

	/**
	 * Returns the value at the specified position.
	 * 
	 * @param i the index
	 * @return the value
	 */
	public short get(final int i) {
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
			final var newArray = new short[this.length];
			System.arraycopy(this.array, 0, newArray, 0, this.length);
			this.array = newArray;
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
