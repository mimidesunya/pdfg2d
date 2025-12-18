package net.zamasoft.pdfg2d.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Index to location table.
 * 
 * @param offsets the array of glyph offsets
 * @param factor  the multiplication factor for offsets
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record LocaTable(int[] offsets, short factor) implements Table {

	/**
	 * Reads a LocaTable from the given file.
	 * 
	 * @param de           the directory entry
	 * @param raf          the file to read from
	 * @param numGlyphs    the number of glyphs
	 * @param shortEntries whether to use short (16-bit) or long (32-bit) entries
	 * @return a new LocaTable
	 * @throws IOException if an I/O error occurs
	 */
	public static LocaTable read(
			final DirectoryEntry de,
			final RandomAccessFile raf,
			final int numGlyphs,
			final boolean shortEntries) throws IOException {
		final byte[] buf;
		synchronized (raf) {
			raf.seek(de.offset());
			buf = new byte[de.length()];
			raf.read(buf);
		}
		final int[] offsets = new int[numGlyphs + 1];
		final short factor;
		final ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		if (shortEntries) {
			factor = 2;
			for (int i = 0; i <= numGlyphs; i++) {
				offsets[i] = (bais.read() << 8 | bais.read());
			}
		} else {
			factor = 1;
			for (int i = 0; i <= numGlyphs; i++) {
				offsets[i] = (bais.read() << 24 | bais.read() << 16 | bais.read() << 8 | bais.read());
			}
		}
		return new LocaTable(offsets, factor);
	}

	public int getOffset(final int i) {
		return this.offsets[i] * this.factor;
	}

	@Override
	public int getType() {
		return LOCA;
	}
}
