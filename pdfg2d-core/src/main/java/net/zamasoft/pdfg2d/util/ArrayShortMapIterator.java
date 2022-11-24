package net.zamasoft.pdfg2d.util;

public class ArrayShortMapIterator implements ShortMapIterator {
	private final short[] array;
	private int i = 0;

	public ArrayShortMapIterator(short[] array) {
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
	public short value() {
		return this.array[this.i - 1];
	}

}
