package net.zamasoft.pdfg2d.util;

public final class NumberUtils {
	private static final Short[] S = { new Short((short) 0), new Short((short) 1), new Short((short) 2),
			new Short((short) 3), };
	private static final Integer[] I = { new Integer(0), new Integer(1), new Integer(2), new Integer(3), };

	private NumberUtils() {
		// unused
	}

	public static Integer intValue(int a) {
		if (a >= 0 && a < I.length) {
			return I[a];
		}
		return new Integer(a);
	}

	public static Short shortValue(short a) {
		if (a >= 0 && a < S.length) {
			return S[a];
		}
		return new Short(a);
	}

	public static double parseDouble(String str) {
		final double a = Double.parseDouble(str);
		if (Double.isNaN(a)) {
			throw new NumberFormatException(str);
		}
		return a;
	}
}
