package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

public class UnicodeRange implements Serializable {
	private static final long serialVersionUID = 2L;

	public final int first, last;

	public UnicodeRange(int first, int last) {
		this.first = first;
		this.last = last;
	}

	public static UnicodeRange parseRange(String s) throws NumberFormatException {
		int first, last;
		int hyph = s.indexOf('-');
		if (hyph != -1) {
			String u1 = s.substring(2, hyph);
			String u2 = s.substring(hyph + 1);
			if (u2.startsWith("U+")) {
				u2 = u2.substring(2);
			}
			first = Integer.parseInt(u1, 16);
			last = Integer.parseInt(u2, 16);
		} else {
			String u = s.substring(2);
			if (u.indexOf('?') != -1) {
				first = Integer.parseInt(u.replace('?', '0'), 16);
				last = Integer.parseInt(u.replace('?', 'F'), 16);
			} else {
				first = last = Integer.parseInt(u, 16);
			}
		}
		return new UnicodeRange(first, last);
	}

	public boolean contains(int c) {
		return (c >= this.first && c <= this.last);
	}

	public String toString() {
		if (this.first == this.last) {
			return "U+" + Integer.toHexString(this.first);
		}
		return "U+" + Integer.toHexString(this.first) + "-" + Integer.toHexString(this.last);
	}
}
