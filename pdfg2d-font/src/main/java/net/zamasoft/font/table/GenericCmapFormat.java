/*

 Copyright 2001  The Apache Software Foundation 

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
package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GenericCmapFormat extends CmapFormat implements Serializable {
	protected Logger LOG = Logger.getLogger(GenericCmapFormat.class.getName());
	private static final long serialVersionUID = 0L;

	protected static final long LEAD_OFFSET = 0xD800 - (0x10000 >> 10);
	protected static final long SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;
	protected int[] glyphIdToCharacterCode;
	protected Map<Integer, Integer> characterCodeToGlyphId;

	/**
	 * This will read the required data from the stream.
	 * 
	 * @param format
	 *            the CMAP this encoding belongs to.
	 * @param numGlyphs
	 *            number of glyphs.
	 * @param data
	 *            The stream to read the data from.
	 * @throws IOException
	 *             If there is an error reading the data.
	 */
	protected GenericCmapFormat(int format, int numGlyphs, RandomAccessFile data) throws IOException {
		if (format < 8) {
			data.readUnsignedShort();
			data.readUnsignedShort();
		} else if (format < 14) {
			// read an other UnsignedShort to read a Fixed32
			data.readUnsignedShort();
			data.readInt();
			data.readInt();
		}
		switch (format) {
		case 0:
			processSubtype0(data);
			break;
		case 2:
			processSubtype2(data, numGlyphs);
			break;
		case 4:
			processSubtype4(data, numGlyphs);
			break;
		case 6:
			processSubtype6(data, numGlyphs);
			break;
		case 8:
			processSubtype8(data, numGlyphs);
			break;
		case 10:
			processSubtype10(data, numGlyphs);
			break;
		case 12:
			processSubtype12(data, numGlyphs);
			break;
		case 13:
			processSubtype13(data, numGlyphs);
			break;
		default:
			throw new IOException("Unknown cmap format:" + format);
		}
	}

	private int[] readUnsignedByteArray(RandomAccessFile raf, int length) throws IOException {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = raf.read();
		}
		return array;
	}

	private int[] readUnsignedShortArray(RandomAccessFile raf, int length) throws IOException {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = raf.readUnsignedShort();
		}
		return array;
	}

	/**
	 * Reads a format 8 subtable.
	 * 
	 * @param raf
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype8(RandomAccessFile raf, int numGlyphs) throws IOException {
		// --- is32 is a 65536 BITS array ( = 8192 BYTES)
		int[] is32 = this.readUnsignedByteArray(raf, 8192);
		long nbGroups = raf.readInt();
		// --- nbGroups shouldn't be greater than 65536
		if (nbGroups > 65536) {
			throw new IOException("CMap ( Subtype8 ) is invalid");
		}
		glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		// -- Read all sub header
		for (long i = 0; i < nbGroups; ++i) {
			long firstCode = raf.readInt();
			long endCode = raf.readInt();
			long startGlyph = raf.readInt();
			// -- process simple validation
			if (firstCode > endCode || 0 > firstCode) {
				throw new IOException("Range invalid");
			}
			for (long j = firstCode; j <= endCode; ++j) {
				// -- Convert the Character code in decimal
				if (j > Integer.MAX_VALUE) {
					throw new IOException("[Sub Format 8] Invalid Character code");
				}
				int currentCharCode;
				if ((is32[(int) j / 8] & (1 << ((int) j % 8))) == 0) {
					currentCharCode = (int) j;
				} else {
					// the character code uses a 32bits format
					// convert it in decimal : see http://www.unicode.org/faq//utf_bom.html#utf16-4
					long lead = LEAD_OFFSET + (j >> 10);
					long trail = 0xDC00 + (j & 0x3FF);
					long codepoint = (lead << 10) + trail + SURROGATE_OFFSET;
					if (codepoint > Integer.MAX_VALUE) {
						throw new IOException("[Sub Format 8] Invalid Character code");
					}
					currentCharCode = (int) codepoint;
				}
				long glyphIndex = startGlyph + (j - firstCode);
				if (glyphIndex > numGlyphs || glyphIndex > Integer.MAX_VALUE) {
					throw new IOException("CMap contains an invalid glyph index");
				}
				glyphIdToCharacterCode[(int) glyphIndex] = currentCharCode;
				characterCodeToGlyphId.put(currentCharCode, (int) glyphIndex);
			}
		}
	}

	/**
	 * Reads a format 10 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype10(RandomAccessFile data, int numGlyphs) throws IOException {
		long startCode = data.readInt();
		long numChars = data.readInt();
		if (numChars > Integer.MAX_VALUE) {
			throw new IOException("Invalid number of Characters");
		}
		if (startCode < 0 || startCode > 0x0010FFFF || (startCode + numChars) > 0x0010FFFF
				|| ((startCode + numChars) >= 0x0000D800 && (startCode + numChars) <= 0x0000DFFF)) {
			throw new IOException("Invalid Characters codes");
		}
	}

	/**
	 * Reads a format 12 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype12(RandomAccessFile data, int numGlyphs) throws IOException {
		long nbGroups = data.readInt();
		glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		for (long i = 0; i < nbGroups; ++i) {
			long firstCode = data.readInt();
			long endCode = data.readInt();
			long startGlyph = data.readInt();
			if (firstCode < 0 || firstCode > 0x0010FFFF || firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF) {
				throw new IOException("Invalid characters codes");
			}
			if (endCode > 0 && endCode < firstCode || endCode > 0x0010FFFF
					|| endCode >= 0x0000D800 && endCode <= 0x0000DFFF) {
				throw new IOException("Invalid characters codes");
			}
			for (long j = 0; j <= endCode - firstCode; ++j) {
				long glyphIndex = startGlyph + j;
				if (glyphIndex >= numGlyphs) {
					LOG.log(Level.WARNING, "Format 12 cmap contains an invalid glyph index");
					break;
				}
				if (firstCode + j > 0x10FFFF) {
					LOG.log(Level.WARNING, "Format 12 cmap contains character beyond UCS-4");
				}
				glyphIdToCharacterCode[(int) glyphIndex] = (int) (firstCode + j);
				characterCodeToGlyphId.put((int) (firstCode + j), (int) glyphIndex);
			}
		}
	}

	/**
	 * Reads a format 13 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype13(RandomAccessFile data, int numGlyphs) throws IOException {
		long nbGroups = data.readInt();
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		for (long i = 0; i < nbGroups; ++i) {
			long firstCode = data.readInt();
			long endCode = data.readInt();
			long glyphId = data.readInt();
			if (glyphId > numGlyphs) {
				LOG.log(Level.WARNING, "Format 13 cmap contains an invalid glyph index");
				break;
			}
			if (firstCode < 0 || firstCode > 0x0010FFFF || (firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF)) {
				throw new IOException("Invalid Characters codes");
			}
			if ((endCode > 0 && endCode < firstCode) || endCode > 0x0010FFFF
					|| (endCode >= 0x0000D800 && endCode <= 0x0000DFFF)) {
				throw new IOException("Invalid Characters codes");
			}
			for (long j = 0; j <= endCode - firstCode; ++j) {
				if (firstCode + j > Integer.MAX_VALUE) {
					throw new IOException("Character Code greater than Integer.MAX_VALUE");
				}
				if (firstCode + j > 0x10FFFF) {
					LOG.log(Level.WARNING, "Format 13 cmap contains character beyond UCS-4");
				}
				glyphIdToCharacterCode[(int) glyphId] = (int) (firstCode + j);
				characterCodeToGlyphId.put((int) (firstCode + j), (int) glyphId);
			}
		}
	}

	/**
	 * Reads a format 6 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype6(RandomAccessFile data, int numGlyphs) throws IOException {
		int firstCode = data.readUnsignedShort();
		int entryCount = data.readUnsignedShort();
		// skip emtpy tables
		if (entryCount == 0) {
			return;
		}
		Map<Integer, Integer> tmpGlyphToChar = new HashMap<Integer, Integer>(numGlyphs);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		int[] glyphIdArray = this.readUnsignedShortArray(data, entryCount);
		int maxGlyphId = 0;
		for (int i = 0; i < entryCount; i++) {
			maxGlyphId = Math.max(maxGlyphId, glyphIdArray[i]);
			tmpGlyphToChar.put(glyphIdArray[i], firstCode + i);
			characterCodeToGlyphId.put(firstCode + i, glyphIdArray[i]);
		}
		buildGlyphIdToCharacterCodeLookup(tmpGlyphToChar, maxGlyphId);
	}

	/**
	 * Reads a format 4 subtable.
	 * 
	 * @param raf
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype4(RandomAccessFile raf, int numGlyphs) throws IOException {
		int segCountX2 = raf.readUnsignedShort();
		int segCount = segCountX2 / 2;
		raf.readUnsignedShort();
		raf.readUnsignedShort();
		raf.readUnsignedShort();
		int[] endCount = this.readUnsignedShortArray(raf, segCount);
		raf.readUnsignedShort();
		int[] startCount = this.readUnsignedShortArray(raf, segCount);
		int[] idDelta = this.readUnsignedShortArray(raf, segCount);
		int[] idRangeOffset = this.readUnsignedShortArray(raf, segCount);
		Map<Integer, Integer> tmpGlyphToChar = new HashMap<Integer, Integer>(numGlyphs);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		int maxGlyphId = 0;
		long currentPosition = raf.getFilePointer();
		for (int i = 0; i < segCount; i++) {
			int start = startCount[i];
			int end = endCount[i];
			int delta = idDelta[i];
			int rangeOffset = idRangeOffset[i];
			if (start != 65535 && end != 65535) {
				for (int j = start; j <= end; j++) {
					if (rangeOffset == 0) {
						int glyphid = (j + delta) & 0xFFFF;
						maxGlyphId = Math.max(glyphid, maxGlyphId);
						tmpGlyphToChar.put(glyphid, j);
						characterCodeToGlyphId.put(j, glyphid);
					} else {
						long glyphOffset = currentPosition + ((rangeOffset / 2) + (j - start) + (i - segCount)) * 2;
						raf.seek(glyphOffset);
						int glyphIndex = raf.readUnsignedShort();
						if (glyphIndex != 0) {
							glyphIndex = (glyphIndex + delta) & 0xFFFF;
							if (!tmpGlyphToChar.containsKey(glyphIndex)) {
								maxGlyphId = Math.max(glyphIndex, maxGlyphId);
								tmpGlyphToChar.put(glyphIndex, j);
								characterCodeToGlyphId.put(j, glyphIndex);
							}
						}
					}
				}
			}
		}
		/*
		 * this is the final result key=glyphId, value is character codes Create an
		 * array that contains MAX(GlyphIds) element, or -1
		 */
		if (tmpGlyphToChar.isEmpty()) {
			LOG.log(Level.WARNING, "cmap format 4 subtable is empty");
			return;
		}
		buildGlyphIdToCharacterCodeLookup(tmpGlyphToChar, maxGlyphId);
	}

	private void buildGlyphIdToCharacterCodeLookup(Map<Integer, Integer> tmpGlyphToChar, int maxGlyphId) {
		glyphIdToCharacterCode = newGlyphIdToCharacterCode(maxGlyphId + 1);
		for (Entry<Integer, Integer> entry : tmpGlyphToChar.entrySet()) {
			// link the glyphId with the right character code
			glyphIdToCharacterCode[entry.getKey()] = entry.getValue();
		}
	}

	/**
	 * Read a format 2 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype2(RandomAccessFile data, int numGlyphs) throws IOException {
		int[] subHeaderKeys = new int[256];
		// ---- keep the Max Index of the SubHeader array to know its length
		int maxSubHeaderIndex = 0;
		for (int i = 0; i < 256; i++) {
			subHeaderKeys[i] = data.readUnsignedShort();
			maxSubHeaderIndex = Math.max(maxSubHeaderIndex, subHeaderKeys[i] / 8);
		}
		// ---- Read all SubHeaders to avoid useless seek on DataSource
		SubHeader[] subHeaders = new SubHeader[maxSubHeaderIndex + 1];
		for (int i = 0; i <= maxSubHeaderIndex; ++i) {
			int firstCode = data.readUnsignedShort();
			int entryCount = data.readUnsignedShort();
			short idDelta = data.readShort();
			int idRangeOffset = data.readUnsignedShort() - (maxSubHeaderIndex + 1 - i - 1) * 8 - 2;
			subHeaders[i] = new SubHeader(firstCode, entryCount, idDelta, idRangeOffset);
		}
		long startGlyphIndexOffset = data.getFilePointer();
		glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(numGlyphs);
		for (int i = 0; i <= maxSubHeaderIndex; ++i) {
			SubHeader sh = subHeaders[i];
			int firstCode = sh.getFirstCode();
			int idRangeOffset = sh.getIdRangeOffset();
			int idDelta = sh.getIdDelta();
			int entryCount = sh.getEntryCount();
			data.seek(startGlyphIndexOffset + idRangeOffset);
			for (int j = 0; j < entryCount; ++j) {
				// ---- compute the Character Code
				int charCode = i;
				charCode = (charCode << 8) + (firstCode + j);
				// ---- Go to the CharacterCOde position in the Sub Array
				// of the glyphIndexArray
				// glyphIndexArray contains Unsigned Short so add (j * 2) bytes
				// at the index position
				int p = data.readUnsignedShort();
				// ---- compute the glyphIndex
				if (p > 0) {
					p = (p + idDelta) % 65536;
				}

				if (p >= numGlyphs) {
					LOG.log(Level.WARNING,
							"glyphId " + p + " for charcode " + charCode + " ignored, numGlyphs is " + numGlyphs);
					continue;
				}

				glyphIdToCharacterCode[p] = charCode;
				characterCodeToGlyphId.put(charCode, p);
			}
		}
	}

	/**
	 * Initialize the CMapEntry when it is a subtype 0.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype0(RandomAccessFile data) throws IOException {
		byte[] glyphMapping = new byte[256];
		data.readFully(glyphMapping);
		glyphIdToCharacterCode = newGlyphIdToCharacterCode(256);
		characterCodeToGlyphId = new HashMap<Integer, Integer>(glyphMapping.length);
		for (int i = 0; i < glyphMapping.length; i++) {
			int glyphIndex = (glyphMapping[i] + 256) % 256;
			glyphIdToCharacterCode[glyphIndex] = i;
			characterCodeToGlyphId.put(i, glyphIndex);
		}
	}

	/**
	 * Workaround for the fact that glyphIdToCharacterCode doesn't distinguish
	 * between missing character codes and code 0.
	 */
	private int[] newGlyphIdToCharacterCode(int size) {
		int[] gidToCode = new int[size];
		Arrays.fill(gidToCode, -1);
		return gidToCode;
	}

	/**
	 * Returns the GlyphId linked with the given character code.
	 *
	 * @param characterCode
	 *            the given character code to be mapped
	 * @return glyphId the corresponding glyph id for the given character code
	 */
	public int mapCharCode(int characterCode) {
		Integer glyphId = characterCodeToGlyphId.get(characterCode);
		return glyphId == null ? 0 : glyphId;
	}

	/**
	 * Returns the character code for the given GID, or null if there is none.
	 *
	 * @param gid
	 *            glyph id
	 * @return character code
	 */
	public Integer getCharacterCode(int gid) {
		if (gid < 0 || gid >= glyphIdToCharacterCode.length) {
			return null;
		}
		// workaround for the fact that glyphIdToCharacterCode doesn't distinguish
		// between
		// missing character codes and code 0.
		int code = glyphIdToCharacterCode[gid];
		if (code == -1) {
			return null;
		}
		return code;
	}

	/**
	 * 
	 * Class used to manage CMap - Format 2.
	 * 
	 */
	private class SubHeader {
		private final int firstCode;
		private final int entryCount;
		/**
		 * used to compute the GlyphIndex : P = glyphIndexArray.SubArray[pos] GlyphIndex
		 * = P + idDelta % 65536.
		 */
		private final short idDelta;
		/**
		 * Number of bytes to skip to reach the firstCode in the glyphIndexArray.
		 */
		private final int idRangeOffset;

		private SubHeader(int firstCodeValue, int entryCountValue, short idDeltaValue, int idRangeOffsetValue) {
			firstCode = firstCodeValue;
			entryCount = entryCountValue;
			idDelta = idDeltaValue;
			idRangeOffset = idRangeOffsetValue;
		}

		/**
		 * @return the firstCode
		 */
		private int getFirstCode() {
			return firstCode;
		}

		/**
		 * @return the entryCount
		 */
		private int getEntryCount() {
			return entryCount;
		}

		/**
		 * @return the idDelta
		 */
		private short getIdDelta() {
			return idDelta;
		}

		/**
		 * @return the idRangeOffset
		 */
		private int getIdRangeOffset() {
			return idRangeOffset;
		}
	}
}
