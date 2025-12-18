package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Ligature substitution lookup subtable interface.
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public interface LigatureSubst extends LookupSubtable {

	int getFormat();

	static LigatureSubst read(final RandomAccessFile raf, final int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			final int format = raf.readUnsignedShort();
			if (format == 1) {
				return LigatureSubstFormat1.read(raf, offset);
			}
		}
		return null;
	}
}
