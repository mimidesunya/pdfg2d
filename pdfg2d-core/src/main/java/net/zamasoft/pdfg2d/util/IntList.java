package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * An expandable int array that allows setting values at arbitrary positions.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class IntList implements Serializable, IntMap {
	private static final long serialVersionUID = 0;

	private static final int[] ZERO = new int[0];
	private int[] array = ZERO;
	private final int defaultValue;
	private int length = 0;

	/**
	 * Constructs a new IntList with default value 0.
	 */
	public IntList() {
		this(0);
	}

	/**
	 * Constructs a new IntList with the specified default value.
	 * 
	 * @param defaultValue the default value for new elements
	 */
	public IntList(final int defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Adds a value to the end of the list.
	 * 
	 * @param value the value to add
	 */
	public void add(final int value) {
		this.set(this.length, value);
	}

	@Override
	public void set(final int pos, final int value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			if (this.array.length <= pos) {
				final var newArray = new int[Math.max(this.length + 10, this.array.length * 3 / 2)];
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
	 * Returns the array as a packed int array.
	 * 
	 * @return the array
	 */
	public int[] toArray() {
		this.pack();
		return this.array;
	}

	@Override
	public int get(final int i) {
		if (i >= this.array.length) {
			return this.defaultValue;
		}
		return this.array[i];
	}

	@Override
	public boolean contains(final int key) {
		return this.get(key) != this.defaultValue;
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
			final var newArray = new int[this.length];
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

	@Override
	public IntMapIterator getIterator() {
		this.pack();
		return new ArrayIntMapIterator(this.array);
	}
}
