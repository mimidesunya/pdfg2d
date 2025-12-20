package net.zamasoft.pdfg2d.io.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A dynamic list of primitive int values.
 * <p>
 * Unlike {@code ArrayList<Integer>}, this class stores primitive int values
 * directly, avoiding boxing overhead. Values can be set at any position,
 * and the list grows automatically.
 * </p>
 * <p>
 * This class is useful for tracking segment indices in file-based storage.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class IntList implements Serializable {
	private static final long serialVersionUID = 0;

	/** Empty array constant for initial state. */
	private static final int[] ZERO = new int[0];

	/** Internal array storage. */
	private int[] array = ZERO;

	/** Default value for unset positions. */
	private final int defaultValue;

	/** Current logical length of the list. */
	private int length = 0;

	/**
	 * Creates a new IntList with default value of 0.
	 */
	public IntList() {
		this(0);
	}

	/**
	 * Creates a new IntList with the specified default value.
	 * 
	 * @param defaultValue value returned for unset positions.
	 */
	public IntList(final int defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Appends a value to the end of the list.
	 * 
	 * @param value value to append.
	 */
	public void add(final int value) {
		this.set(this.length, value);
	}

	/**
	 * Sets the value at the specified position.
	 * <p>
	 * If the position is beyond the current length, the list expands.
	 * New positions are filled with the default value.
	 * </p>
	 * 
	 * @param pos   position (0-indexed).
	 * @param value value to set.
	 */
	public void set(final int pos, final int value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			// Expand array if needed
			if (this.array.length <= pos) {
				int[] newArray = new int[Math.max(this.length + 10, this.array.length * 3 / 2)];
				if (this.defaultValue != 0) {
					Arrays.fill(newArray, this.array.length, newArray.length, this.defaultValue);
				}
				System.arraycopy(this.array, 0, newArray, 0, this.array.length);
				this.array = newArray;
			}
		}
		this.array[pos] = value;
	}

	/**
	 * Returns the internal array after packing.
	 * <p>
	 * The returned array has exactly the same length as the list.
	 * </p>
	 * 
	 * @return packed array of values.
	 */
	public int[] toArray() {
		this.pack();
		return this.array;
	}

	/**
	 * Gets the value at the specified position.
	 * 
	 * @param i position (0-indexed).
	 * @return value at the position, or default value if out of bounds.
	 */
	public int get(final int i) {
		if (i >= this.array.length) {
			return this.defaultValue;
		}
		return this.array[i];
	}

	/**
	 * Returns the current logical size of the list.
	 * 
	 * @return number of elements.
	 */
	public int size() {
		return this.length;
	}

	/**
	 * Shrinks the internal array to match the logical length.
	 * <p>
	 * Call this to reduce memory usage after all values are set.
	 * </p>
	 */
	public void pack() {
		if (this.length != this.array.length) {
			final int[] newArray = new int[this.length];
			System.arraycopy(this.array, 0, newArray, 0, this.length);
			this.array = newArray;
		}
	}

	/**
	 * Checks if the list is empty.
	 * 
	 * @return true if the list has no elements.
	 */
	public boolean isEmpty() {
		return this.length == 0;
	}
}
