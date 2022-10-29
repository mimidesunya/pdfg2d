package net.zamasoft.font.cff;

import java.lang.ref.SoftReference;

import net.zamasoft.font.Glyph;
import net.zamasoft.font.GlyphList;
import net.zamasoft.font.table.HeadTable;
import net.zamasoft.font.table.MaxpTable;

public class CffGlyphList implements GlyphList {

	private final CffTable cff;

	private final HeadTable head;

	private SoftReference<Glyph>[] glyphs;

	@SuppressWarnings("unchecked")
	public CffGlyphList(CffTable cff, HeadTable head, MaxpTable maxp) {
		this.cff = cff;
		this.head = head;
		this.glyphs = new SoftReference[maxp.getNumGlyphs()];
	}

	public synchronized Glyph getGlyph(int ix) {
		if (ix >= this.glyphs.length) {
			return null;
		}
		Glyph glyph = this.glyphs[ix] == null ? null : this.glyphs[ix].get();
		if (glyph != null) {
			return glyph;
		}
		short upm = this.head.getUnitsPerEm();
		glyph = this.cff.getGlyph(ix, upm);
		this.glyphs[ix] = new SoftReference<Glyph>(glyph);
		return glyph;
	}
}
