package net.zamasoft.pdfg2d.font.truetype;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.pdfg2d.font.table.GlyfTable;
import net.zamasoft.pdfg2d.font.table.Program;

/**
 * Glyph description for composite glyphs. Composite glyphs are made up of one
 * or more simple glyphs, usually with some sort of transformation applied to
 * each.
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GlyfCompositeDescript extends GlyfDescript {

	private final List<GlyfCompositeComp> components;

	private GlyfCompositeDescript(final GlyfTable parentTable, final short xMin, final short yMin, final short xMax,
			final short yMax, final short[] instructions, final List<GlyfCompositeComp> components) {
		super(parentTable, -1, xMin, yMin, xMax, yMax, instructions);
		this.components = components;
	}

	public static GlyfCompositeDescript read(final GlyfTable parentTable, final RandomAccessFile raf)
			throws IOException {
		final short xMin = (short) (raf.read() << 8 | raf.read());
		final short yMin = (short) (raf.read() << 8 | raf.read());
		final short xMax = (short) (raf.read() << 8 | raf.read());
		final short yMax = (short) (raf.read() << 8 | raf.read());

		// Get all of the composite components
		final List<GlyfCompositeComp> components = new ArrayList<>();
		GlyfCompositeComp comp;
		int firstIndex = 0;
		int firstContour = 0;
		do {
			comp = GlyfCompositeComp.read(firstIndex, firstContour, raf);
			components.add(comp);

			final long off = raf.getFilePointer();
			final GlyfDescript desc = parentTable.getDescription(comp.getGlyphIndex());
			raf.seek(off);
			if (desc != null) {
				firstIndex += desc.getPointCount();
				firstContour += desc.getContourCount();
			}
		} while ((comp.getFlags() & GlyfCompositeComp.MORE_COMPONENTS) != 0);

		// Are there hinting instructions to read?
		short[] instructions = null;
		if ((comp.getFlags() & GlyfCompositeComp.WE_HAVE_INSTRUCTIONS) != 0) {
			instructions = Program.readInstructions(raf, (raf.read() << 8 | raf.read()));
		}

		return new GlyfCompositeDescript(parentTable, xMin, yMin, xMax, yMax, instructions, components);
	}

	@Override
	public int getEndPtOfContours(final int i) {
		final GlyfCompositeComp c = getCompositeCompEndPt(i);
		if (c != null) {
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
		}
		return 0;
	}

	@Override
	public byte getFlags(final int i) {
		final GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			return gd.getFlags(i - c.getFirstIndex());
		}
		return 0;
	}

	@Override
	public short getXCoordinate(final int i) {
		final GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			final int n = i - c.getFirstIndex();
			final int x = gd.getXCoordinate(n);
			final int y = gd.getYCoordinate(n);
			short x1 = (short) c.scaleX(x, y);
			x1 += c.getXTranslate();
			return x1;
		}
		return 0;
	}

	@Override
	public short getYCoordinate(final int i) {
		final GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			final int n = i - c.getFirstIndex();
			final int x = gd.getXCoordinate(n);
			final int y = gd.getYCoordinate(n);
			short y1 = (short) c.scaleY(x, y);
			y1 += c.getYTranslate();
			return y1;
		}
		return 0;
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public int getPointCount() {
		final GlyfCompositeComp c = this.components.get(this.components.size() - 1);
		final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
		return c.getFirstIndex() + (gd == null ? 0 : gd.getPointCount());
	}

	@Override
	public int getContourCount() {
		final GlyfCompositeComp c = this.components.get(this.components.size() - 1);
		final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
		return c.getFirstContour() + (gd == null ? 0 : gd.getContourCount());
	}

	public int getComponentIndex(final int i) {
		return this.components.get(i).getFirstIndex();
	}

	public int getComponentCount() {
		return this.components.size();
	}

	protected GlyfCompositeComp getCompositeComp(final int i) {
		for (int n = 0; n < this.components.size(); n++) {
			final GlyfCompositeComp c = this.components.get(n);
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			if (c.getFirstIndex() <= i && i < (c.getFirstIndex() + gd.getPointCount())) {
				return c;
			}
		}
		return null;
	}

	protected GlyfCompositeComp getCompositeCompEndPt(final int i) {
		for (int j = 0; j < this.components.size(); j++) {
			final GlyfCompositeComp c = this.components.get(j);
			final GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			if (c.getFirstContour() <= i && i < (c.getFirstContour() + gd.getContourCount())) {
				return c;
			}
		}
		return null;
	}
}
