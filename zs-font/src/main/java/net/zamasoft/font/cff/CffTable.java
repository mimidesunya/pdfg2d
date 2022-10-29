/*

 Copyright 2001,2003  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.zamasoft.font.cff;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.zamasoft.font.Glyph;
import net.zamasoft.font.table.DirectoryEntry;
import net.zamasoft.font.table.Table;

/**
 * @version $Id: CffTable.java 1502 2017-05-17 06:12:35Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class CffTable implements Table {

	public static final int VERSION = 0x0000;

	public static final int NOTICE = 0x0001;

	public static final int FULL_NAME = 0x0002;

	public static final int FAMILY_NAME = 0x0003;

	public static final int WEIGHT = 0x0004;

	public static final int FONT_BBOX = 0x0005;

	public static final int BLUE_VALUES = 0x0006;

	public static final int OTHER_BLUES = 0x0007;

	public static final int FAMILY_BLUES = 0x0008;

	public static final int FAMILY_OTHER_BLUES = 0x0009;

	public static final int STD_HW = 0x000A;

	public static final int STD_VW = 0x000B;

	public static final int UNIQUE_ID = 0x000D;

	public static final int XUID = 0x000E;

	public static final int CHARSETS = 0x000F;

	public static final int ENCODING = 0x0010;

	public static final int CHAR_STRINGS = 0x0011;

	public static final int PRIVATE = 0x0012;

	public static final int SUBRS = 0x0013;

	public static final int DEFAULT_WIDTHX = 0x0014;

	public static final int NOMINAL_WIDTHX = 0x0015;

	public static final int COPYRIGHT = 0x0C00;

	public static final int IS_FIXED_PITCH = 0x0C01;

	public static final int ITALIC_ANGLE = 0x0C02;

	public static final int UNDERLINE_POSITION = 0x0C03;

	public static final int UNDERLINE_THICKNESS = 0x0C04;

	public static final int PAINT_TYPE = 0x0C05;

	public static final int CHARSTRING_TYPE = 0x0C06;

	public static final int FONT_MATRIX = 0x070C;

	public static final int STROKE_WIDTH = 0x0C08;

	public static final int BLUE_SCALE = 0x0C09;

	public static final int BLUE_SHIFT = 0x0C0A;

	public static final int BLUE_FUZZ = 0x0C0B;

	public static final int STEM_SNAP_H = 0x0C0C;

	public static final int STEM_SNAP_V = 0x0C0D;

	public static final int FORCE_BOLD = 0x0C0E;

	public static final int LANGUAGE_GROUP = 0x0C11;

	public static final int EXPANSION_FACTOR = 0x120C;

	public static final int INITIAL_RANDOM_SEED = 0x130C;

	public static final int SYNTHETIC_BASE = 0x140C;

	public static final int POST_SCRIPT = 0x150C;

	public static final int BASE_FONT_NAME = 0x160C;

	public static final int BASE_FONT_BLEND = 0x170C;

	public static final int ROS = 0x1E0C;

	public static final int CID_FONT_VERSION = 0x1F0C;

	public static final int CID_FONT_REVISION = 0x0C20;

	public static final int CID_FONT_TYPE = 0x0C21;

	public static final int CID_COUNT = 0x0C22;

	public static final int UID_BASE = 0x0C23;

	public static final int FD_ARRAY = 0x0C24;

	public static final int FD_SELECT = 0x0C25;

	public static final int FONT_NAME = 0x0C26;

	public static final byte TYPE_OPERATOR = 1;
	public static final byte TYPE_INTEGER = 2;
	public static final byte TYPE_REAL = 3;

	private static final boolean DEBUG = false;

	private final RandomAccessFile raf;

	private final CffStack stack = new CffStack();

	/**
	 * 文字ごとのCharStringの位置。
	 */
	private int[] charStringOffsets;

	/**
	 * グローバルサブルーチンの位置。
	 */
	private int[] globalSubrOffsets;

	/**
	 * 文字ごとのローカルサブルーチンの位置。
	 */
	private int[][] localSubrOffsets;

	private int b0 = -1;

	private final StringBuffer buff = new StringBuffer();

	private Type2CharString charString;

	public CffTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		this.raf = raf;
		synchronized (this.raf) {
			// ヘッダをスキップ
			{
				this.raf.seek(de.getOffset());
				byte[] header = this.readHeader();
				if (DEBUG) {
					System.err.println("Major Version: " + header[0]);
					System.err.println("Minor Version: " + header[1]);
				}
				int skip = header[2];
				this.raf.skipBytes(skip - 4);
			}

			// Name INDEX をスキップ
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Name INDEX count: " + count);
				}
				if (count != 1) {
					throw new IOException("Name INDEXが1つではありません。:" + count);
				}
				int offSize = this.readOffSize();
				this.raf.skipBytes(offSize);
				int offset = this.readOffset(offSize);
				this.raf.skipBytes(offset - 1);
			}

			// Top DICT INDEX に入る
			int dictEnd;
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Top DICT INDEX count: " + count);
				}
				if (count != 1) {
					throw new IOException("Top DICT INDEXが1つではありません。:" + count);
				}
				int offSize = this.readOffSize();
				this.raf.skipBytes(offSize);
				int offset = this.readOffset(offSize);
				dictEnd = offset - 1;
			}
			if (DEBUG) {
				System.err.println("Top DICT size:" + dictEnd);
			}
			dictEnd += this.raf.getFilePointer();

			// 最初のDICT Data
			int charStringsOffset = -1;
			int privateOffset = -1, privateEnd = 0;
			int fontDictIndexOffset = -1;
			int fdSelectOffset = -1;
			while (this.raf.getFilePointer() < dictEnd) {
				int op = this.scanOperator();
				if (DEBUG) {
					System.err.println("[DICT]:" + Integer.toHexString(op) + ";" + this.stack.size());
				}
				switch (op) {
				case CHAR_STRINGS:
					charStringsOffset = this.stack.get(0).intValue();
					break;
				case PRIVATE:
					privateEnd = this.stack.get(0).intValue();
					privateOffset = this.stack.get(1).intValue();
					break;
				case FD_ARRAY:
					fontDictIndexOffset = this.stack.get(0).intValue();
					break;
				case FD_SELECT:
					fdSelectOffset = this.stack.get(0).intValue();
					break;
				}
				this.stack.clear();
			}
			if (charStringsOffset == -1) {
				throw new IOException("DICTにCharStringsがありません。");
			}

			// String INDEX をスキップ
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("String INDEX count: " + count);
				}
				if (count > 0) {
					int offSize = this.readOffSize();
					int skip = count * offSize;
					this.raf.skipBytes(skip);
					int offset = this.readOffset(offSize);
					this.raf.skipBytes(offset - 1);
				}
			}

			// Global Subrs INDEX を解析
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Global Subrs INDEX count: " + count);
				}
				int offSize = this.readOffSize();
				++count;
				this.globalSubrOffsets = new int[count];
				for (int i = 0; i < count; ++i) {
					this.globalSubrOffsets[i] = this.readOffset(offSize);
				}
				int globalSubrsIndexOffset = (int) this.raf.getFilePointer() - 1;
				for (int i = 0; i < count; ++i) {
					this.globalSubrOffsets[i] += globalSubrsIndexOffset;
				}
			}

			// CharStrings を解析
			this.raf.seek(de.getOffset() + charStringsOffset);
			int charCount = this.readCard16();
			{
				if (DEBUG) {
					System.err.println("CharStrings INDEX count:" + charCount);
				}
				int offSize = this.readOffSize();
				this.charStringOffsets = new int[charCount];
				for (int i = 0; i < charCount; ++i) {
					this.charStringOffsets[i] = this.readOffset(offSize);
				}
				int charStringsIndexOffset = (int) this.raf.getFilePointer() + offSize - 1;
				for (int i = 0; i < charCount; ++i) {
					this.charStringOffsets[i] += charStringsIndexOffset;
				}
			}

			// Private DICT を解析
			if (DEBUG) {
				System.err.println("Private DICT offset:" + privateOffset);
				System.err.println("Private DICT size:" + privateEnd);
			}
			if (privateEnd != 0) {
				this.raf.seek(de.getOffset() + privateOffset);
				privateEnd += this.raf.getFilePointer();

				int localSubrsOffset = -1;
				while (this.raf.getFilePointer() < privateEnd) {
					int op = this.scanOperator();
					if (DEBUG) {
						System.err.println("[Private DICT]:" + Integer.toHexString(op) + ";" + this.stack.size());
					}
					switch (op) {
					case SUBRS:
						localSubrsOffset = this.stack.get(0).intValue() + privateOffset;
						break;
					}
					this.stack.clear();
				}

				if (localSubrsOffset != -1) {
					// Local Subrs INDEX を解析
					this.raf.seek(de.getOffset() + localSubrsOffset);
					int count = this.readCard16();
					if (DEBUG) {
						System.err.println("Local Subrs INDEX count: " + count);
					}
					int offSize = this.readOffSize();
					++count;
					int[] localSubrOffsets = new int[count];
					for (int i = 0; i < count; ++i) {
						localSubrOffsets[i] = this.readOffset(offSize);
					}
					int localSubrsIndexOffset = (int) this.raf.getFilePointer() - 1;
					for (int i = 0; i < count; ++i) {
						localSubrOffsets[i] += localSubrsIndexOffset;
					}
					this.localSubrOffsets = new int[charCount][];
					for (int i = 0; i < charCount; ++i) {
						this.localSubrOffsets[i] = localSubrOffsets;
					}
				}
			}

			if (fontDictIndexOffset != -1) {
				// Font DICT INDEX を解析
				this.raf.seek(de.getOffset() + fontDictIndexOffset);
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Font DICT INDEX count: " + count);
				}
				++count;
				int offSize = this.readOffSize();
				int[] offsets = new int[count];
				for (int i = 0; i < count; ++i) {
					offsets[i] = this.readOffset(offSize);
				}
				int[] privateOffsets = new int[count - 1];
				int[] privateLengths = new int[count - 1];
				for (int i = 0; i < count - 1; ++i) {
					int end = (int) this.raf.getFilePointer() + offsets[i + 1] - offsets[i];
					while (this.raf.getFilePointer() < end) {
						int op = this.scanOperator();
						if (DEBUG) {
							System.err.println(
									"[Font DICT]:" + i + "/" + Integer.toHexString(op) + ";" + this.stack.size());
						}
						switch (op) {
						case PRIVATE:
							privateLengths[i] = this.stack.get(0).intValue();
							privateOffsets[i] = this.stack.get(1).intValue();
							if (DEBUG) {
								System.err.println("PRIVATE length/offset: " + i + "/" + privateLengths[i] + "/"
										+ privateOffsets[i]);
							}
							break;
						}
						this.stack.clear();
					}
				}

				// Private DICT を解析
				int[] subrsOffsets = new int[count - 1];
				for (int i = 0; i < count - 1; ++i) {
					int end = privateLengths[i];
					if (end <= 0) {
						continue;
					}
					int fdPrivateOffset = privateOffsets[i];
					this.raf.seek(de.getOffset() + fdPrivateOffset);
					end += this.raf.getFilePointer();
					while (this.raf.getFilePointer() < end) {
						int op = this.scanOperator();
						if (DEBUG) {
							System.err.println(
									"[Private DICT]:" + i + "/" + Integer.toHexString(op) + ";" + this.stack.size());
						}
						switch (op) {
						case SUBRS:
							subrsOffsets[i] = fdPrivateOffset + this.stack.get(0).intValue();
							if (DEBUG) {
								System.err.println(
										"FD Subrs offset: " + i + "/" + fdPrivateOffset + "+" + this.stack.get(0));
							}
							break;
						}
						this.stack.clear();
					}
				}

				// Subrs を解析
				int[][] fdLocalSubrOffsets = new int[count - 1][];
				for (int i = 0; i < count - 1; ++i) {
					if (subrsOffsets[i] == 0) {
						continue;
					}
					this.raf.seek(de.getOffset() + subrsOffsets[i]);
					int subrCount = this.readCard16() + 1;
					if (DEBUG) {
						System.err.println("FD Local Subrs INDEX count: " + i + "/" + subrCount);
					}
					int subrOffSize = this.readOffSize();
					if (subrOffSize == 0) {
						continue;
					}
					int[] subrOffsets = new int[subrCount];
					for (int j = 0; j < subrCount; ++j) {
						subrOffsets[j] = this.readOffset(subrOffSize);
					}
					int fdLocalSubrsIndexOffset = (int) this.raf.getFilePointer() - 1;
					for (int j = 0; j < subrCount; ++j) {
						subrOffsets[j] += fdLocalSubrsIndexOffset;
					}
					fdLocalSubrOffsets[i] = subrOffsets;
				}

				// FD Select を解析
				{
					this.localSubrOffsets = new int[charCount][];
					this.raf.seek(de.getOffset() + fdSelectOffset);
					int format = this.readCard8();
					if (DEBUG) {
						System.err.println("FD Select format type: " + format);
					}
					switch (format) {
					case 0:
						for (int i = 0; i < charCount; ++i) {
							int fdIx = this.readCard8();
							this.localSubrOffsets[i] = fdLocalSubrOffsets[fdIx];
						}
						break;
					case 3:
						int nRanges = this.readCard16();
						int first = this.readCard16();
						for (int i = 0; i < nRanges; ++i) {
							int fdIx = this.readCard8();
							int last = this.readCard16();
							for (int j = first; j < last; ++j) {
								this.localSubrOffsets[j] = fdLocalSubrOffsets[fdIx];
							}
							first = last;
						}
						break;
					default:
						throw new IOException("Unsupported FD Select format: " + format);
					}
				}
			}
		}
	}

	public int getType() {
		return CFF;
	}

	public void init() {
		this.charString = new Type2CharString(this.raf);
	}

	public Glyph getGlyph(int ix, short upm) {
		int[] localSubrOffsets = this.localSubrOffsets == null ? null : this.localSubrOffsets[ix];
		return this.charString.getGlyph(ix, this.charStringOffsets[ix], upm, this.globalSubrOffsets, localSubrOffsets);
	}

	private byte[] readHeader() throws IOException {
		byte[] header = new byte[4];
		for (int i = 0; i < 4; ++i) {
			header[i] = this.raf.readByte();
		}
		return header;
	}

	private int readCard8() throws IOException {
		return this.raf.read();
	}

	private int readCard16() throws IOException {
		return (int) this.raf.readShort() & 0xFFFF;
	}

	private int readOffSize() throws IOException {
		byte offSize = this.raf.readByte();
		if (offSize < 1 || offSize > 4) {
			// return 0;
			throw new IOException("OffSizeは1から4までです。:" + offSize);
		}
		return offSize;
	}

	private int readOffset(int size) throws IOException {
		int offset = 0;
		for (int i = 0; i < size; ++i) {
			offset <<= 8;
			offset |= this.raf.read();
		}
		return offset;
	}

	private int scanOperator() throws IOException {
		for (;;) {
			byte type = this.nextType();
			Number number;
			switch (type) {
			case TYPE_OPERATOR:
				return this.readOperator();
			case TYPE_INTEGER:
				number = this.readInteger();
				break;
			case TYPE_REAL:
				String real = this.readReal();
				try {
					number = Float.parseFloat(real);
				} catch (NumberFormatException e) {
					if (DEBUG) {
						System.err.println("BadNumber: " + real);
					}
					throw e;
				}
				break;
			default:
				throw new IllegalStateException();
			}
			this.stack.push(number);
		}
	}

	private byte nextType() throws IOException {
		this.b0 = this.raf.read();
		if (this.b0 <= 21) {
			return TYPE_OPERATOR;
		}
		if (this.b0 == 30) {
			return TYPE_REAL;
		}
		if (this.b0 >= 28 && this.b0 != 31 && this.b0 != 255) {
			return TYPE_INTEGER;
		}
		throw new IOException("未知のオペランドです。");
	}

	private int readOperator() throws IOException {
		if (this.b0 == -1) {
			if (this.nextType() != TYPE_OPERATOR) {
				throw new IOException("Operatorではありません。");
			}
		}
		int b = this.b0;
		this.b0 = -1;
		if (b == 12) {
			b <<= 8;
			b |= this.raf.read();
		}
		return b;
	}

	private int readInteger() throws IOException {
		if (this.b0 == -1) {
			if (this.nextType() != TYPE_INTEGER) {
				throw new IOException("Integerではありません。");
			}
		}
		int b0 = this.b0;
		this.b0 = -1;
		if (b0 >= 32 && b0 <= 246) {
			return b0 - 139;
		}
		if (b0 >= 247 && b0 <= 250) {
			int b1 = this.raf.read();
			return (b0 - 247) * 256 + b1 + 108;
		}
		if (b0 >= 251 && b0 <= 254) {
			int b1 = this.raf.read();
			return -(b0 - 251) * 256 - b1 - 108;
		}
		if (b0 == 28) {
			short b1 = (short) this.raf.read();
			short b2 = (short) this.raf.read();
			return b1 << 8 | b2;
		}
		if (b0 == 29) {
			int b1 = this.raf.read();
			int b2 = this.raf.read();
			int b3 = this.raf.read();
			int b4 = this.raf.read();
			return b1 << 24 | b2 << 16 | b3 << 8 | b4;
		}
		throw new IOException("不正なIntegerです。:" + b0);
	}

	private String readReal() throws IOException {
		if (this.b0 == -1) {
			if (this.nextType() != TYPE_REAL) {
				throw new IOException("Realではありません。");
			}
		}
		this.buff.setLength(0);
		this.b0 = -1;
		OUTER: for (;;) {
			int b = this.raf.read();
			for (int i = 0; i < 2; ++i) {
				int c = (b >> 4) & 0x0F;
				switch (c) {
				case 0xA:
					this.buff.append('.');
					break;
				case 0xB:
					this.buff.append('E');
					break;
				case 0xC:
					this.buff.append("E-");
					break;
				case 0xD:
					break;
				case 0xE:
					this.buff.append('-');
					break;
				case 0xF:
					break OUTER;
				default:
					this.buff.append(String.valueOf(c));
					break;
				}
				b <<= 4;
			}
		}
		return this.buff.toString();
	}
}
