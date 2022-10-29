package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UvsCmapFormat extends CmapFormat {
	protected Map<Long, Integer> codeToGlyphId;
	
	protected Set<Integer> varSelectors;
	
	protected UvsCmapFormat(int format, int numGlyphs, RandomAccessFile data) throws IOException {
		data.readInt();
		this.processSubtype14(data, numGlyphs);
	}

	/**
	 * Reads a format 14 subtable.
	 * 
	 * @param data
	 *            the data stream of the to be parsed ttf font
	 * @param numGlyphs
	 *            number of glyphs to be read
	 * @throws IOException
	 *             If there is an error parsing the true type font.
	 */
	protected void processSubtype14(RandomAccessFile data, int numGlyphs) throws IOException {
		long start = data.getFilePointer() - 6;
		long numVarSelectorRecords = data.readInt();
		this.codeToGlyphId = new HashMap<Long, Integer>();
		this.varSelectors = new HashSet<Integer>();
		for (int i = 0; i < numVarSelectorRecords; ++i) {
			int varSelector = 0xFFFFFF & ((data.readShort() << 8) | (0xFF & data.readByte()));
			this.varSelectors.add(varSelector);
			@SuppressWarnings("unused")
			long defaultUVSOffset  = data.readInt();
			long nonDefaultUVSOffset  = data.readInt();
			long pos = data.getFilePointer();
//			if (defaultUVSOffset != 0) {
//				data.seek(start + defaultUVSOffset);
//				long numUnicodeValueRanges = data.readInt();
//				for (int j = 0; j < numUnicodeValueRanges; ++j) {
//					int startUnicodeValue = 0xFFFFFF & ((data.readShort() << 8) | (0xFF & data.readByte()));
//					int additionalCount = 0xFF & data.readByte();
//				}
//			}
			if (nonDefaultUVSOffset != 0) {
				data.seek(start + nonDefaultUVSOffset);
				long numUVSMappings = data.readInt();
				for (int j = 0; j < numUVSMappings; ++j) {
					long unicodeValue = 0xFFFFFF & ((data.readShort() << 8) | (0xFF & data.readByte()));
					int glyphId = 0xFFFF & data.readShort();
					this.codeToGlyphId.put((unicodeValue << 32L) | varSelector, glyphId);
				}
			}
			data.seek(pos);
		}
		this.codeToGlyphId = Collections.unmodifiableMap(this.codeToGlyphId);
		this.varSelectors = Collections.unmodifiableSet(this.varSelectors);
	}
	
	public int mapCharCode(int c, int vs) {
		Integer glyphId = this.codeToGlyphId.get(((long)c << 32L) | (long)vs);
		return glyphId == null ? 0 : glyphId;
	}
	
	public boolean isVarSelector(int c) {
		return this.varSelectors.contains(c);
	}
}
