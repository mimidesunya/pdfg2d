package net.zamasoft.pdfg2d.util;

public class ArrayIntMapIterator implements IntMapIterator {
	private final int[] array;
	private int i = 0;

	public ArrayIntMapIterator(int[] array) {
		this.array = array;
	}

	public boolean next() {
		if (this.i >= this.array.length) {
			return false;
		}
		this.i++;
		return true;
	}

	@Override
	public int key() {
		return this.i - 1;
	}

	@Override
	public int value() {
		return this.array[this.i - 1];
	}

}
