package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * OS/2 and Windows Metrics table.
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Os2Table(
		int version,
		short xAvgCharWidth,
		int usWeightClass,
		int usWidthClass,
		short fsType,
		short ySubscriptXSize,
		short ySubscriptYSize,
		short ySubscriptXOffset,
		short ySubscriptYOffset,
		short ySuperscriptXSize,
		short ySuperscriptYSize,
		short ySuperscriptXOffset,
		short ySuperscriptYOffset,
		short yStrikeoutSize,
		short yStrikeoutPosition,
		short sFamilyClass,
		Panose panose,
		int ulUnicodeRange1,
		int ulUnicodeRange2,
		int ulUnicodeRange3,
		int ulUnicodeRange4,
		int achVendorID,
		short fsSelection,
		int usFirstCharIndex,
		int usLastCharIndex,
		short sTypoAscender,
		short sTypoDescender,
		short sTypoLineGap,
		int usWinAscent,
		int usWinDescent,
		int ulCodePageRange1,
		int ulCodePageRange2) implements Table {

	protected Os2Table(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private Os2Table(Os2Table other) {
		this(
				other.version,
				other.xAvgCharWidth,
				other.usWeightClass,
				other.usWidthClass,
				other.fsType,
				other.ySubscriptXSize,
				other.ySubscriptYSize,
				other.ySubscriptXOffset,
				other.ySubscriptYOffset,
				other.ySuperscriptXSize,
				other.ySuperscriptYSize,
				other.ySuperscriptXOffset,
				other.ySuperscriptYOffset,
				other.yStrikeoutSize,
				other.yStrikeoutPosition,
				other.sFamilyClass,
				other.panose,
				other.ulUnicodeRange1,
				other.ulUnicodeRange2,
				other.ulUnicodeRange3,
				other.ulUnicodeRange4,
				other.achVendorID,
				other.fsSelection,
				other.usFirstCharIndex,
				other.usLastCharIndex,
				other.sTypoAscender,
				other.sTypoDescender,
				other.sTypoLineGap,
				other.usWinAscent,
				other.usWinDescent,
				other.ulCodePageRange1,
				other.ulCodePageRange2);
	}

	private static Os2Table readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			final int version = raf.readUnsignedShort();
			final short xAvgCharWidth = raf.readShort();
			final int usWeightClass = raf.readUnsignedShort();
			final int usWidthClass = raf.readUnsignedShort();
			final short fsType = raf.readShort();
			final short ySubscriptXSize = raf.readShort();
			final short ySubscriptYSize = raf.readShort();
			final short ySubscriptXOffset = raf.readShort();
			final short ySubscriptYOffset = raf.readShort();
			final short ySuperscriptXSize = raf.readShort();
			final short ySuperscriptYSize = raf.readShort();
			final short ySuperscriptXOffset = raf.readShort();
			final short ySuperscriptYOffset = raf.readShort();
			final short yStrikeoutSize = raf.readShort();
			final short yStrikeoutPosition = raf.readShort();
			final short sFamilyClass = raf.readShort();
			final byte[] buf = new byte[10];
			raf.read(buf);
			final Panose panose = new Panose(buf);
			final int ulUnicodeRange1 = raf.readInt();
			final int ulUnicodeRange2 = raf.readInt();
			final int ulUnicodeRange3 = raf.readInt();
			final int ulUnicodeRange4 = raf.readInt();
			final int achVendorID = raf.readInt();
			final short fsSelection = raf.readShort();
			final int usFirstCharIndex = raf.readUnsignedShort();
			final int usLastCharIndex = raf.readUnsignedShort();
			final short sTypoAscender = raf.readShort();
			final short sTypoDescender = raf.readShort();
			final short sTypoLineGap = raf.readShort();
			final int usWinAscent = raf.readUnsignedShort();
			final int usWinDescent = raf.readUnsignedShort();
			final int ulCodePageRange1 = raf.readInt();
			final int ulCodePageRange2 = raf.readInt();
			return new Os2Table(
					version,
					xAvgCharWidth,
					usWeightClass,
					usWidthClass,
					fsType,
					ySubscriptXSize,
					ySubscriptYSize,
					ySubscriptXOffset,
					ySubscriptYOffset,
					ySuperscriptXSize,
					ySuperscriptYSize,
					ySuperscriptXOffset,
					ySuperscriptYOffset,
					yStrikeoutSize,
					yStrikeoutPosition,
					sFamilyClass,
					panose,
					ulUnicodeRange1,
					ulUnicodeRange2,
					ulUnicodeRange3,
					ulUnicodeRange4,
					achVendorID,
					fsSelection,
					usFirstCharIndex,
					usLastCharIndex,
					sTypoAscender,
					sTypoDescender,
					sTypoLineGap,
					usWinAscent,
					usWinDescent,
					ulCodePageRange1,
					ulCodePageRange2);
		}
	}

	public int getVersion() {
		return this.version;
	}

	public short getAvgCharWidth() {
		return this.xAvgCharWidth;
	}

	public int getWeightClass() {
		return this.usWeightClass;
	}

	public int getWidthClass() {
		return this.usWidthClass;
	}

	public short getLicenseType() {
		return this.fsType;
	}

	public short getSubscriptXSize() {
		return this.ySubscriptXSize;
	}

	public short getSubscriptYSize() {
		return this.ySubscriptYSize;
	}

	public short getSubscriptXOffset() {
		return this.ySubscriptXOffset;
	}

	public short getSubscriptYOffset() {
		return this.ySubscriptYOffset;
	}

	public short getSuperscriptXSize() {
		return this.ySuperscriptXSize;
	}

	public short getSuperscriptYSize() {
		return this.ySuperscriptYSize;
	}

	public short getSuperscriptXOffset() {
		return this.ySuperscriptXOffset;
	}

	public short getSuperscriptYOffset() {
		return this.ySuperscriptYOffset;
	}

	public short getStrikeoutSize() {
		return this.yStrikeoutSize;
	}

	public short getStrikeoutPosition() {
		return this.yStrikeoutPosition;
	}

	public short getFamilyClass() {
		return this.sFamilyClass;
	}

	public Panose getPanose() {
		return this.panose;
	}

	public int getUnicodeRange1() {
		return this.ulUnicodeRange1;
	}

	public int getUnicodeRange2() {
		return this.ulUnicodeRange2;
	}

	public int getUnicodeRange3() {
		return this.ulUnicodeRange3;
	}

	public int getUnicodeRange4() {
		return this.ulUnicodeRange4;
	}

	public int getVendorID() {
		return this.achVendorID;
	}

	public short getSelection() {
		return this.fsSelection;
	}

	public int getFirstCharIndex() {
		return this.usFirstCharIndex;
	}

	public int getLastCharIndex() {
		return this.usLastCharIndex;
	}

	public short getTypoAscender() {
		return this.sTypoAscender;
	}

	public short getTypoDescender() {
		return this.sTypoDescender;
	}

	public short getTypoLineGap() {
		return this.sTypoLineGap;
	}

	public int getWinAscent() {
		return this.usWinAscent;
	}

	public int getWinDescent() {
		return this.usWinDescent;
	}

	public int getCodePageRange1() {
		return this.ulCodePageRange1;
	}

	public int getCodePageRange2() {
		return this.ulCodePageRange2;
	}

	@Override
	public int getType() {
		return OS_2;
	}
}
