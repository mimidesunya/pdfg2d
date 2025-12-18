package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Horizontal header table.
 * 
 * @param ascender            ascender
 * @param descender           descender
 * @param lineGap             line gap
 * @param advanceWidthMax     maximum advance width
 * @param minLeftSideBearing  minimum left side bearing
 * @param minRightSideBearing minimum right side bearing
 * @param xMaxExtent          maximum X extent
 * @param caretSlopeRise      caret slope rise
 * @param caretSlopeRun       caret slope run
 * @param metricDataFormat    metric data format
 * @param numberOfHMetrics    number of horizontal metrics
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record HheaTable(
		short ascender,
		short descender,
		short lineGap,
		short advanceWidthMax,
		short minLeftSideBearing,
		short minRightSideBearing,
		short xMaxExtent,
		short caretSlopeRise,
		short caretSlopeRun,
		short metricDataFormat,
		int numberOfHMetrics) implements XheaTable {

	protected HheaTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private HheaTable(HheaTable other) {
		this(
				other.ascender,
				other.descender,
				other.lineGap,
				other.advanceWidthMax,
				other.minLeftSideBearing,
				other.minRightSideBearing,
				other.xMaxExtent,
				other.caretSlopeRise,
				other.caretSlopeRun,
				other.metricDataFormat,
				other.numberOfHMetrics);
	}

	private static HheaTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			raf.readInt(); // version
			final short ascender = raf.readShort();
			final short descender = raf.readShort();
			final short lineGap = raf.readShort();
			final short advanceWidthMax = raf.readShort();
			final short minLeftSideBearing = raf.readShort();
			final short minRightSideBearing = raf.readShort();
			final short xMaxExtent = raf.readShort();
			final short caretSlopeRise = raf.readShort();
			final short caretSlopeRun = raf.readShort();
			for (int i = 0; i < 5; i++) {
				raf.readShort();
			}
			final short metricDataFormat = raf.readShort();
			final int numberOfHMetrics = raf.readUnsignedShort();
			return new HheaTable(
					ascender,
					descender,
					lineGap,
					advanceWidthMax,
					minLeftSideBearing,
					minRightSideBearing,
					xMaxExtent,
					caretSlopeRise,
					caretSlopeRun,
					metricDataFormat,
					numberOfHMetrics);
		}
	}

	@Override
	public short getAdvanceWidthMax() {
		return this.advanceWidthMax;
	}

	@Override
	public short getAscender() {
		return this.ascender;
	}

	@Override
	public short getCaretSlopeRise() {
		return this.caretSlopeRise;
	}

	@Override
	public short getCaretSlopeRun() {
		return this.caretSlopeRun;
	}

	@Override
	public short getDescender() {
		return this.descender;
	}

	@Override
	public short getLineGap() {
		return this.lineGap;
	}

	@Override
	public short getMetricDataFormat() {
		return this.metricDataFormat;
	}

	@Override
	public short getMinLeftSideBearing() {
		return this.minLeftSideBearing;
	}

	@Override
	public short getMinRightSideBearing() {
		return this.minRightSideBearing;
	}

	@Override
	public int getNumberOfHMetrics() {
		return this.numberOfHMetrics;
	}

	@Override
	public short getXMaxExtent() {
		return this.xMaxExtent;
	}

	@Override
	public int getType() {
		return HHEA;
	}
}
