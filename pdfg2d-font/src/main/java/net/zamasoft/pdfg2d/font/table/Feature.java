package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a feature in an OpenType font.
 * 
 * @param lookupListIndex array of lookup list indices
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Feature(int[] lookupListIndex) {

	/**
	 * Creates a new Feature by reading from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset to seek to
	 * @throws IOException if an I/O error occurs
	 */
	protected static Feature read(final RandomAccessFile raf, final int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			raf.readUnsignedShort(); // featureParams
			final int lookupCount = raf.readUnsignedShort();
			final int[] lookupListIndex = new int[lookupCount];
			for (int i = 0; i < lookupCount; i++) {
				lookupListIndex[i] = raf.readUnsignedShort();
			}
			return new Feature(lookupListIndex);
		}
	}

	public int getLookupCount() {
		return this.lookupListIndex.length;
	}

	public int getLookupListIndex(final int i) {
		return this.lookupListIndex[i];
	}
}
