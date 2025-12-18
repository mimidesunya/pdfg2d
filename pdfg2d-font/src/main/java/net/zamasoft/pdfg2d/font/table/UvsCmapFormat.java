package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * UVS (Unicode Variation Sequence) cmap format.
 * 
 * @param codeToGlyphId mapping from (unicodeValue << 32L) | varSelector to
 *                      glyph
 *                      ID
 * @param varSelectors  set of variation selectors
 */
public record UvsCmapFormat(Map<Long, Integer> codeToGlyphId, Set<Integer> varSelectors) implements CmapFormat {

	/**
	 * Reads a format 14 subtable.
	 * 
	 * @param data      the data stream of the to be parsed ttf font
	 * @param numGlyphs number of glyphs to be read
	 * @return a new UvsCmapFormat
	 * @throws IOException If there is an error parsing the true type font.
	 */
	public static UvsCmapFormat read(final RandomAccessFile data, final int numGlyphs) throws IOException {
		data.readInt(); // length
		final long start = data.getFilePointer() - 6;
		final long numVarSelectorRecords = data.readInt();
		final Map<Long, Integer> codeToGlyphId = new HashMap<>();
		final Set<Integer> varSelectors = new HashSet<>();
		for (int i = 0; i < numVarSelectorRecords; ++i) {
			final int varSelector = 0xFFFFFF & ((data.readShort() << 8) | (0xFF & data.readByte()));
			varSelectors.add(varSelector);
			@SuppressWarnings("unused")
			final long defaultUVSOffset = data.readInt();
			final long nonDefaultUVSOffset = data.readInt();
			final long pos = data.getFilePointer();
			if (nonDefaultUVSOffset != 0) {
				data.seek(start + nonDefaultUVSOffset);
				final long numUVSMappings = data.readInt();
				for (int j = 0; j < numUVSMappings; ++j) {
					final long unicodeValue = 0xFFFFFF & ((data.readShort() << 8) | (0xFF & data.readByte()));
					final int glyphId = 0xFFFF & data.readShort();
					codeToGlyphId.put((unicodeValue << 32L) | varSelector, glyphId);
				}
			}
			data.seek(pos);
		}
		return new UvsCmapFormat(Collections.unmodifiableMap(codeToGlyphId), Collections.unmodifiableSet(varSelectors));
	}

	@Override
	public int mapCharCode(final int c) {
		return 0;
	}

	@Override
	public int mapCharCode(final int c, final int vs) {
		final Integer glyphId = this.codeToGlyphId.get(((long) c << 32L) | (long) vs);
		return glyphId == null ? 0 : glyphId;
	}

	public boolean isVarSelector(final int c) {
		return this.varSelectors.contains(c);
	}
}
