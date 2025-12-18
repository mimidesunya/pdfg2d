package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Control value program table.
 * 
 * @param instructions the control value program instructions
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record PrepTable(short[] instructions) implements Program, Table {

	public PrepTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private static short[] readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			return Program.readInstructions(raf, de.length());
		}
	}

	@Override
	public short[] getInstructions() {
		return this.instructions;
	}

	@Override
	public int getType() {
		return PREP;
	}
}
