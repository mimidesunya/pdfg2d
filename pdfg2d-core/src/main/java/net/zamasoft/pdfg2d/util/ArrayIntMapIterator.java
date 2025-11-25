package net.zamasoft.pdfg2d.util;

public final class ArrayIntMapIterator implements IntMapIterator {
	private final int[] array;
	private int i = 0;

	public ArrayIntMapIterator(int[] array) {
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
	public int value() {
		return array[i - 1];
	}
}
