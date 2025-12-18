package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Name table containing font naming information.
 * 
 * @param records the naming records
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record NameTable(NameRecord[] records) implements Table {

	protected NameTable(final DirectoryEntry entry, final RandomAccessFile raf) throws IOException {
		this(readData(entry, raf));
	}

	private static NameRecord[] readData(final DirectoryEntry entry, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(entry.offset());
			raf.readShort(); // formatSelector
			final int numberOfNameRecords = raf.readShort();
			final int stringStorageOffset = raf.readShort();
			final NameRecord[] records = new NameRecord[numberOfNameRecords];

			// Load the records (without strings)
			for (int i = 0; i < numberOfNameRecords; i++) {
				records[i] = NameRecord.read(raf);
			}

			// Now load the strings
			for (int i = 0; i < numberOfNameRecords; i++) {
				records[i] = records[i].withLoadedString(raf, entry.offset() + stringStorageOffset);
			}
			return records;
		}
	}

	public String getRecord(final short nameId) {
		// Search for the first instance of this name ID
		for (final NameRecord record : this.records) {
			if (record.getNameId() == nameId) {
				return record.getRecordString();
			}
		}
		return "";
	}

	@Override
	public int getType() {
		return NAME;
	}

	public NameRecord get(final int i) {
		return this.records[i];
	}

	public int size() {
		return this.records.length;
	}
}
