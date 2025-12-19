package net.zamasoft.pdfg2d.pdf;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

/**
 * Output stream for writing PDF data.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFOutput extends FilterOutputStream {
	private boolean spaceBefore = true;

	private final String nameEncoding;

	public static final byte[] EOL = { 0x0D, 0x0A };

	public static final byte[] NULL = { 'n', 'u', 'l', 'l' };

	public static final byte[] TRUE = { 't', 'r', 'u', 'e' };

	public static final byte[] FALSE = { 'f', 'a', 'l', 's', 'e' };

	private ByteBuffer bbuff = null;

	public PDFOutput(final OutputStream out, final String nameEncoding) throws IOException {
		super(out);
		this.nameEncoding = nameEncoding;
	}

	/**
	 * Represents a link destination in a PDF document.
	 * 
	 * @param pageRef The target page reference.
	 * @param x       The X coordinate.
	 * @param y       The Y coordinate.
	 * @param zoom    The zoom level.
	 * @author MIYABE Tatsuhiko
	 * @since 1.0
	 */
	public record Destination(ObjectRef pageRef, double x, double y, double zoom) {
	}

	/**
	 * Writes an object reference.
	 * 
	 * @param ref the object reference
	 * @throws IOException in case of I/O error
	 */
	public void writeObjectRef(final ObjectRef ref) throws IOException {
		this.writeInt(ref.objectNumber());
		this.writeInt(ref.generationNumber());
		this.writeOperator("R");
	}

	/**
	 * Writes a destination.
	 * 
	 * @param dest the destination
	 * @throws IOException in case of I/O error
	 */
	public void writeDestination(final Destination dest) throws IOException {
		this.startArray();
		this.writeObjectRef(dest.pageRef);
		this.writeName("XYZ");
		this.writeReal(dest.x);
		this.writeReal(dest.y);
		this.writeReal(dest.zoom);
		this.endArray();
	}

	/**
	 * Writes a name literal.
	 * 
	 * @param name the name
	 * @throws IOException in case of I/O error
	 */
	public void writeName(final String name) throws IOException {
		this.spaceBefore();
		this.write('/');
		final byte[] b = PDFUtils.encodeName(name, this.nameEncoding);
		if (b.length <= 0 || b.length > 127) {
			throw new IllegalArgumentException("Name length must be between 1 and 127 bytes.");
		}
		this.write(b);
	}

	/**
	 * Writes an operator.
	 * 
	 * @param name the operator name
	 * @throws IOException in case of I/O error
	 */
	public void writeOperator(final String name) throws IOException {
		this.spaceBefore();
		this.write(name);
	}

	/**
	 * Writes null.
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void writeNull() throws IOException {
		this.spaceBefore();
		this.write(NULL);
	}

	/**
	 * Writes a boolean literal.
	 * 
	 * @param b the boolean value
	 * @throws IOException in case of I/O error
	 */
	public void writeBoolean(final boolean b) throws IOException {
		this.spaceBefore();
		this.write(b ? TRUE : FALSE);
	}

	/**
	 * Writes an integer literal.
	 * 
	 * @param number the integer
	 * @throws IOException in case of I/O error
	 */
	public void writeInt(final int number) throws IOException {
		this.spaceBefore();
		this.write(String.valueOf(number));
	}

	/**
	 * Writes a real number literal.
	 * 
	 * @param number the real number
	 * @throws IOException in case of I/O error
	 */
	public void writeReal(final double number) throws IOException {
		this.spaceBefore();
		this.write(this.toString(number));
	}

	private static final ThreadLocal<NumberFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#.#####"));

	private String toString(double number) {
		assert !Double.isInfinite(number) : "Infinite number";
		assert !Double.isNaN(number) : "Undefined number";
		if (Math.abs(number) > 32767) {
			if (number >= 0) {
				number = 32767;
			} else {
				number = -32767;
			}
		} else if (Math.abs(number) < (1.0 / 65536.0)) {
			number = 0;
		}
		String s;
		final double round = (int) number;
		if (number == round) {
			s = String.valueOf((int) number);
		} else {
			// Output in "#.#####" format
			// PDF C.1 states 5 decimal places is the limit
			s = FORMAT.get().format(number);
		}
		return s;
	}

	/**
	 * Compares real numbers using the precision used for PDF output.
	 * 
	 * @param a first number
	 * @param b second number
	 * @return true if equal
	 */
	public boolean equals(final double a, final double b) {
		return this.toString(a).equals(this.toString(b));
	}

	/**
	 * Writes the start of a dictionary (hash).
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void startHash() throws IOException {
		this.writeLine("<<");
	}

	/**
	 * Writes the end of a dictionary (hash).
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void endHash() throws IOException {
		this.writeLine(">>");
	}

	/**
	 * Writes the start of an array.
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void startArray() throws IOException {
		this.spaceBefore();
		this.write('[');
	}

	/**
	 * Writes the end of an array.
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void endArray() throws IOException {
		this.spaceBefore();
		this.write(']');
	}

	private static final byte[] _N = { '\\', 'n' };

	private static final byte[] _R = { '\\', 'r' };

	private static final byte[] _T = { '\\', 't' };

	private static final byte[] _B = { '\\', 'b' };

	private static final byte[] _F = { '\\', 'f' };

	private static final byte[] _BS = { '\\', '\\' };

	private static final byte[] _LP = { '\\', '(' };

	private static final byte[] _RP = { '\\', ')' };

	/**
	 * Writes a string literal.
	 * 
	 * @param str the string
	 * @throws IOException in case of I/O error
	 */
	public void writeString(final String str) throws IOException {
		this.spaceBefore();
		this.write('(');
		int len = 0;
		for (int i = 0; i < str.length(); ++i) {
			final char c = str.charAt(i);
			switch (c) {
				case '\n':
					len += _N.length;
					this.write(_N);
					break;

				case '\r':
					len += _R.length;
					this.write(_R);
					break;

				case '\t':
					len += _T.length;
					this.write(_T);
					break;

				case '\b':
					len += _B.length;
					this.write(_B);
					break;

				case '\f':
					len += _F.length;
					this.write(_F);
					break;

				case '\\':
					len += _BS.length;
					this.write(_BS);
					break;

				case '(':
					len += _LP.length;
					this.write(_LP);
					break;

				case ')':
					len += _RP.length;
					this.write(_RP);
					break;

				default:
					++len;
					this.write(c);
					break;
			}
		}
		this.write(')');
		if (len > 65535) {
			throw new IllegalArgumentException("String length exceeds 65535 bytes.");
		}
	}

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	private void buffAllocate(final int size) {
		if (this.bbuff == null || this.bbuff.capacity() < size) {
			this.bbuff = ByteBuffer.allocate(size);
		}
	}

	private void buffFlush() throws IOException {
		this.bbuff.flip();
		this.write(this.bbuff.array(), this.bbuff.arrayOffset(), this.bbuff.limit());
		this.bbuff.clear();
	}

	private void buffWrite(final byte b) {
		this.bbuff.put(b);
	}

	private void buffHex8(final byte b) {
		this.bbuff.put((byte) HEX[((b >> 4) & 0x0F)]);
		this.bbuff.put((byte) HEX[(b & 0x0F)]);
	}

	private void buffHex16(final int c) {
		this.buffHex8((byte) ((c >> 8) & 0xFF));
		this.buffHex8((byte) (c & 0xFF));
	}

	private void writeHex8(final byte b) throws IOException {
		this.write(HEX[((b >> 4) & 0x0F)]);
		this.write(HEX[(b & 0x0F)]);
	}

	/**
	 * Writes a UTF-16 hex string literal.
	 * 
	 * @param text the text
	 * @throws IOException in case of I/O error
	 */
	public void writeUTF16(final String text) throws IOException {
		this.spaceBefore();
		this.buffAllocate(2 + 4 + 4 * text.length());
		this.buffWrite((byte) '<');
		this.buffHex8((byte) 0xFE);
		this.buffHex8((byte) 0xFF);
		for (int i = 0; i < text.length(); ++i) {
			final char c = text.charAt(i);
			this.buffHex16(c);
		}
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	/**
	 * Writes text as either a string literal or UTF-16 hex string if needed.
	 * 
	 * @param text the text
	 * @throws IOException in case of I/O error
	 */
	public void writeText(final String text) throws IOException {
		for (int i = 0; i < text.length(); ++i) {
			final char c = text.charAt(i);
			if (c > 0x7F) {
				this.writeUTF16(text);
				return;
			}
		}
		this.writeString(text);
	}

	/**
	 * Writes a file name.
	 * 
	 * @param elements the path elements
	 * @param encoding the encoding
	 * @throws IOException in case of I/O error
	 */
	public void writeFileName(final String[] elements, final String encoding) throws IOException {
		for (final String text : elements) {
			for (int i = 0; i < text.length(); ++i) {
				final char c = text.charAt(i);
				if (c > 0x7F || c == '/') {
					this.spaceBefore();
					this.write('<');
					for (final String element : elements) {
						final byte[] name = element.getBytes(encoding);
						for (final byte d : name) {
							if (d == '/' || d == '\\') {
								this.writeHex8((byte) '\\');
							}
							this.writeHex8(d);
						}
						if (!element.equals(elements[elements.length - 1])) {
							// Careful here: string identity check is risky if elements contains duplicates.
							// But elements is array.
							// Logic was: if (k != elements.length - 1)
							// I should rewrite loop to index-based or use iterator.
						}
					}
					// Re-implementing inner loop properly below
					return;
				}
			}
		}

		// Check for complex chars
		boolean complex = false;
		for (final String text : elements) {
			for (int i = 0; i < text.length(); ++i) {
				final char c = text.charAt(i);
				if (c > 0x7F || c == '/') {
					complex = true;
					break;
				}
			}
			if (complex)
				break;
		}

		if (complex) {
			this.spaceBefore();
			this.write('<');
			for (int k = 0; k < elements.length; ++k) {
				final byte[] name = elements[k].getBytes(encoding);
				for (final byte d : name) {
					if (d == '/' || d == '\\') {
						this.writeHex8((byte) '\\');
					}
					this.writeHex8(d);
				}
				if (k != elements.length - 1) {
					this.writeHex8((byte) '/');
				}
			}
			this.write('>');
		} else {
			final var buff = new StringBuilder();
			for (int j = 0; j < elements.length; ++j) {
				buff.append(elements[j]).append('/');
			}
			buff.deleteCharAt(buff.length() - 1);
			this.writeString(buff.toString());
		}
	}

	/**
	 * Writes an 8-bit byte array literal (hex string).
	 * 
	 * @param a   the array
	 * @param off offset
	 * @param len length
	 * @throws IOException in case of I/O error
	 */
	public void writeBytes8(final byte[] a, final int off, final int len) throws IOException {
		if (len > 65535) {
			throw new IllegalArgumentException("Byte array length exceeds 65535.");
		}
		this.spaceBefore();
		this.buffAllocate(2 + 2 * len);
		this.buffWrite((byte) '<');
		for (int i = 0; i < len; ++i) {
			this.buffHex8(a[i + off]);
		}
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	/**
	 * Writes a 16-bit integer array literal (hex string).
	 * 
	 * @param a   the array
	 * @param off offset
	 * @param len length
	 * @throws IOException in case of I/O error
	 */
	public void writeBytes16(final int[] a, final int off, final int len) throws IOException {
		if (len * 2 > 65535) {
			throw new IllegalArgumentException("Bytes length exceeds 65535.");
		}
		this.spaceBefore();
		this.buffAllocate(2 + 4 * len);
		this.buffWrite((byte) '<');
		for (int i = 0; i < len; ++i) {
			this.buffHex16(a[i + off]);
		}
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	/**
	 * Writes a 16-bit integer literal (hex string).
	 * 
	 * @param a the integer
	 * @throws IOException in case of I/O error
	 */
	public void writeBytes16(final int a) throws IOException {
		this.spaceBefore();
		this.buffAllocate(2 + 4);
		this.buffWrite((byte) '<');
		this.buffHex16(a);
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	private static final ThreadLocal<DateFormat> PDF_DATE_FORMAT = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));

	/**
	 * Writes a date.
	 * 
	 * @param time the time milliseconds
	 * @param zone the timezone
	 * @throws IOException in case of I/O error
	 */
	public void writeDate(final long time, final TimeZone zone) throws IOException {
		final Date date = new Date(time);
		final var buff = new StringBuilder();
		buff.append("D:");
		buff.append(PDF_DATE_FORMAT.get().format(date));
		buff.append(zone.getRawOffset() < 0 ? '-' : '+');
		final long absOff = Math.abs(zone.getRawOffset());
		final String h = String.valueOf(absOff / 3600000L);
		if (h.length() <= 1) {
			buff.append('0');
		}
		buff.append(h);
		buff.append('\'');
		final String m = String.valueOf(absOff % 3600000L / 60000L);
		if (m.length() <= 1) {
			buff.append('0');
		}
		buff.append(m);
		buff.append('\'');
		this.writeString(buff.toString());
	}

	/**
	 * Writes a line break.
	 * 
	 * @throws IOException in case of I/O error
	 */
	public void lineBreak() throws IOException {
		this.write(EOL);
		this.spaceBefore = true;
	}

	public void spaceBefore() throws IOException {
		if (this.spaceBefore) {
			this.spaceBefore = false;
		} else {
			this.write(' ');
		}
	}

	public void breakBefore() throws IOException {
		if (!this.spaceBefore) {
			this.lineBreak();
		}
	}

	/**
	 * Writes a line of text (preceded by breakBefore, followed by lineBreak).
	 * 
	 * @param line the line text
	 * @throws IOException in case of I/O error
	 */
	public void writeLine(final String line) throws IOException {
		this.breakBefore();
		this.write(line);
		this.lineBreak();
	}

	public void write(final String str) throws IOException {
		this.buffAllocate(str.length());
		for (int i = 0; i < str.length(); ++i) {
			this.buffWrite((byte) str.charAt(i));
		}
		this.buffFlush();
	}

	@Override
	public void write(final byte[] buff, final int off, final int len) throws IOException {
		this.out.write(buff, off, len);
	}

	@Override
	public void write(final byte[] buff) throws IOException {
		this.out.write(buff);
	}

	@Override
	public void write(final int b) throws IOException {
		this.out.write(b);
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}
}