package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Control Value Table (CVT).
 * 
 * @param values the array of control value values
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record CvtTable(short[] values) implements Table {

	protected CvtTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private static short[] readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			final int len = de.length() / 2;
			final short[] values = new short[len];
			for (int i = 0; i < len; i++) {
				values[i] = raf.readShort();
			}
			return values;
		}
	}

	public short[] getValues() {
		return this.values;
	}

	@Override
	public int getType() {
		return CVT;
	}
}
