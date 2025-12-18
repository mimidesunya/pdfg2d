package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Ligature substitution format 1.
 * 
 * @param ligatureSets the array of ligature sets
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record LigatureSubstFormat1(LigatureSet[] ligatureSets) implements LigatureSubst {

	/**
	 * Reads a LigatureSubstFormat1 from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset of this subtable
	 * @return a new LigatureSubstFormat1
	 * @throws IOException if an I/O error occurs
	 */
	protected static LigatureSubstFormat1 read(final RandomAccessFile raf, final int offset) throws IOException {
		raf.readUnsignedShort(); // coverageOffset
		final int ligSetCount = raf.readUnsignedShort();
		final int[] ligatureSetOffsets = new int[ligSetCount];
		for (int i = 0; i < ligSetCount; i++) {
			ligatureSetOffsets[i] = raf.readUnsignedShort();
		}
		final LigatureSet[] ligatureSets = new LigatureSet[ligSetCount];
		synchronized (raf) {
			for (int i = 0; i < ligSetCount; i++) {
				raf.seek(offset + ligatureSetOffsets[i]);
				ligatureSets[i] = new LigatureSet(raf, offset + ligatureSetOffsets[i]);
			}
		}
		return new LigatureSubstFormat1(ligatureSets);
	}

	@Override
	public int getFormat() {
		return 1;
	}

	public int getLigatureSetCount() {
		return this.ligatureSets.length;
	}

	public LigatureSet getLigatureSet(final int i) {
		return this.ligatureSets[i];
	}
}
