package net.zamasoft.pdfg2d.util;

/**
 * Utility methods for number handling.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class NumberUtils {
	private static final Short[] S = { (short) 0, (short) 1, (short) 2, (short) 3 };
	private static final Integer[] I = { 0, 1, 2, 3 };

	private NumberUtils() {
	}

	/**
	 * Returns an Integer instance representing the specified int value.
	 * <p>
	 * Ideally this should cache commonly used values similar to
	 * {@link Integer#valueOf(int)}.
	 * </p>
	 * 
	 * @param a the int value
	 * @return the Integer instance
	 */
	public static Integer intValue(final int a) {
		return (a >= 0 && a < I.length) ? I[a] : a;
	}

	/**
	 * Returns a Short instance representing the specified short value.
	 * 
	 * @param a the short value
	 * @return the Short instance
	 */
	public static Short shortValue(final short a) {
		return (a >= 0 && a < S.length) ? S[a] : a;
	}

	/**
	 * Parses a string to a double value.
	 * 
	 * @param str the string to parse
	 * @return the double value
	 * @throws NumberFormatException if the string does not contain a parsable
	 *                               double or is NaN
	 */
	public static double parseDouble(final String str) {
		final var a = Double.parseDouble(str);
		if (Double.isNaN(a)) {
			throw new NumberFormatException(str);
		}
		return a;
	}
}
