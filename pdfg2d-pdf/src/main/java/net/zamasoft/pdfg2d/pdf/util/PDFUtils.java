package net.zamasoft.pdfg2d.pdf.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Utility class for PDF operations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class PDFUtils {
	private PDFUtils() {
		// unused
	}

	/** Points per inch. */
	public static final double POINTS_PER_INCH = 72.0;

	/** Points per mm. */
	public static final double POINTS_PER_MM = POINTS_PER_INCH / 25.4;

	/** Points per cm. */
	public static final double POINTS_PER_CM = POINTS_PER_INCH / 2.54;

	public static final double PAPER_A4_WIDTH_MM = 210.0;

	public static final double PAPER_A4_HEIGHT_MM = 297.0;

	public static final double CUTTING_MARGIN_MM = 3.0;

	public static double mmToPt(final double mm) {
		return mm * POINTS_PER_MM;
	}

	private static final byte[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static byte[] encodeName(final String s, final String encoding) throws UnsupportedEncodingException {
		boolean encode = false;
		final byte[] b = s.getBytes(encoding);
		for (final byte c : b) {
			if (c >= '!' && c <= '~') {
				switch (c) {
					case '#':
					case '(':
					case ')':
					case '[':
					case ']':
					case '{':
					case '}':
					case '<':
					case '>':
					case '/':
					case '%':
						break;
					default:
						continue;
				}
			}
			encode = true;
			break;
		}
		if (!encode) {
			return b;
		}
		final ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (final byte c : b) {
			if (c >= '!' && c <= '~') {
				switch (c) {
					case '#':
					case '(':
					case ')':
					case '[':
					case ']':
					case '{':
					case '}':
					case '<':
					case '>':
					case '/':
					case '%': {
						buff.write('#');
						final short h = (short) ((c >> 4) & 0x0F);
						final short l = (short) (c & 0x0F);
						buff.write(HEX[h]);
						buff.write(HEX[l]);
					}
						break;
					default:
						buff.write(c);
						break;
				}
			} else {
				buff.write('#');
				final short h = (short) ((c >> 4) & 0x0F);
				final short l = (short) (c & 0x0F);
				buff.write(HEX[h]);
				buff.write(HEX[l]);
			}
		}
		return buff.toByteArray();
	}

	public static String decodeName(final String s, final String encoding) throws UnsupportedEncodingException {
		final char[] ch = s.toCharArray();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < ch.length; ++i) {
			final char c = ch[i];
			if (c != '#') {
				out.write(c);
			} else {
				final char h = s.charAt(++i);
				final char l = s.charAt(++i);
				out.write(Integer.parseInt("" + h + l, 16));
			}
		}
		return new String(out.toByteArray(), encoding);
	}
}