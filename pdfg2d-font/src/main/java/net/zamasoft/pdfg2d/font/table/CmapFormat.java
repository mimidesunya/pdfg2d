package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Character to glyph mapping format interface.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public interface CmapFormat {
	static CmapFormat createCmapFormat(final int format, final int numGlyphs, final RandomAccessFile data)
			throws IOException {
		if (format == 14) {
			return UvsCmapFormat.read(data, numGlyphs);
		}
		return GenericCmapFormat.read(format, numGlyphs, data);
	}

	int mapCharCode(int charCode);

	default int mapCharCode(int charCode, int vs) {
		return this.mapCharCode(charCode);
	}
}
