package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * 任意の位置の値をセット可能なint値の配列です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class IntList implements Serializable, IntMap {
	private static final long serialVersionUID = 0;

	private static final int[] ZERO = new int[0];
	private int[] array = ZERO;
	private int defaultValue;
	private int length = 0;

	public IntList() {
		this(0);
	}

	public IntList(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void add(int value) {
		set(length, value);
	}

	public void set(int pos, int value) {
		if (length <= pos) {
			length = pos + 1;
			if (array.length <= pos) {
				var newArray = new int[Math.max(length + 10, array.length * 3 / 2)];
				for (int i = array.length; i < newArray.length; ++i) {
					newArray[i] = defaultValue;
				}
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
		}
		array[pos] = value;
	}

	public int[] toArray() {
		this.pack();
		return this.array;
	}

	public int get(int i) {
		if (i >= array.length) {
			return defaultValue;
		}
		return array[i];
	}

	public boolean contains(int key) {
		return this.get(key) != this.defaultValue;
	}

	public int size() {
		return length;
	}

	public void pack() {
		if (length != array.length) {
			var newArray = new int[length];
			System.arraycopy(array, 0, newArray, 0, length);
			array = newArray;
		}
	}

	public boolean isEmpty() {
		return length == 0;
	}

	public IntMapIterator getIterator() {
		this.pack();
		return new ArrayIntMapIterator(this.array);
	}
}
