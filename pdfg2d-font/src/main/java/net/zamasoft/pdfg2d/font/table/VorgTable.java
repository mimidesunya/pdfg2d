package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * Vertical Origin table.
 * 
 * @param defaultVertOriginY the default vertical origin Y
 * @param indexToVertY       map of glyph index to vertical origin Y
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record VorgTable(short defaultVertOriginY, Map<Integer, Short> indexToVertY) implements Table {

	protected VorgTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private VorgTable(VorgTable other) {
		this(other.defaultVertOriginY, other.indexToVertY);
	}

	private static VorgTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			raf.readUnsignedShort(); // majorVersion
			raf.readUnsignedShort(); // minorVersion
			final short defaultVertOriginY = raf.readShort();
			final int numVertOriginYMetrics = raf.readUnsignedShort();

			final Map<Integer, Short> indexToVertY = new HashMap<>();
			for (int i = 0; i < numVertOriginYMetrics; ++i) {
				final int glyphIndex = raf.readUnsignedShort();
				final short vertOriginY = raf.readShort();
				indexToVertY.put(glyphIndex, vertOriginY);
			}
			return new VorgTable(defaultVertOriginY, indexToVertY);
		}
	}

	public short getDefaultVertOriginY() {
		return this.defaultVertOriginY;
	}

	public short getVertOrigunY(final int ix) {
		final Short y = this.indexToVertY.get(ix);
		if (y == null) {
			return this.defaultVertOriginY;
		}
		return y;
	}

	@Override
	public int getType() {
		return VORG;
	}
}
