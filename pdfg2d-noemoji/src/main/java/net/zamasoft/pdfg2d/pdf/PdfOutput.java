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

import net.zamasoft.pdfg2d.pdf.util.PdfUtils;

/**
 * PDFデータを出力します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class PdfOutput extends FilterOutputStream {
	private boolean spaceBefore = true;

	private final String nameEncoding;

	public static final byte[] EOL = { 0x0D, 0x0A };

	public static final byte[] NULL = { 'n', 'u', 'l', 'l' };

	public static final byte[] TRUE = { 't', 'r', 'u', 'e' };

	public static final byte[] FALSE = { 'f', 'a', 'l', 's', 'e' };

	private ByteBuffer bbuff = null;

	public PdfOutput(OutputStream out, String nameEncoding) throws IOException {
		super(out);
		this.nameEncoding = nameEncoding;
	}

	/**
	 * リンクの位置です。
	 * 
	 * @author MIYABE Tatsuhiko
	 * @version $Id: PdfOutput.java 1565 2018-07-04 11:51:25Z miyabe $
	 */
	public static class Destination {
		public final ObjectRef pageRef;

		public final double x, y, zoom;

		/**
		 * 
		 * @param pageRef
		 *            リンク先ページ。
		 * @param x
		 *            X座標。
		 * @param y
		 *            Y座標。
		 * @param zoom
		 *            拡大率。
		 */
		public Destination(ObjectRef pageRef, double x, double y, double zoom) {
			this.pageRef = pageRef;
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}
	}

	/**
	 * オブジェクトリファレンスを出力します。
	 * 
	 * @throws IOException
	 */
	public void writeObjectRef(ObjectRef ref) throws IOException {
		this.writeInt(ref.objectNumber);
		this.writeInt(ref.generationNumber);
		this.writeOperator("R");
	}

	/**
	 * 位置を出力します。
	 * 
	 * @throws IOException
	 */
	public void writeDestination(Destination dest) throws IOException {
		this.startArray();
		this.writeObjectRef(dest.pageRef);
		this.writeName("XYZ");
		this.writeReal(dest.x);
		this.writeReal(dest.y);
		this.writeReal(dest.zoom);
		this.endArray();
	}

	/**
	 * 名前リテラルを出力します。
	 * 
	 * @param name
	 * @throws IOException
	 */
	public void writeName(String name) throws IOException {
		this.spaceBefore();
		this.write('/');
		byte[] b = PdfUtils.encodeName(name, this.nameEncoding);
		if (b.length <= 0 || b.length > 127) {
			throw new IllegalArgumentException("名前は1から127バイトまでです。");
		}
		this.write(b);
	}

	/**
	 * オペレータを出力します。
	 * 
	 * @param name
	 * @throws IOException
	 */
	public void writeOperator(String name) throws IOException {
		this.spaceBefore();
		this.write(name);
	}

	/**
	 * nullを出力します。
	 * 
	 * @throws IOException
	 */
	public void writeNull() throws IOException {
		this.spaceBefore();
		this.write(NULL);
	}

	/**
	 * booleanリテラルを出力します。
	 * 
	 * @param b
	 * @throws IOException
	 */
	public void writeBoolean(boolean b) throws IOException {
		this.spaceBefore();
		this.write(b ? TRUE : FALSE);
	}

	/**
	 * 整数リテラルを出力します。
	 * 
	 * @param number
	 * @throws IOException
	 */
	public void writeInt(int number) throws IOException {
		this.spaceBefore();
		this.write(String.valueOf(number));
	}

	/**
	 * 実数リテラルを出力します。
	 * 
	 * @param number
	 * @throws IOException
	 */
	public void writeReal(double number) throws IOException {
		this.spaceBefore();
		this.write(this.toString(number));
	}

	private static NumberFormat FORMAT = new DecimalFormat("#.#####");

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
		double round = (int) number;
		if (number == round) {
			s = String.valueOf((int) number);
		} else {
			// "#.#####" 形式で出力
			// PDF C.1によると小数点以下は5桁が限界
			s = FORMAT.format(number);
		}
		return s;
	}

	/**
	 * PDFに出力する際の精度で実数を比較します。
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean equals(double a, double b) {
		return this.toString(a).equals(this.toString(b));
	}

	/**
	 * ハッシュ(辞書)の開始を出力します。
	 * 
	 * @throws IOException
	 */
	public void startHash() throws IOException {
		this.writeLine("<<");
	}

	/**
	 * ハッシュ(辞書)を出力します。
	 * 
	 * @throws IOException
	 */
	public void endHash() throws IOException {
		this.writeLine(">>");
	}

	/**
	 * 配列の開始を出力します。
	 * 
	 * @throws IOException
	 */
	public void startArray() throws IOException {
		this.spaceBefore();
		this.write('[');
	}

	/**
	 * 配列の終端を出力します。
	 * 
	 * @throws IOException
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
	 * 文字列リテラルを出力します。
	 * 
	 * @param str
	 * @throws IOException
	 */
	public void writeString(String str) throws IOException {
		this.spaceBefore();
		this.write('(');
		int len = 0;
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
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
			throw new IllegalArgumentException("文字列が65535バイトを超えています。");
		}
	}

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	private void buffAllocate(int size) {
		if (this.bbuff == null || this.bbuff.capacity() < size) {
			this.bbuff = ByteBuffer.allocate(size);
		}
	}

	private void buffFlush() throws IOException {
		this.bbuff.flip();
		this.write(this.bbuff.array(), this.bbuff.arrayOffset(), this.bbuff.limit());
		this.bbuff.clear();
	}

	private void buffWrite(byte b) {
		this.bbuff.put(b);
	}

	private void buffHex8(byte b) {
		this.bbuff.put((byte) HEX[((b >> 4) & 0x0F)]);
		this.bbuff.put((byte) HEX[(b & 0x0F)]);
	}

	private void buffHex16(int c) {
		this.buffHex8((byte) ((c >> 8) & 0xFF));
		this.buffHex8((byte) (c & 0xFF));
	}

	private void writeHex8(byte b) throws IOException {
		this.write(HEX[((b >> 4) & 0x0F)]);
		this.write(HEX[(b & 0x0F)]);
	}

	/**
	 * ユニコード文字列リテラルを出力します。
	 * 
	 * @param text
	 * @throws IOException
	 */
	public void writeUTF16(String text) throws IOException {
		this.spaceBefore();
		this.buffAllocate(2 + 4 + 4 * text.length());
		this.buffWrite((byte) '<');
		this.buffHex8((byte) 0xFE);
		this.buffHex8((byte) 0xFF);
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			this.buffHex16(c);
		}
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	/**
	 * 8ビット以外の文字が含まれているとき、ユニコード文字列リテラルを出力します。 そうでない場合は文字列リテラルを出力します。
	 * 
	 * @param text
	 * @throws IOException
	 */
	public void writeText(String text) throws IOException {
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			if (c > 0x7F) {
				this.writeUTF16(text);
				return;
			}
		}
		this.writeString(text);
	}

	/**
	 * ファイル名を出力します。
	 * 
	 * @param elements
	 * @param encoding
	 * @throws IOException
	 */
	public void writeFileName(String[] elements, String encoding) throws IOException {
		for (int j = 0; j < elements.length; ++j) {
			String text = elements[j];
			for (int i = 0; i < text.length(); ++i) {
				char c = text.charAt(i);
				if (c > 0x7F || c == '/') {
					this.spaceBefore();
					this.write('<');
					for (int k = 0; k < elements.length; ++k) {
						byte[] name = elements[k].getBytes(encoding);
						for (int l = 0; l < name.length; ++l) {
							byte d = name[l];
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
					return;
				}
			}
		}
		StringBuffer buff = new StringBuffer();
		for (int j = 0; j < elements.length; ++j) {
			buff.append(elements[j]).append('/');
		}
		buff.deleteCharAt(buff.length() - 1);
		this.writeString(buff.toString());
	}

	/**
	 * 8ビットバイト列リテラルを出力します。
	 * 
	 * @param a
	 * @throws IOException
	 */
	public void writeBytes8(byte[] a, int off, int len) throws IOException {
		if (len > 65535) {
			throw new IllegalArgumentException("文字列が65535バイトを超えています。");
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
	 * 16ビットバイト列リテラルを出力します。
	 * 
	 * @param a
	 * @throws IOException
	 */
	public void writeBytes16(int[] a, int off, int len) throws IOException {
		if (len * 2 > 65535) {
			throw new IllegalArgumentException("文字列が65535バイトを超えています。");
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
	 * 16ビットバイトリテラルを出力します。
	 * 
	 * @param a
	 * @throws IOException
	 */
	public void writeBytes16(int a) throws IOException {
		this.spaceBefore();
		this.buffAllocate(2 + 4);
		this.buffWrite((byte) '<');
		this.buffHex16(a);
		this.buffWrite((byte) '>');
		this.buffFlush();
	}

	private final DateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * 日付を出力します。
	 * 
	 * @param time
	 * @param zone
	 * @throws IOException
	 */
	public void writeDate(long time, TimeZone zone) throws IOException {
		Date date = new Date(time);
		StringBuffer buff = new StringBuffer();
		buff.append("D:");
		buff.append(PDF_DATE_FORMAT.format(date));
		buff.append(zone.getRawOffset() < 0 ? '-' : '+');
		long absOff = Math.abs(zone.getRawOffset());
		String h = String.valueOf(absOff / 3600000L);
		if (h.length() <= 1) {
			buff.append('0');
		}
		buff.append(h);
		buff.append('\'');
		String m = String.valueOf(absOff % 3600000L / 60000L);
		if (m.length() <= 1) {
			buff.append('0');
		}
		buff.append(m);
		buff.append('\'');
		this.writeString(buff.toString());
	}

	/**
	 * 現在の位置で改行します。
	 * 
	 * @throws IOException
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
	 * 前後に改行が入った1行のテキストを出力します。
	 * 
	 * @param line
	 * @throws IOException
	 */
	public void writeLine(String line) throws IOException {
		this.breakBefore();
		this.write(line);
		this.lineBreak();
	}

	public void write(String str) throws IOException {
		this.buffAllocate(str.length());
		for (int i = 0; i < str.length(); ++i) {
			this.buffWrite((byte) str.charAt(i));
		}
		this.buffFlush();
	}

	public void write(byte[] buff, int off, int len) throws IOException {
		this.out.write(buff, off, len);
	}

	public void write(byte[] buff) throws IOException {
		this.out.write(buff);
	}

	public void write(int b) throws IOException {
		this.out.write(b);
	}

	public void close() throws IOException {
		this.out.close();
	}

	public void flush() throws IOException {
		this.out.flush();
	}
}