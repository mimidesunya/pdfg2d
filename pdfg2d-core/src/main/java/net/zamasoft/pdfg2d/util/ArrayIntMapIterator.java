package net.zamasoft.pdfg2d.util;

/**
 * An {@link IntMapIterator} implementation backed by an int array.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class ArrayIntMapIterator implements IntMapIterator {
	private final int[] array;
	private int i = 0;

	/**
	 * Constructs a new iterator.
	 * 
	 * @param array the backing array
	 */
	public ArrayIntMapIterator(final int[] array) {
		this.array = array;
	}

	@Override
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
