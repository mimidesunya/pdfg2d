package net.zamasoft.pdfg2d.pdf.font.type2;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * CFFのデータを出力します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CFFOutputStream extends FilterOutputStream {
	public static final byte[] VERSION = { 0 };

	public static final byte[] NOTICE = { 1 };

	public static final byte[] FULL_NAME = { 2 };

	public static final byte[] FAMILY_NAME = { 3 };

	public static final byte[] WEIGHT = { 4 };

	public static final byte[] FONT_BBOX = { 5 };

	public static final byte[] BLUE_VALUES = { 6 };

	public static final byte[] OTHER_BLUES = { 7 };

	public static final byte[] FAMILY_BLUES = { 8 };

	public static final byte[] FAMILY_OTHER_BLUES = { 9 };

	public static final byte[] STD_HW = { 10 };

	public static final byte[] STD_VW = { 11 };

	public static final byte[] UNIQUE_ID = { 13 };

	public static final byte[] XUID = { 14 };

	public static final byte[] CHARSETS = { 15 };

	public static final byte[] ENCODING = { 16 };

	public static final byte[] CHAR_STRINGS = { 17 };

	public static final byte[] PRIVATE = { 18 };

	public static final byte[] SUBRS = { 19 };

	public static final byte[] DEFAULT_WIDTHX = { 20 };

	public static final byte[] NOMINAL_WIDTHX = { 21 };

	public static final byte[] COPYRIGHT = { 12, 0 };

	public static final byte[] IS_FIXED_PITCH = { 12, 1 };

	public static final byte[] ITALIC_ANGLE = { 12, 2 };

	public static final byte[] UNDERLINE_POSITION = { 12, 3 };

	public static final byte[] UNDERLINE_THICKNESS = { 12, 4 };

	public static final byte[] PAINT_TYPE = { 12, 5 };

	public static final byte[] CHARSTRING_TYPE = { 12, 6 };

	public static final byte[] FONT_MATRIX = { 12, 7 };

	public static final byte[] STROKE_WIDTH = { 12, 8 };

	public static final byte[] BLUE_SCALE = { 12, 9 };

	public static final byte[] BLUE_SHIFT = { 12, 10 };

	public static final byte[] BLUE_FUZZ = { 12, 11 };

	public static final byte[] STEM_SNAP_H = { 12, 12 };

	public static final byte[] STEM_SNAP_V = { 12, 13 };

	public static final byte[] FORCE_BOLD = { 12, 14 };

	public static final byte[] LANGUAGE_GROUP = { 12, 17 };

	public static final byte[] EXPANSION_FACTOR = { 12, 18 };

	public static final byte[] INITIAL_RANDOM_SEED = { 12, 19 };

	public static final byte[] SYNTHETIC_BASE = { 12, 20 };

	public static final byte[] POST_SCRIPT = { 12, 21 };

	public static final byte[] BASE_FONT_NAME = { 12, 22 };

	public static final byte[] BASE_FONT_BLEND = { 12, 23 };

	public static final byte[] ROS = { 12, 30 };

	public static final byte[] CID_FONT_VERSION = { 12, 31 };

	public static final byte[] CID_FONT_REVISION = { 12, 32 };

	public static final byte[] CID_FONT_TYPE = { 12, 33 };

	public static final byte[] CID_COUNT = { 12, 34 };

	public static final byte[] UID_BASE = { 12, 35 };

	public static final byte[] FD_ARRAY = { 12, 36 };

	public static final byte[] FD_SELECT = { 12, 37 };

	public static final byte[] FONT_NAME = { 12, 38 };

	private static final int NSTDSTRINGS = 391;

	private int offset = 0;

	public CFFOutputStream(OutputStream out) {
		super(out);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.offset += len;
		this.out.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.offset += b.length;
		this.out.write(b);
	}

	public void write(int b) throws IOException {
		++this.offset;
		this.out.write(b);
	}

	public int getOffset() {
		return this.offset;
	}

	public static byte[] toBytes(String str) {
		try {
			return str.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeCard8(byte b) throws IOException {
		this.write(b);
	}

	public void writeCard16(int a) throws IOException {
		this.write(a >> 8);
		this.write(a);
	}

	public void writeOffSize(byte offSize) throws IOException {
		if (offSize < 1 || offSize > 4) {
			throw new IllegalArgumentException();
		}
		this.write(offSize);
	}

	public void writeOffset(int a, int size) throws IOException {
		switch (size) {
		case 4:
			this.write(a >> 24);
		case 3:
			this.write(a >> 16);
		case 2:
			this.write(a >> 8);
		case 1:
			this.write(a);
			break;

		default:
			throw new IllegalArgumentException();
		}
	}

	public void writeNSSID(int sid) throws IOException {
		sid += NSTDSTRINGS;
		if (sid < 0 || sid > 64999) {
			throw new IllegalArgumentException();
		}
		this.writeInteger(sid);
	}

	public int writeOperator(byte[] o) throws IOException {
		this.write(o);
		return o.length;
	}

	public int writeInteger(int a) throws IOException {
		if (a >= -107 && a <= 107) {
			this.write(a + 139);
			return 1;
		} else if (a >= 108 && a <= 1131) {
			a -= 108;
			this.write((a >> 8) + 247);
			this.write(a);
			return 2;
		} else if (a >= -1131 && a <= -108) {
			a += 108;
			this.write((-a >> 8) + 251);
			this.write(-a);
			return 2;
		} else if (a >= -32768 && a <= 32767) {
			this.write(28);
			this.write(a >> 8);
			this.write(a);
			return 3;
		} else {
			this.write(29);
			this.write(a >> 24);
			this.write(a >> 16);
			this.write(a >> 8);
			this.write(a);
			return 5;
		}
	}

	public int writeReal(String real) throws IOException {
		this.write(0x1e);

		int count = 1;
		byte b = 0;
		boolean low = false;

		int len = real.length();
		for (int i = 0; i < len; ++i) {
			char c = real.charAt(i);
			byte hex;
			switch (c) {
			case '.':
				hex = 0xA;
				break;

			case 'E':
				if (real.charAt(i + 1) == '-') {
					++i;
					hex = 0xC;
				} else {
					hex = 0xB;
				}
				break;

			case '-':
				hex = 0xE;
				break;

			default:
				if (c < '0' || c > '9') {
					throw new IllegalArgumentException();
				}
				hex = (byte) (c - '0');
				break;
			}

			if (low) {
				++count;
				this.write(b | hex);
				low = false;
			} else {
				b = (byte) (hex << 4);
				low = true;
			}
		}
		if (low) {
			this.write(b | 0xF);
		} else {
			this.write(0xFF);
		}
		return count + 1;
	}

	public void writeHeader(byte major, byte minor, byte hdrSize, byte offSize) throws IOException {
		this.writeCard8(major);
		this.writeCard8(minor);
		this.writeCard8(hdrSize);
		this.writeOffSize(offSize);
	}

	public void writeIndex(byte[][] objects, byte offSize) throws IOException {
		this.writeCard16((short) (objects.length));
		if (objects.length <= 0) {
			// 空のインデックスはカウントだけ出力する
			return;
		}
		this.writeOffSize(offSize);

		// 各オブジェクトの位置(1オリジン)
		int offset = 1;
		for (int i = 0; i < objects.length; ++i) {
			byte[] object = objects[i];
			this.writeOffset(offset, offSize);
			offset += object.length;
		}

		// データ全体の大きさ+1
		this.writeOffset(offset, offSize);

		// データ本体
		for (int i = 0; i < objects.length; ++i) {
			byte[] object = objects[i];
			this.write(object);
		}
	}
}
