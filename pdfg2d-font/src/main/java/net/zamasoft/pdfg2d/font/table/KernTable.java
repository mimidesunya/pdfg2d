package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Kerning table.
 * 
 * @param tables the array of kerning subtables
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record KernTable(KernSubtable[] tables) implements Table {

	protected KernTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private static KernSubtable[] readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			raf.readUnsignedShort(); // version
			final int nTables = raf.readUnsignedShort();
			final KernSubtable[] tables = new KernSubtable[nTables];
			for (int i = 0; i < nTables; i++) {
				tables[i] = KernSubtable.read(raf);
			}
			return tables;
		}
	}

	public int getSubtableCount() {
		return this.tables.length;
	}

	public KernSubtable getSubtable(final int i) {
		return this.tables[i];
	}

	@Override
	public int getType() {
		return KERN;
	}
}
