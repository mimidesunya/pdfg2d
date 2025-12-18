package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.zamasoft.pdfg2d.font.OpenTypeFont;

/**
 * Character to glyph index mapping table.
 * 
 * @param entries array of index entries
 * @param formats array of cmap formats
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record CmapTable(CmapIndexEntry[] entries, CmapFormat[] formats) implements Table {

	protected CmapTable(final OpenTypeFont otf, final DirectoryEntry de, final RandomAccessFile raf)
			throws IOException {
		this(readData(otf, de, raf));
	}

	private CmapTable(CmapTable other) {
		this(other.entries, other.formats);
	}

	private static CmapTable readData(final OpenTypeFont otf, final DirectoryEntry de, final RandomAccessFile raf)
			throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			final long fp = raf.getFilePointer();
			raf.readUnsignedShort(); // version
			final int numTables = raf.readUnsignedShort();
			final CmapIndexEntry[] entries = new CmapIndexEntry[numTables];
			final CmapFormat[] formats = new CmapFormat[numTables];

			// Get each of the index entries
			for (int i = 0; i < numTables; i++) {
				entries[i] = CmapIndexEntry.read(raf);
			}

			// Read each of the formats
			for (int i = 0; i < numTables; i++) {
				raf.seek(fp + entries[i].offset());
				final int format = raf.readUnsignedShort();
				formats[i] = CmapFormat.createCmapFormat(format, otf.getNumGlyphs(), raf);
			}

			return new CmapTable(entries, formats);
		}
	}

	public CmapFormat getCmapFormat(final short platformId, final short encodingId) {
		// Find the requested format
		for (int i = 0; i < this.entries.length; i++) {
			if (this.entries[i].platformId() == platformId
					&& (encodingId == -1 || this.entries[i].encodingId() == encodingId)) {
				return this.formats[i];
			}
		}
		return null;
	}

	public CmapFormat getCmapFormat(final int ix) {
		return this.formats[ix];
	}

	public int getTableCount() {
		return this.entries.length;
	}

	@Override
	public int getType() {
		return CMAP;
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder().append("cmap\n");

		// Get each of the index entries
		for (int i = 0; i < this.entries.length; i++) {
			sb.append("\t").append(this.entries[i]).append("\n");
		}

		// Get each of the tables
		for (int i = 0; i < this.formats.length; i++) {
			sb.append("\t").append(this.formats[i]).append("\n");
		}
		return sb.toString();
	}
}
