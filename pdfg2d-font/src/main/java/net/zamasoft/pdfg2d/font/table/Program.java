package net.zamasoft.pdfg2d.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Interface for tables that contain instructions.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public interface Program {

	short[] getInstructions();

	/**
	 * Reads instructions from the given file.
	 * 
	 * @param raf   the file to read from
	 * @param count the number of instructions to read
	 * @return the instructions array
	 * @throws IOException if an I/O error occurs
	 */
	static short[] readInstructions(final RandomAccessFile raf, final int count) throws IOException {
		if (count < 0) {
			return null;
		}
		final short[] instructions = new short[count];
		for (int i = 0; i < count; i++) {
			instructions[i] = (short) raf.readUnsignedByte();
		}
		return instructions;
	}

	/**
	 * Reads instructions from the given input stream.
	 * 
	 * @param bais  the input stream to read from
	 * @param count the number of instructions to read
	 * @return the instructions array
	 */
	static short[] readInstructions(final ByteArrayInputStream bais, final int count) {
		if (count < 0) {
			return null;
		}
		final short[] instructions = new short[count];
		for (int i = 0; i < count; i++) {
			instructions[i] = (short) bais.read();
		}
		return instructions;
	}
}
