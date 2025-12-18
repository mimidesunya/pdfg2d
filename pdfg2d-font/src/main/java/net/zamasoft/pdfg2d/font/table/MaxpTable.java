package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Maximum profile table.
 * 
 * @param numGlyphs             the number of glyphs
 * @param maxPoints             maximum points in a non-composite glyph
 * @param maxContours           maximum contours in a non-composite glyph
 * @param maxCompositePoints    maximum points in a composite glyph
 * @param maxCompositeContours  maximum contours in a composite glyph
 * @param maxZones              maximum zones
 * @param maxTwilightPoints     maximum twilight points
 * @param maxStorage            maximum storage
 * @param maxFunctionDefs       maximum function definitions
 * @param maxInstructionDefs    maximum instruction definitions
 * @param maxStackElements      maximum stack elements
 * @param maxSizeOfInstructions maximum size of instructions
 * @param maxComponentElements  maximum component elements
 * @param maxComponentDepth     maximum component depth
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record MaxpTable(
		int numGlyphs,
		int maxPoints,
		int maxContours,
		int maxCompositePoints,
		int maxCompositeContours,
		int maxZones,
		int maxTwilightPoints,
		int maxStorage,
		int maxFunctionDefs,
		int maxInstructionDefs,
		int maxStackElements,
		int maxSizeOfInstructions,
		int maxComponentElements,
		int maxComponentDepth) implements Table {

	protected MaxpTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private MaxpTable(MaxpTable other) {
		this(
				other.numGlyphs,
				other.maxPoints,
				other.maxContours,
				other.maxCompositePoints,
				other.maxCompositeContours,
				other.maxZones,
				other.maxTwilightPoints,
				other.maxStorage,
				other.maxFunctionDefs,
				other.maxInstructionDefs,
				other.maxStackElements,
				other.maxSizeOfInstructions,
				other.maxComponentElements,
				other.maxComponentDepth);
	}

	private static MaxpTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			raf.readInt(); // versionNumber
			return new MaxpTable(
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort(),
					raf.readUnsignedShort());
		}
	}

	public int getMaxComponentDepth() {
		return this.maxComponentDepth;
	}

	public int getMaxComponentElements() {
		return this.maxComponentElements;
	}

	public int getMaxCompositeContours() {
		return this.maxCompositeContours;
	}

	public int getMaxCompositePoints() {
		return this.maxCompositePoints;
	}

	public int getMaxContours() {
		return this.maxContours;
	}

	public int getMaxFunctionDefs() {
		return this.maxFunctionDefs;
	}

	public int getMaxInstructionDefs() {
		return this.maxInstructionDefs;
	}

	public int getMaxPoints() {
		return this.maxPoints;
	}

	public int getMaxSizeOfInstructions() {
		return this.maxSizeOfInstructions;
	}

	public int getMaxStackElements() {
		return this.maxStackElements;
	}

	public int getMaxStorage() {
		return this.maxStorage;
	}

	public int getMaxTwilightPoints() {
		return this.maxTwilightPoints;
	}

	public int getMaxZones() {
		return this.maxZones;
	}

	public int getNumGlyphs() {
		return this.numGlyphs;
	}

	@Override
	public int getType() {
		return MAXP;
	}
}
