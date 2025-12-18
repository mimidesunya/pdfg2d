package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Vertical metrics table.
 * 
 * @param xMetrics        array of combined advance height and top side bearing
 * @param leftSideBearing array of additional top side bearings
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record VmtxTable(int[] xMetrics, short[] leftSideBearing) implements XmtxTable {

	private static final long serialVersionUID = 0L;

	/**
	 * Reads a VmtxTable from the given file.
	 * 
	 * @param de               the directory entry
	 * @param raf              the file to read from
	 * @param numberOfHMetrics the number of horizontal metrics
	 * @param lsbCount         the number of additional left side bearings
	 * @return a new VmtxTable
	 * @throws IOException if an I/O error occurs
	 */
	public static VmtxTable read(
			final DirectoryEntry de,
			final RandomAccessFile raf,
			final int numberOfHMetrics,
			final int lsbCount) throws IOException {
		synchronized (raf) {
			final int[] xMetrics = new int[numberOfHMetrics];
			raf.seek(de.offset());
			for (int i = 0; i < numberOfHMetrics; i++) {
				xMetrics[i] = raf.readInt();
			}
			short[] leftSideBearing = null;
			if (lsbCount > 0) {
				leftSideBearing = new short[lsbCount];
				for (int i = 0; i < lsbCount; i++) {
					leftSideBearing[i] = raf.readShort();
				}
			}
			return new VmtxTable(xMetrics, leftSideBearing);
		}
	}

	@Override
	public int getAdvanceWidth(final int i) {
		if (i < this.xMetrics.length) {
			return this.xMetrics[i] >> 16;
		} else {
			return this.xMetrics[this.xMetrics.length - 1] >> 16;
		}
	}

	@Override
	public short getLeftSideBearing(int i) {
		if (i < this.xMetrics.length) {
			return (short) (this.xMetrics[i] & 0xffff);
		} else {
			i -= this.xMetrics.length;
			return this.leftSideBearing[i];
		}
	}

	@Override
	public int getType() {
		return VMTX;
	}
}
