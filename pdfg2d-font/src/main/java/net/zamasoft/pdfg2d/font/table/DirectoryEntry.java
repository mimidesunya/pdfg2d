package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * Represents an entry in the table directory of a font file.
 *
 * @param tag      The table tag.
 * @param checksum The table checksum.
 * @param offset   The offset from the beginning of the file.
 * @param length   The length of the table.
 */
public record DirectoryEntry(int tag, int checksum, int offset, int length) implements Serializable {

	/**
	 * Reads a DirectoryEntry from the given RandomAccessFile.
	 *
	 * @param raf The RandomAccessFile to read from.
	 * @return A new DirectoryEntry.
	 * @throws IOException If an I/O error occurs.
	 */
	public static DirectoryEntry read(RandomAccessFile raf) throws IOException {
		int tag = raf.readInt();
		int checksum = raf.readInt();
		int offset = raf.readInt();
		int length = raf.readInt();
		return new DirectoryEntry(tag, checksum, offset, length);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append((char) ((this.tag >> 24) & 0xff))
				.append((char) ((this.tag >> 16) & 0xff))
				.append((char) ((this.tag >> 8) & 0xff))
				.append((char) ((this.tag) & 0xff))
				.append(", offset: ").append(this.offset)
				.append(", length: ").append(this.length)
				.append(", checksum: 0x").append(Integer.toHexString(this.checksum))
				.toString();
	}
}
