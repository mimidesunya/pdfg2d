package net.zamasoft.pdfg2d.util;

public final class ArrayShortMapIterator implements ShortMapIterator {
	private final short[] array;
	private int i = 0;

	public ArrayShortMapIterator(short[] array) {
		this.array = array;
	}

	@Override
	public boolean next() {
		if (i >= array.length) {
			return false;
		}
		i++;
		return true;
	}

	@Override
	public int key() {
		return i - 1;
	}

	@Override
	public short value() {
		return array[i - 1];
	}
}
