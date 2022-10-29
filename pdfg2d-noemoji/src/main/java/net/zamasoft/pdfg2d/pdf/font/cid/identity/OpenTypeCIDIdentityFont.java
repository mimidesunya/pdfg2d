package net.zamasoft.pdfg2d.pdf.font.cid.identity;

import java.awt.Shape;
import java.io.IOException;

import net.zamasoft.font.Glyph;
import net.zamasoft.pdfg2d.font.otf.OpenTypeFont;
import net.zamasoft.pdfg2d.font.otf.OpenTypeFontSource;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.util.FontUtils;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfFragmentOutput;
import net.zamasoft.pdfg2d.pdf.XRef;
import net.zamasoft.pdfg2d.pdf.font.PdfFont;
import net.zamasoft.pdfg2d.pdf.font.cid.CIDUtils;
import net.zamasoft.pdfg2d.pdf.font.util.PdfFontUtils;
import net.zamasoft.pdfg2d.pdf.gc.PdfGC;
import net.zamasoft.pdfg2d.util.IntList;
import net.zamasoft.pdfg2d.util.ShortList;

class OpenTypeCIDIdentityFont extends OpenTypeFont implements PdfFont {
	private static final long serialVersionUID = 0L;

	protected final ObjectRef fontRef;

	protected final String name;

	protected final ShortList widths = new ShortList(Short.MIN_VALUE);

	protected final ShortList heights;

	protected final IntList unicodes = new IntList();

	protected OpenTypeCIDIdentityFont(OpenTypeCIDIdentityFontSource metaFont, String name, ObjectRef fontRef) {
		super(metaFont);
		this.fontRef = fontRef;
		this.name = name;
		if (this.isVertical()) {
			this.heights = new ShortList(Short.MIN_VALUE);
		} else {
			this.heights = null;
		}
	}

	public String getName() {
		return this.name;
	}

	public int toGID(int c) {
		OpenTypeCIDIdentityFontSource source = (OpenTypeCIDIdentityFontSource) this.getFontSource();
		int gid = source.getCmapFormat().mapCharCode(c);
		if (this.vSubst != null) {
			gid = this.vSubst.substitute(gid);
		}
		return gid;
	}

	protected int toChar(int gid) {
		return this.unicodes.get(gid);
	}

	public Shape getShapeByGID(int gid) {
		OpenTypeFontSource source = (OpenTypeFontSource) this.getFontSource();
		Glyph glyph = source.getOpenTypeFont().getGlyph(gid);
		if (glyph == null) {
			return null;
		}
		Shape shape = glyph.getPath();
		if (shape == null) {
			return null;
		}
		shape = this.adjustShape(shape, gid);
		return shape;
	}

	public short getAdvance(int gid) {
		if (this.isVertical()) {
			return this.getVAdvance(gid);
		}
		return this.getHAdvance(gid);
	}

	public short getWidth(int gid) {
		return this.getHAdvance(gid);
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		if (gc instanceof PdfGC) {
			PdfFontUtils.drawCIDTo(((PdfGC) gc).getPDFGraphicsOutput(), text, this.isVertical());
		} else {
			FontUtils.drawText(gc, this, text);
		}

		int glen = text.getGLen();
		int[] gids = text.getGIDs();
		char[] chars = text.getChars();
		for (int i = 0; i < glen; ++i) {
			this.widths.set(gids[i], this.getHAdvance(gids[i]));
			if (this.isVertical()) {
				this.heights.set(gids[i], this.getVAdvance(gids[i]));
			}
			this.unicodes.set(gids[i], chars[i]);
		}
	}

	public void writeTo(PdfFragmentOutput out, XRef xref) throws IOException {
		OpenTypeCIDIdentityFontSource source = (OpenTypeCIDIdentityFontSource) this.getFontSource();
		short[] w = this.widths.toArray();
		short[] w2;
		if (this.isVertical()) {
			w2 = this.heights.toArray();
		} else {
			w2 = null;
		}
		CIDUtils.writeIdentityFont(out, xref, source, this.fontRef, w, w2, this.unicodes.toArray());
	}

	public short getKerning(int scid, int cid) {
		return 0;
	}

	public int getLigature(int gid, int cid) {
		return -1;
	}
}