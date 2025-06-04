package net.zamasoft.pdfg2d.util;

public final class NumberUtils {
	private static final Short[] S = { Short.valueOf((short) 0), Short.valueOf((short) 1), Short.valueOf((short) 2),
			Short.valueOf((short) 3), };
	private static final Integer[] I = { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2),
			Integer.valueOf(3), };

	private NumberUtils() {
		// unused
	}

	public static Integer intValue(int a) {
		if (a >= 0 && a < I.length) {
			return I[a];
		}
		return Integer.valueOf(a);
	}

	public static Short shortValue(short a) {
		if (a >= 0 && a < S.length) {
			return S[a];
		}
		return Short.valueOf(a);
	}

	public static double parseDouble(String str) {
		final double a = Double.parseDouble(str);
		if (Double.isNaN(a)) {
			throw new NumberFormatException(str);
		}
		return a;
	}
}
