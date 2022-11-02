package net.zamasoft.pdfg2d.pdf.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class PDFUtils {
	private PDFUtils() {
		// unused
	}

	/** 1インチ辺りのポイント数。 */
	public static final double POINTS_PER_INCH = 72.0;

	/** 1mm辺りのポイント数。 */
	public static final double POINTS_PER_MM = POINTS_PER_INCH / 25.4;

	/** 1cm辺りのポイント数。 */
	public static final double POINTS_PER_CM = POINTS_PER_INCH / 2.54;

	public static final double PAPER_A4_WIDTH_MM = 210.0;

	public static final double PAPER_A4_HEIGHT_MM = 297.0;

	public static final double CUTTING_MARGIN_MM = 3.0;

	public static double mmToPt(double mm) {
		return mm * POINTS_PER_MM;
	}

	private static final byte[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static byte[] encodeName(String s, String encoding) throws UnsupportedEncodingException {
		boolean encode = false;
		byte[] b = s.getBytes(encoding);
		for (int i = 0; i < b.length; ++i) {
			byte c = b[i];
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
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int i = 0; i < b.length; ++i) {
			byte c = b[i];
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
					short h = (short) ((c >> 4) & 0x0F);
					short l = (short) (c & 0x0F);
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
				short h = (short) ((c >> 4) & 0x0F);
				short l = (short) (c & 0x0F);
				buff.write(HEX[h]);
				buff.write(HEX[l]);
			}
		}
		return buff.toByteArray();
	}

	public static String decodeName(String s, String encoding) throws UnsupportedEncodingException {
		char[] ch = s.toCharArray();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < ch.length; ++i) {
			char c = ch[i];
			if (c != '#') {
				out.write(c);
			} else {
				char h = s.charAt(++i);
				char l = s.charAt(++i);
				out.write(Integer.parseInt("" + h + l, 16));
			}
		}
		return new String(out.toByteArray(), encoding);
	}
}