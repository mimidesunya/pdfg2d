package net.zamasoft.pdfg2d.gc.font;

public class UnicodeRangeList {
	private final UnicodeRange[] includes;

	public UnicodeRangeList(UnicodeRange[] includes) {
		if (includes == null) {
			throw new NullPointerException();
		}
		this.includes = includes;
	}

	public boolean canDisplay(int c) {
		if (this.includes.length == 0) {
			return true;
		}
		for (int i = 0; i < this.includes.length; ++i) {
			if (this.includes[i].contains(c)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < this.includes.length; ++i) {
			if (i > 0) {
				buff.append(", ");
			}
			buff.append(this.includes[i]);
		}
		return buff.toString();
	}

	public boolean isEmpty() {
		return this.includes.length == 0;
	}
}
