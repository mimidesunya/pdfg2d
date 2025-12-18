package net.zamasoft.pdfg2d.font.cff;

/**
 * CFF operand stack.
 */
class CFFStack {
	private int size = 0;
	private final Number[] values;

	public CFFStack() {
		final int size = 48;
		this.values = new Number[size];
	}

	public void push(final Number value) {
		this.values[this.size] = value;
		++this.size;
	}

	public Number get(final int ix) {
		if (ix >= this.size) {
			return 0;
		}
		return this.values[ix];
	}

	public Number pop() {
		if (this.size == 0) {
			return 0;
		}
		return this.values[--this.size];
	}

	public void clear() {
		this.size = 0;
	}

	public void clear(final int count) {
		this.size -= count;
		System.arraycopy(this.values, count, this.values, 0, this.size);
	}

	public int size() {
		return this.size;
	}
}
