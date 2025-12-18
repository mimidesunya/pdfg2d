package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.zamasoft.pdfg2d.font.truetype.GlyfCompositeDescript;
import net.zamasoft.pdfg2d.font.truetype.GlyfDescript;
import net.zamasoft.pdfg2d.font.truetype.GlyfSimpleDescript;

/**
 * Glyph data table.
 * 
 * @param de   the directory entry
 * @param loca the index to location table
 * @param raf  the file to read from
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record GlyfTable(DirectoryEntry de, LocaTable loca, RandomAccessFile raf) implements Table {

	public GlyfDescript getDescription(final int i) {
		GlyfDescript desc = null;
		try {
			final int len = this.loca.getOffset((i + 1)) - this.loca.getOffset(i);
			if (len <= 0) {
				return null;
			}
			synchronized (this.raf) {
				this.raf.seek(this.de.offset() + this.loca.getOffset(i));
				final int numberOfContours = this.raf.readShort();
				if (numberOfContours >= 0) {
					desc = GlyfSimpleDescript.read(this, numberOfContours, this.raf);
				} else {
					desc = GlyfCompositeDescript.read(this, this.raf);
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return desc;
	}

	@Override
	public int getType() {
		return GLYF;
	}
}
