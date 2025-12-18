package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Represents the Table Directory of a font file.
 * 
 * @param version       the version number
 * @param searchRange   the search range
 * @param entrySelector the entry selector
 * @param rangeShift    the range shift
 * @param entries       the directory entries, sorted by offset
 */
public record TableDirectory(
		int version,
		short searchRange,
		short entrySelector,
		short rangeShift,
		DirectoryEntry[] entries) {

	/**
	 * Creates a new TableDirectory by reading from the given file.
	 * 
	 * @param raf the file to read from
	 * @throws IOException if an I/O error occurs
	 */
	public TableDirectory(final RandomAccessFile raf) throws IOException {
		this(readData(raf));
	}

	private TableDirectory(TableDirectory other) {
		this(other.version, other.searchRange, other.entrySelector, other.rangeShift, other.entries);
	}

	private static TableDirectory readData(final RandomAccessFile raf) throws IOException {
		final int version = raf.readInt();
		final short numTables = raf.readShort();
		final short searchRange = raf.readShort();
		final short entrySelector = raf.readShort();
		final short rangeShift = raf.readShort();
		final DirectoryEntry[] entries = new DirectoryEntry[numTables];
		for (int i = 0; i < numTables; i++) {
			entries[i] = DirectoryEntry.read(raf);
		}

		// Sort them into file order
		Arrays.sort(entries, Comparator.comparingInt(DirectoryEntry::offset));
		return new TableDirectory(version, searchRange, entrySelector, rangeShift, entries);
	}

	public DirectoryEntry getEntry(final int index) {
		return this.entries[index];
	}

	public DirectoryEntry getEntryByTag(final int tag) {
		for (final DirectoryEntry entry : this.entries) {
			if (entry.tag() == tag) {
				return entry;
			}
		}
		return null;
	}

	public short getNumTables() {
		return (short) this.entries.length;
	}
}
