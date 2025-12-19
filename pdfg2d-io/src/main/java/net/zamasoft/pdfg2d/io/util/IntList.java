package net.zamasoft.pdfg2d.io.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An array of int values that can be set at any position.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class IntList implements Serializable {
	private static final long serialVersionUID = 0;

	private static final int[] ZERO = new int[0];

	private int[] array = ZERO;

	private final int defaultValue;

	private int length = 0;

	public IntList() {
		this(0);
	}

	public IntList(final int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void add(final int value) {
		this.set(this.length, value);
	}

	public void set(final int pos, final int value) {
		if (this.length <= pos) {
			this.length = pos + 1;
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

	public int[] toArray() {
		this.pack();
		return this.array;
	}

	public int get(final int i) {
		if (i >= this.array.length) {
			return this.defaultValue;
		}
		return this.array[i];
	}

	public int size() {
		return this.length;
	}

	public void pack() {
		if (this.length != this.array.length) {
			final int[] newArray = new int[this.length];
			System.arraycopy(this.array, 0, newArray, 0, this.length);
			this.array = newArray;
		}
	}

	public boolean isEmpty() {
		return this.length == 0;
	}
}
