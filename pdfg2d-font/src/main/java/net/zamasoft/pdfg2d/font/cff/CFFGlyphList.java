package net.zamasoft.pdfg2d.font.cff;

import java.lang.ref.SoftReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

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
	private final AtomicReferenceArray<SoftReference<Glyph>> glyphs;

	public CFFGlyphList(final CFFTable cff, final HeadTable head, final MaxpTable maxp) {
		this.cff = cff;
		this.head = head;
		this.glyphs = new AtomicReferenceArray<>(maxp.getNumGlyphs());
	}

	@Override
	public Glyph getGlyph(final int ix) {
		if (ix >= this.glyphs.length()) {
			return null;
		}
		final SoftReference<Glyph> ref = this.glyphs.get(ix);
		Glyph glyph = (ref != null) ? ref.get() : null;
		if (glyph != null) {
			return glyph;
		}
		final short upm = this.head.getUnitsPerEm();
		glyph = this.cff.getGlyph(ix, upm);
		this.glyphs.set(ix, new SoftReference<>(glyph));
		return glyph;
	}
}
