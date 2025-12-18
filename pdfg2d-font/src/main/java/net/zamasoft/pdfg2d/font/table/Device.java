package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a device table in an OpenType font.
 * 
 * @param startSize   the start size
 * @param endSize     the end size
 * @param deltaFormat the delta format
 * @param deltaValues the array of delta values
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Device(int startSize, int endSize, int deltaFormat, int[] deltaValues) {

	/**
	 * Creates a new Device by reading from the given file.
	 * 
	 * @param raf the file to read from
	 * @throws IOException if an I/O error occurs
	 */
	public Device(final RandomAccessFile raf) throws IOException {
		this(readData(raf));
	}

	private Device(Device other) {
		this(other.startSize, other.endSize, other.deltaFormat, other.deltaValues);
	}

	private static Device readData(final RandomAccessFile raf) throws IOException {
		final int startSize = raf.readUnsignedShort();
		final int endSize = raf.readUnsignedShort();
		final int deltaFormat = raf.readUnsignedShort();
		int size = startSize - endSize;
		size = switch (deltaFormat) {
			case 1 -> (size % 8 == 0) ? size / 8 : size / 8 + 1;
			case 2 -> (size % 4 == 0) ? size / 4 : size / 4 + 1;
			case 3 -> (size % 2 == 0) ? size / 2 : size / 2 + 1;
			default -> size;
		};
		final int[] deltaValues = new int[size];
		for (int i = 0; i < size; i++) {
			deltaValues[i] = raf.readUnsignedShort();
		}
		return new Device(startSize, endSize, deltaFormat, deltaValues);
	}
}
