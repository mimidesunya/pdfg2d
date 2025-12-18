package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a ligature in an OpenType font.
 * 
 * @param ligGlyph   the glyph ID of the ligature
 * @param compCount  the number of components in the ligature
 * @param components array of component glyph IDs (excluding the first
 *                   component)
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Ligature(int ligGlyph, int compCount, int[] components) {

	/**
	 * Creates a new Ligature by reading from the given file.
	 * 
	 * @param raf the file to read from
	 * @throws IOException if an I/O error occurs
	 */
	public static Ligature read(final RandomAccessFile raf) throws IOException {
		final int ligGlyph = raf.readUnsignedShort();
		final int compCount = raf.readUnsignedShort();
		final int[] components = new int[compCount - 1];
		for (int i = 0; i < compCount - 1; i++) {
			components[i] = raf.readUnsignedShort();
		}
		return new Ligature(ligGlyph, compCount, components);
	}

	public int getGlyphCount() {
		return this.compCount;
	}

	public int getGlyphId(final int i) {
		return (i == 0) ? this.ligGlyph : this.components[i - 1];
	}
}
