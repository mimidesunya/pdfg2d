package net.zamasoft.pdfg2d.font.truetype;

import net.zamasoft.pdfg2d.font.table.GlyfTable;
import net.zamasoft.pdfg2d.font.table.Program;

/**
 * Base class for glyph descriptions.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class GlyfDescript implements Program {

	// flags
	public static final byte onCurve = 0x01;
	public static final byte xShortVector = 0x02;
	public static final byte yShortVector = 0x04;
	public static final byte repeat = 0x08;
	public static final byte xDual = 0x10;
	public static final byte yDual = 0x20;

	protected final GlyfTable parentTable;
	private final int numberOfContours;
	private final short xMin;
	private final short yMin;
	private final short xMax;
	private final short yMax;
	private final short[] instructions;

	protected GlyfDescript(final GlyfTable parentTable, final int numberOfContours, final short xMin, final short yMin,
			final short xMax, final short yMax, final short[] instructions) {
		this.parentTable = parentTable;
		this.numberOfContours = numberOfContours;
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.instructions = instructions;
	}

	@Override
	public short[] getInstructions() {
		return this.instructions;
	}

	public int getNumberOfContours() {
		return this.numberOfContours;
	}

	public short getXMaximum() {
		return this.xMax;
	}

	public short getXMinimum() {
		return this.xMin;
	}

	public short getYMaximum() {
		return this.yMax;
	}

	public short getYMinimum() {
		return this.yMin;
	}

	public abstract int getEndPtOfContours(int i);

	public abstract byte getFlags(int i);

	public abstract short getXCoordinate(int i);

	public abstract short getYCoordinate(int i);

	public abstract boolean isComposite();

	public abstract int getPointCount();

	public abstract int getContourCount();
}
