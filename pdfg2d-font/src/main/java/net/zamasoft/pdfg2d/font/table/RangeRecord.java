package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a range record in coverage tables.
 * Coverage Index (GlyphID) = StartCoverageIndex + GlyphID - Start GlyphID
 * 
 * @param start              the start glyph ID
 * @param end                the end glyph ID
 * @param startCoverageIndex the start coverage index
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record RangeRecord(int start, int end, int startCoverageIndex) {

	/**
	 * Reads a RangeRecord from the given RandomAccessFile.
	 * 
	 * @param raf the file to read from
	 * @return a new RangeRecord
	 * @throws IOException if an I/O error occurs
	 */
	public static RangeRecord read(RandomAccessFile raf) throws IOException {
		int start = raf.readUnsignedShort();
		int end = raf.readUnsignedShort();
		int startCoverageIndex = raf.readUnsignedShort();
		return new RangeRecord(start, end, startCoverageIndex);
	}

	/**
	 * Checks if the given glyph ID is within this range.
	 * 
	 * @param glyphId the glyph ID to check
	 * @return true if the glyph ID is in range
	 */
	public boolean isInRange(int glyphId) {
		return (start <= glyphId && glyphId <= end);
	}

	/**
	 * Gets the coverage index for the given glyph ID.
	 * 
	 * @param glyphId the glyph ID
	 * @return the coverage index, or -1 if not in range
	 */
	public int getCoverageIndex(int glyphId) {
		if (isInRange(glyphId)) {
			return startCoverageIndex + glyphId - start;
		}
		return -1;
	}
}
