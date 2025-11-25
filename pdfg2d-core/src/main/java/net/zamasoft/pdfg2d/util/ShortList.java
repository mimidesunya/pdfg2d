package net.zamasoft.pdfg2d.util;

import java.io.Serializable;

/**
 * 任意の位置の値をセット可能なshort値の配列です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class ShortList implements Serializable {
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
		if (length <= pos) {
			length = pos + 1;
			if (array.length <= pos) {
				var newArray = new short[Math.max(length + 10, array.length * 3 / 2)];
				for (int i = array.length; i < newArray.length; ++i) {
					newArray[i] = defaultValue;
				}
				System.arraycopy(array, 0, newArray, 0, array.length);
				array = newArray;
			}
		}
		array[pos] = value;
	}

	public short[] toArray() {
		pack();
		return array;
	}

	public short get(int i) {
		if (i >= array.length) {
			return defaultValue;
		}
		return array[i];
	}

	public int size() {
		return length;
	}

	public void pack() {
		if (length != array.length) {
			var newArray = new short[length];
			System.arraycopy(array, 0, newArray, 0, length);
			array = newArray;
		}
	}

	public boolean isEmpty() {
		return length == 0;
	}
}
