package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a record in the naming table.
 * 
 * @param platformId   platform ID
 * @param encodingId   encoding ID
 * @param languageId   language ID
 * @param nameId       name ID
 * @param stringLength string length in bytes
 * @param stringOffset string offset from start of storage
 * @param record       the actual string value
 */
public record NameRecord(
		short platformId,
		short encodingId,
		short languageId,
		short nameId,
		short stringLength,
		short stringOffset,
		String record) {

	/**
	 * Reads a record from the file (without loading the string).
	 * 
	 * @param raf the file to read from
	 * @return a NameRecord with null for the record string
	 * @throws IOException if an I/O error occurs
	 */
	protected static NameRecord read(final RandomAccessFile raf) throws IOException {
		return new NameRecord(
				raf.readShort(),
				raf.readShort(),
				raf.readShort(),
				raf.readShort(),
				raf.readShort(),
				raf.readShort(),
				null);
	}

	/**
	 * Loads the string value for this record.
	 * 
	 * @param raf                 the file to read from
	 * @param stringStorageOffset base offset for string storage
	 * @return a new NameRecord with the loaded string
	 * @throws IOException if an I/O error occurs
	 */
	protected NameRecord withLoadedString(final RandomAccessFile raf, final int stringStorageOffset)
			throws IOException {
		final var sb = new StringBuilder();
		synchronized (raf) {
			raf.seek(stringStorageOffset + this.stringOffset);
			if (this.platformId == Table.PLATFORM_UNICODE) {
				// Unicode (big-endian)
				for (int i = 0; i < this.stringLength / 2; i++) {
					sb.append(raf.readChar());
				}
			} else if (this.platformId == Table.PLATFORM_MACINTOSH) {
				// Macintosh encoding, ASCII
				for (int i = 0; i < this.stringLength; i++) {
					sb.append((char) raf.readByte());
				}
			} else if (this.platformId == Table.PLATFORM_ISO) {
				// ISO encoding, ASCII
				for (int i = 0; i < this.stringLength; i++) {
					sb.append((char) raf.readByte());
				}
			} else if (this.platformId == Table.PLATFORM_MICROSOFT) {
				// Microsoft encoding, Unicode
				for (int i = 0; i < this.stringLength / 2; i++) {
					sb.append(raf.readChar());
				}
			}
		}
		return new NameRecord(
				this.platformId,
				this.encodingId,
				this.languageId,
				this.nameId,
				this.stringLength,
				this.stringOffset,
				sb.toString());
	}

	public short getNameId() {
		return this.nameId;
	}

	public String getRecordString() {
		return this.record;
	}
}
