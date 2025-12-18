package net.zamasoft.pdfg2d.font.cff;

import java.lang.ref.SoftReference;

import net.zamasoft.pdfg2d.font.Glyph;
import net.zamasoft.pdfg2d.font.GlyphList;
import net.zamasoft.pdfg2d.font.table.HeadTable;
import net.zamasoft.pdfg2d.font.table.MaxpTable;

/**
 * Glyph list for CFF fonts.
 */
public class CFFGlyphList implements GlyphList {

	private final CFFTable cff;
	private final HeadTable head;
	private final SoftReference<Glyph>[] glyphs;

	@SuppressWarnings("unchecked")
	public CFFGlyphList(final CFFTable cff, final HeadTable head, final MaxpTable maxp) {
		this.cff = cff;
		this.head = head;
		this.glyphs = new SoftReference[maxp.getNumGlyphs()];
	}

	@Override
	public synchronized Glyph getGlyph(final int ix) {
		if (ix >= this.glyphs.length) {
			return null;
		}
		Glyph glyph = this.glyphs[ix] == null ? null : this.glyphs[ix].get();
		if (glyph != null) {
			return glyph;
		}
		final short upm = this.head.getUnitsPerEm();
		glyph = this.cff.getGlyph(ix, upm);
		this.glyphs[ix] = new SoftReference<>(glyph);
		return glyph;
	}
}
