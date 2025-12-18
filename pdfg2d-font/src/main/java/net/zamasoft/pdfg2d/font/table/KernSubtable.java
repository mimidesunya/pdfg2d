package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Interface for kerning subtables.
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public interface KernSubtable {

	/**
	 * Returns the number of kerning pairs.
	 * 
	 * @return the kerning pair count
	 */
	int getKerningPairCount();

	/**
	 * Returns a kerning pair at the given index.
	 * 
	 * @param i the index
	 * @return the kerning pair
	 */
	KerningPair getKerningPair(int i);

	/**
	 * Reads a KernSubtable from the given file.
	 * 
	 * @param raf the file to read from
	 * @return the kerning subtable, or null if unknown format
	 * @throws IOException if an I/O error occurs
	 */
	static KernSubtable read(final RandomAccessFile raf) throws IOException {
		raf.readUnsignedShort(); // version
		raf.readUnsignedShort(); // length
		final int coverage = raf.readUnsignedShort();
		final int format = coverage >> 8;

		return switch (format) {
			case 0 -> KernSubtableFormat0.read(raf);
			case 2 -> KernSubtableFormat2.read(raf);
			default -> null;
		};
	}
}
