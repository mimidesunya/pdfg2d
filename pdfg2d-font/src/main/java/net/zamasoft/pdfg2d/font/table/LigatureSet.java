package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a set of ligatures.
 * 
 * @param ligatures the array of ligatures in this set
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record LigatureSet(Ligature[] ligatures) {

	/**
	 * Creates a new LigatureSet by reading from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset to seek to
	 * @throws IOException if an I/O error occurs
	 */
	public LigatureSet(final RandomAccessFile raf, final int offset) throws IOException {
		this(readData(raf, offset));
	}

	private static Ligature[] readData(final RandomAccessFile raf, final int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			final int ligatureCount = raf.readUnsignedShort();
			final int[] ligatureOffsets = new int[ligatureCount];
			for (int i = 0; i < ligatureCount; i++) {
				ligatureOffsets[i] = raf.readUnsignedShort();
			}
			final Ligature[] ligatures = new Ligature[ligatureCount];
			for (int i = 0; i < ligatureCount; i++) {
				raf.seek(offset + ligatureOffsets[i]);
				ligatures[i] = Ligature.read(raf);
			}
			return ligatures;
		}
	}

	public int getLigatureCount() {
		return this.ligatures.length;
	}

	public Ligature getLigature(final int i) {
		return this.ligatures[i];
	}
}
