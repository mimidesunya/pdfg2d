package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Font header table.
 * 
 * @param versionNumber      the version number
 * @param fontRevision       the font revision
 * @param checkSumAdjustment the checksum adjustment
 * @param magicNumber        the magic number
 * @param flags              flags
 * @param unitsPerEm         units per em
 * @param created            date created
 * @param modified           date modified
 * @param xMin               minimum X
 * @param yMin               minimum Y
 * @param xMax               maximum X
 * @param yMax               maximum Y
 * @param macStyle           Mac style
 * @param lowestRecPPEM      lowest recommended PPEM
 * @param fontDirectionHint  font direction hint
 * @param indexToLocFormat   index to loc format
 * @param glyphDataFormat    glyph data format
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record HeadTable(
		int versionNumber,
		int fontRevision,
		int checkSumAdjustment,
		int magicNumber,
		short flags,
		short unitsPerEm,
		long created,
		long modified,
		short xMin,
		short yMin,
		short xMax,
		short yMax,
		short macStyle,
		short lowestRecPPEM,
		short fontDirectionHint,
		short indexToLocFormat,
		short glyphDataFormat) implements Table {

	protected HeadTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private HeadTable(HeadTable other) {
		this(
				other.versionNumber,
				other.fontRevision,
				other.checkSumAdjustment,
				other.magicNumber,
				other.flags,
				other.unitsPerEm,
				other.created,
				other.modified,
				other.xMin,
				other.yMin,
				other.xMax,
				other.yMax,
				other.macStyle,
				other.lowestRecPPEM,
				other.fontDirectionHint,
				other.indexToLocFormat,
				other.glyphDataFormat);
	}

	private static HeadTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			return new HeadTable(
					raf.readInt(),
					raf.readInt(),
					raf.readInt(),
					raf.readInt(),
					raf.readShort(),
					raf.readShort(),
					raf.readLong(),
					raf.readLong(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort(),
					raf.readShort());
		}
	}

	public short getIndexToLocFormat() {
		return this.indexToLocFormat;
	}

	public short getUnitsPerEm() {
		return this.unitsPerEm;
	}

	public short getXMax() {
		return this.xMax;
	}

	public short getXMin() {
		return this.xMin;
	}

	public short getYMax() {
		return this.yMax;
	}

	public short getYMin() {
		return this.yMin;
	}

	public short getMacStyle() {
		return this.macStyle;
	}

	@Override
	public int getType() {
		return HEAD;
	}
}
