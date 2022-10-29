package net.zamasoft.pdfg2d.pdf.font.cid.identity;

import java.io.IOException;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfFragmentOutput;
import net.zamasoft.pdfg2d.pdf.XRef;
import net.zamasoft.pdfg2d.pdf.font.cid.CIDFont;
import net.zamasoft.pdfg2d.pdf.font.cid.CIDUtils;
import net.zamasoft.pdfg2d.pdf.font.util.PdfFontUtils;
import net.zamasoft.pdfg2d.pdf.gc.PdfGC;
import net.zamasoft.pdfg2d.util.IntList;
import net.zamasoft.pdfg2d.util.ShortList;

class SystemCIDIdentityFont extends CIDFont {
	private static final long serialVersionUID = 0L;

	protected final ShortList advances = new ShortList(Short.MIN_VALUE);

	protected final IntList unicodes = new IntList();

	protected SystemCIDIdentityFont(SystemCIDIdentityFontSource source, String name, ObjectRef fontRef) {
		super(source, name, fontRef);
	}

	public int toGID(int c) {
		SystemCIDIdentityFontSource source = (SystemCIDIdentityFontSource) this.source;
		int gid = source.toGID(c);
		return gid;
	}

	public short getAdvance(int cid) {
		SystemCIDIdentityFontSource source = (SystemCIDIdentityFontSource) this.source;
		return source.getWidth(cid);
	}

	public short getWidth(int gid) {
		return this.getAdvance(gid);
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		if (gc instanceof PdfGC) {
			PdfFontUtils.drawCIDTo(((PdfGC) gc).getPDFGraphicsOutput(), text, false);
		} else {
			SystemCIDIdentityFontSource source = (SystemCIDIdentityFontSource) this.getFontSource();
			PdfFontUtils.drawAwtFont(gc, source, source.getAwtFont(), text);
		}

		int glen = text.getGLen();
		int[] gids = text.getGIDs();
		char[] chars = text.getChars();
		for (int i = 0; i < glen; ++i) {
			int gid = gids[i];
			short nadvance = this.getAdvance(gid);
			this.advances.set(gid, nadvance);
			this.unicodes.set(gid, chars[i]);
		}
	}

	public void writeTo(PdfFragmentOutput out, XRef xref) throws IOException {
		SystemCIDIdentityFontSource source = (SystemCIDIdentityFontSource) this.source;
		CIDUtils.writeIdentityFont(out, xref, source, this.fontRef, this.advances.toArray(), null,
				this.unicodes.toArray());
	}
}