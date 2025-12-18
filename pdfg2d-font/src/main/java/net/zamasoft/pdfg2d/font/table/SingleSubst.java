package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * Single substitution lookup subtable interface.
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public interface SingleSubst extends LookupSubtable, Serializable {

	int getFormat();

	int substitute(int glyphId);

	static SingleSubst read(final RandomAccessFile raf, final int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			final int format = raf.readUnsignedShort();
			if (format == 1) {
				return SingleSubstFormat1.read(raf, offset);
			} else if (format == 2) {
				return SingleSubstFormat2.read(raf, offset);
			}
		}
		return null;
	}
}
