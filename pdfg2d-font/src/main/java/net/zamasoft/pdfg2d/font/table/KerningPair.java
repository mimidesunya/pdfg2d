package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a kerning pair in a font.
 * 
 * @param left  the left glyph ID
 * @param right the right glyph ID
 * @param value the kerning value
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record KerningPair(int left, int right, short value) {

	/**
	 * Reads a KerningPair from the given RandomAccessFile.
	 * 
	 * @param raf the file to read from
	 * @return a new KerningPair
	 * @throws IOException if an I/O error occurs
	 */
	public static KerningPair read(RandomAccessFile raf) throws IOException {
		int left = raf.readUnsignedShort();
		int right = raf.readUnsignedShort();
		short value = raf.readShort();
		return new KerningPair(left, right, value);
	}
}
