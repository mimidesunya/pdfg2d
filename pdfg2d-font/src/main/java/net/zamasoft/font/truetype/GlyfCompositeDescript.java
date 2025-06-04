/*

 Copyright 2001,2003  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.zamasoft.font.truetype;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.font.table.GlyfTable;

/**
 * Glyph description for composite glyphs. Composite glyphs are made up of one
 * or more simple glyphs, usually with some sort of transformation applied to
 * each.
 * 
 * @version $Id: GlyfCompositeDescript.java,v 1.1 2006/04/04 12:21:56 harumanx
 *          Exp $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GlyfCompositeDescript extends GlyfDescript {

	private List<GlyfCompositeComp> components = new ArrayList<GlyfCompositeComp>();

	public GlyfCompositeDescript(GlyfTable parentTable, RandomAccessFile raf) throws IOException {
		super(parentTable, (short) -1, raf);

		// Get all of the composite components
		GlyfCompositeComp comp;
		int firstIndex = 0;
		int firstContour = 0;
		do {
			comp = new GlyfCompositeComp(firstIndex, firstContour, raf);
			this.components.add(comp);

			long off = raf.getFilePointer();
			GlyfDescript desc = parentTable.getDescription(comp.getGlyphIndex());
			raf.seek(off);
			if (desc != null) {
				firstIndex += desc.getPointCount();
				firstContour += desc.getContourCount();
			}
		} while ((comp.getFlags() & GlyfCompositeComp.MORE_COMPONENTS) != 0);

		// Are there hinting intructions to read?
		if ((comp.getFlags() & GlyfCompositeComp.WE_HAVE_INSTRUCTIONS) != 0) {
			readInstructions(raf, (raf.read() << 8 | raf.read()));
		}
	}

	public int getEndPtOfContours(int i) {
		GlyfCompositeComp c = getCompositeCompEndPt(i);
		if (c != null) {
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			return gd.getEndPtOfContours(i - c.getFirstContour()) + c.getFirstIndex();
		}
		return 0;
	}

	public byte getFlags(int i) {
		GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			return gd.getFlags(i - c.getFirstIndex());
		}
		return 0;
	}

	public short getXCoordinate(int i) {
		GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			int n = i - c.getFirstIndex();
			int x = gd.getXCoordinate(n);
			int y = gd.getYCoordinate(n);
			short x1 = (short) c.scaleX(x, y);
			x1 += c.getXTranslate();
			return x1;
		}
		return 0;
	}

	public short getYCoordinate(int i) {
		GlyfCompositeComp c = getCompositeComp(i);
		if (c != null) {
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			int n = i - c.getFirstIndex();
			int x = gd.getXCoordinate(n);
			int y = gd.getYCoordinate(n);
			short y1 = (short) c.scaleY(x, y);
			y1 += c.getYTranslate();
			return y1;
		}
		return 0;
	}

	public boolean isComposite() {
		return true;
	}

	public int getPointCount() {
		GlyfCompositeComp c = (GlyfCompositeComp) this.components.get(this.components.size() - 1);
		GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
		return c.getFirstIndex() + (gd == null ? 0 : gd.getPointCount());
	}

	public int getContourCount() {
		GlyfCompositeComp c = (GlyfCompositeComp) this.components.get(this.components.size() - 1);
		GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
		return c.getFirstContour() + (gd == null ? 0 : gd.getContourCount());
	}

	public int getComponentIndex(int i) {
		return ((GlyfCompositeComp) this.components.get(i)).getFirstIndex();
	}

	public int getComponentCount() {
		return this.components.size();
	}

	protected GlyfCompositeComp getCompositeComp(int i) {
		GlyfCompositeComp c;
		for (int n = 0; n < this.components.size(); n++) {
			c = (GlyfCompositeComp) this.components.get(n);
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			if (c.getFirstIndex() <= i && i < (c.getFirstIndex() + gd.getPointCount())) {
				return c;
			}
		}
		return null;
	}

	protected GlyfCompositeComp getCompositeCompEndPt(int i) {
		GlyfCompositeComp c;
		for (int j = 0; j < this.components.size(); j++) {
			c = (GlyfCompositeComp) this.components.get(j);
			GlyfDescript gd = this.parentTable.getDescription(c.getGlyphIndex());
			if (c.getFirstContour() <= i && i < (c.getFirstContour() + gd.getContourCount())) {
				return c;
			}
		}
		return null;
	}
}
