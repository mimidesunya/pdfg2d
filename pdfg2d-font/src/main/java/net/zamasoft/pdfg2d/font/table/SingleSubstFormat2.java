package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Single substitution format 2.
 * 
 * @param substitutes the array of substitute glyph IDs
 * @param coverage    the coverage table
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record SingleSubstFormat2(int[] substitutes, Coverage coverage) implements SingleSubst {
	private static final long serialVersionUID = 0L;

	/**
	 * Reads a SingleSubstFormat2 from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset of this subtable
	 * @return a new SingleSubstFormat2
	 * @throws IOException if an I/O error occurs
	 */
	protected static SingleSubstFormat2 read(final RandomAccessFile raf, final int offset) throws IOException {
		final int coverageOffset = raf.readUnsignedShort();
		final int glyphCount = raf.readUnsignedShort();
		final int[] substitutes = new int[glyphCount];
		for (int i = 0; i < glyphCount; i++) {
			substitutes[i] = raf.readUnsignedShort();
		}
		final Coverage coverage;
		synchronized (raf) {
			raf.seek(offset + coverageOffset);
			coverage = Coverage.read(raf);
		}
		return new SingleSubstFormat2(substitutes, coverage);
	}

	@Override
	public int getFormat() {
		return 2;
	}

	@Override
	public int substitute(final int glyphId) {
		final int i = this.coverage.findGlyph(glyphId);
		if (i > -1) {
			return this.substitutes[i];
		}
		return glyphId;
	}
}
