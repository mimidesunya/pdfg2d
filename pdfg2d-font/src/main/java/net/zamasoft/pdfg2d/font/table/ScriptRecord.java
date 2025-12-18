package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record ScriptRecord(int tag, int offset) {
	public static ScriptRecord read(RandomAccessFile raf) throws IOException {
		return new ScriptRecord(raf.readInt(), raf.readUnsignedShort());
	}
}
