package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * 任意の位置の値をセット可能なint値の配列です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class IntList implements Serializable, IntMap {
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
		this.set(this.length, value);
	}

	public void set(int pos, int value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			if (this.array.length <= pos) {
				int[] array = new int[Math.max(this.length + 10, this.array.length * 3 / 2)];
				for (int i = this.array.length; i < array.length; ++i) {
					array[i] = this.defaultValue;
				}
				System.arraycopy(this.array, 0, array, 0, this.array.length);
				this.array = array;
			}
		}
		this.array[pos] = value;
	}

	public int[] toArray() {
		this.pack();
		return this.array;
	}

	public int get(int i) {
		if (i >= this.array.length) {
			return this.defaultValue;
		}
		return this.array[i];
	}

	public boolean contains(int key) {
		return this.get(key) != this.defaultValue;
	}

	public int size() {
		return this.length;
	}

	public void pack() {
		if (this.length != this.array.length) {
			int[] array = new int[this.length];
			System.arraycopy(this.array, 0, array, 0, this.length);
			this.array = array;
		}
	}

	public boolean isEmpty() {
		return this.length == 0;
	}

	public IntMapIterator getIterator() {
		this.pack();
		return new ArrayIntMapIterator(this.array);
	}
}
