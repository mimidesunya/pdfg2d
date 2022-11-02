package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * 任意の位置の値をセット可能なshort値の配列です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ShortList implements Serializable {
	private static final long serialVersionUID = 0;

	private static final short[] ZERO = new short[0];

	private short[] array = ZERO;

	private short defaultValue;

	private int length = 0;

	public ShortList() {
		this((short) 0);
	}

	public ShortList(short defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void set(int pos, short value) {
		if (this.length <= pos) {
			this.length = pos + 1;
			if (this.array.length <= pos) {
				short[] array = new short[Math.max(this.length + 10, this.array.length * 3 / 2)];
				for (int i = this.array.length; i < array.length; ++i) {
					array[i] = this.defaultValue;
				}
				System.arraycopy(this.array, 0, array, 0, this.array.length);
				this.array = array;
			}
		}
		this.array[pos] = value;
	}

	public short[] toArray() {
		this.pack();
		return this.array;
	}

	public short get(int i) {
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
			short[] array = new short[this.length];
			System.arraycopy(this.array, 0, array, 0, this.length);
			this.array = array;
		}
	}

	public boolean isEmpty() {
		return this.length == 0;
	}
}
