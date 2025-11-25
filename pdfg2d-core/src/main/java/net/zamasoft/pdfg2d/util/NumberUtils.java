package net.zamasoft.pdfg2d.util;

public final class NumberUtils {
	private static final Short[] S = { (short) 0, (short) 1, (short) 2, (short) 3 };
	private static final Integer[] I = { 0, 1, 2, 3 };

	private NumberUtils() {}

	public static Integer intValue(int a) {
		return (a >= 0 && a < I.length) ? I[a] : a;
	}

	public static Short shortValue(short a) {
		return (a >= 0 && a < S.length) ? S[a] : a;
	}

	public static double parseDouble(String str) {
		var a = Double.parseDouble(str);
		if (Double.isNaN(a)) throw new NumberFormatException(str);
		return a;
	}
}
