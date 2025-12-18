package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Single substitution format 1.
 * 
 * @param deltaGlyphID the delta to add to the glyph ID
 * @param coverage     the coverage table
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record SingleSubstFormat1(short deltaGlyphID, Coverage coverage) implements SingleSubst {
	private static final long serialVersionUID = 0L;

	/**
	 * Reads a SingleSubstFormat1 from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset of this subtable
	 * @return a new SingleSubstFormat1
	 * @throws IOException if an I/O error occurs
	 */
	protected static SingleSubstFormat1 read(final RandomAccessFile raf, final int offset) throws IOException {
		final int coverageOffset = raf.readUnsignedShort();
		final short deltaGlyphID = raf.readShort();
		final Coverage coverage;
		synchronized (raf) {
			raf.seek(offset + coverageOffset);
			coverage = Coverage.read(raf);
		}
		return new SingleSubstFormat1(deltaGlyphID, coverage);
	}

	@Override
	public int getFormat() {
		return 1;
	}

	@Override
	public int substitute(final int glyphId) {
		final int i = this.coverage.findGlyph(glyphId);
		if (i > -1) {
			return glyphId + this.deltaGlyphID;
		}
		return glyphId;
	}
}
