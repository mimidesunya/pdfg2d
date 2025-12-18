package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a lookup table in an OpenType font.
 * 
 * @param type      the lookup type
 * @param subTables the array of lookup subtables
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Lookup(int type, LookupSubtable[] subTables) {

	// LookupFlag bit enumeration
	public static final int IGNORE_BASE_GLYPHS = 0x0002;
	public static final int IGNORE_BASE_LIGATURES = 0x0004;
	public static final int IGNORE_BASE_MARKS = 0x0008;
	public static final int MARK_ATTACHMENT_TYPE = 0xFF00;

	/**
	 * Creates a new Lookup by reading from the given file.
	 * 
	 * @param factory the factory for creating subtables
	 * @param raf     the file to read from
	 * @param offset  the offset to seek to
	 * @throws IOException if an I/O error occurs
	 */
	public Lookup(final LookupSubtableFactory factory, final RandomAccessFile raf, final int offset)
			throws IOException {
		this(readData(factory, raf, offset));
	}

	private Lookup(Lookup other) {
		this(other.type, other.subTables);
	}

	private static Lookup readData(final LookupSubtableFactory factory, final RandomAccessFile raf, final int offset)
			throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			final int type = raf.readUnsignedShort();
			raf.readUnsignedShort(); // flag
			final int subTableCount = raf.readUnsignedShort();
			final int[] subTableOffsets = new int[subTableCount];
			final LookupSubtable[] subTables = new LookupSubtable[subTableCount];
			for (int i = 0; i < subTableCount; i++) {
				subTableOffsets[i] = raf.readUnsignedShort();
			}
			for (int i = 0; i < subTableCount; i++) {
				subTables[i] = factory.read(type, raf, offset + subTableOffsets[i]);
			}
			return new Lookup(type, subTables);
		}
	}

	public int getType() {
		return this.type;
	}

	public int getSubtableCount() {
		return this.subTables.length;
	}

	public LookupSubtable getSubtable(final int i) {
		return this.subTables[i];
	}
}
