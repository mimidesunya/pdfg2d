package net.zamasoft.pdfg2d.util;

/**
 * A {@link ShortMapIterator} implementation backed by a short array.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class ArrayShortMapIterator implements ShortMapIterator {
	private final short[] array;
	private int i = 0;

	/**
	 * Constructs a new iterator.
	 * 
	 * @param array the backing array
	 */
	public ArrayShortMapIterator(final short[] array) {
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
	public short value() {
		return this.array[this.i - 1];
	}
}
