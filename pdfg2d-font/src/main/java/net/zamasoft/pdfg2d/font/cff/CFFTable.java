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
package net.zamasoft.pdfg2d.font.cff;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.zamasoft.pdfg2d.font.Glyph;
import net.zamasoft.pdfg2d.font.table.DirectoryEntry;
import net.zamasoft.pdfg2d.font.table.Table;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class CFFTable implements Table {

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

	private final CFFStack stack = new CFFStack();

	/**
	 * CharString offsets for each character.
	 */
	private final int[] charStringOffsets;

	/**
	 * Global subroutine offsets.
	 */
	private final int[] globalSubrOffsets;

	/**
	 * Local subroutine offsets for each character.
	 */
	private final int[][] localSubrOffsets;

	private int b0 = -1;

	private final StringBuilder buff = new StringBuilder();

	private final Type2CharString charString;

	public CFFTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this.raf = raf;
		int[] gso = null;
		int[] cso = null;
		int[][] lso = null;
		synchronized (this.raf) {
			// Skip header
			{
				this.raf.seek(de.offset());
				byte[] header = this.readHeader();
				if (DEBUG) {
					System.err.println("Major Version: " + header[0]);
					System.err.println("Minor Version: " + header[1]);
				}
				int skip = header[2];
				this.raf.skipBytes(skip - 4);
			}

			// Skip Name INDEX
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Name INDEX count: " + count);
				}
				if (count != 1) {
					throw new IOException("Name INDEX is not 1: " + count);
				}
				int offSize = this.readOffSize();
				this.raf.skipBytes(offSize);
				int offset = this.readOffset(offSize);
				this.raf.skipBytes(offset - 1);
			}

			// Parse Top DICT INDEX
			int dictEnd;
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Top DICT INDEX count: " + count);
				}
				if (count != 1) {
					throw new IOException("Top DICT INDEX is not 1: " + count);
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

			// Parse first DICT Data
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
				throw new IOException("CharStrings not found in DICT.");
			}

			// Skip String INDEX
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

			// Parse Global Subrs INDEX
			{
				int count = this.readCard16();
				if (DEBUG) {
					System.err.println("Global Subrs INDEX count: " + count);
				}
				int offSize = this.readOffSize();
				++count;
				gso = new int[count];
				for (int i = 0; i < count; ++i) {
					gso[i] = this.readOffset(offSize);
				}
				int globalSubrsIndexOffset = (int) this.raf.getFilePointer() - 1;
				for (int i = 0; i < count; ++i) {
					gso[i] += globalSubrsIndexOffset;
				}
			}

			// Parse CharStrings
			this.raf.seek(de.offset() + charStringsOffset);
			int charCount = this.readCard16();
			{
				if (DEBUG) {
					System.err.println("CharStrings INDEX count:" + charCount);
				}
				int offSize = this.readOffSize();
				cso = new int[charCount];
				for (int i = 0; i < charCount; ++i) {
					cso[i] = this.readOffset(offSize);
				}
				int charStringsIndexOffset = (int) this.raf.getFilePointer() + offSize - 1;
				for (int i = 0; i < charCount; ++i) {
					cso[i] += charStringsIndexOffset;
				}
			}

			// Parse Private DICT
			if (DEBUG) {
				System.err.println("Private DICT offset:" + privateOffset);
				System.err.println("Private DICT size:" + privateEnd);
			}
			if (privateEnd != 0) {
				this.raf.seek(de.offset() + privateOffset);
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
					// Parse Local Subrs INDEX
					this.raf.seek(de.offset() + localSubrsOffset);
					int count = this.readCard16();
					if (DEBUG) {
						System.err.println("Local Subrs INDEX count: " + count);
					}
					int offSize = this.readOffSize();
					++count;
					int[] localOffsets = new int[count];
					for (int i = 0; i < count; ++i) {
						localOffsets[i] = this.readOffset(offSize);
					}
					int localSubrsIndexOffset = (int) this.raf.getFilePointer() - 1;
					for (int i = 0; i < count; ++i) {
						localOffsets[i] += localSubrsIndexOffset;
					}
					lso = new int[charCount][];
					for (int i = 0; i < charCount; ++i) {
						lso[i] = localOffsets;
					}
				}
			}

			if (fontDictIndexOffset != -1) {
				// Parse Font DICT INDEX
				this.raf.seek(de.offset() + fontDictIndexOffset);
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

				// Parse Private DICT
				int[] subrsOffsets = new int[count - 1];
				for (int i = 0; i < count - 1; ++i) {
					int end = privateLengths[i];
					if (end <= 0) {
						continue;
					}
					int fdPrivateOffset = privateOffsets[i];
					this.raf.seek(de.offset() + fdPrivateOffset);
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

				// Parse Subrs
				int[][] fdLocalSubrOffsets = new int[count - 1][];
				for (int i = 0; i < count - 1; ++i) {
					if (subrsOffsets[i] == 0) {
						continue;
					}
					this.raf.seek(de.offset() + subrsOffsets[i]);
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

				// Parse FD Select
				{
					lso = new int[charCount][];
					this.raf.seek(de.offset() + fdSelectOffset);
					int format = this.readCard8();
					if (DEBUG) {
						System.err.println("FD Select format type: " + format);
					}
					switch (format) {
						case 0:
							for (int i = 0; i < charCount; ++i) {
								int fdIx = this.readCard8();
								lso[i] = fdLocalSubrOffsets[fdIx];
							}
							break;
						case 3:
							int nRanges = this.readCard16();
							int first = this.readCard16();
							for (int i = 0; i < nRanges; ++i) {
								int fdIx = this.readCard8();
								int last = this.readCard16();
								for (int j = first; j < last; ++j) {
									lso[j] = fdLocalSubrOffsets[fdIx];
								}
								first = last;
							}
							break;
						default:
							throw new IOException("Unsupported FD Select format: " + format);
					}
				}
			}
			this.charString = new Type2CharString(this.raf);
		}
		this.globalSubrOffsets = gso;
		this.charStringOffsets = cso;
		this.localSubrOffsets = lso;
	}

	public int getType() {
		return CFF;
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
			throw new IOException("OffSize must be 1-4: " + offSize);
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
		throw new IOException("Unknown operand.");
	}

	private int readOperator() throws IOException {
		if (this.b0 == -1) {
			if (this.nextType() != TYPE_OPERATOR) {
				throw new IOException("Not an Operator.");
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
				throw new IOException("Not an Integer.");
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
		throw new IOException("Invalid Integer: " + b0);
	}

	private String readReal() throws IOException {
		if (this.b0 == -1) {
			if (this.nextType() != TYPE_REAL) {
				throw new IOException("Not a Real.");
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
