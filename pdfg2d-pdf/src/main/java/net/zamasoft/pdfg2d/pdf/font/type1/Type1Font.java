package net.zamasoft.pdfg2d.pdf.font.type1;

import java.io.IOException;

import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.XRef;
import net.zamasoft.pdfg2d.pdf.font.PDFFont;
import net.zamasoft.pdfg2d.pdf.font.util.PDFFontUtils;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;

/**
 * Standard Type1 font.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class Type1Font implements PDFFont {
	private static final long serialVersionUID = 0L;

	private final AbstractType1FontSource source;

	private final String name, encoding;

	private final ObjectRef fontRef;

	Type1Font(AbstractType1FontSource source, String name, String encoding, ObjectRef fontRef) {
		this.source = source;
		this.name = name;
		this.encoding = encoding;
		this.fontRef = fontRef;
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public int toGID(int c) {
		int gid = this.source.toGID(c);
		return gid;
	}

	public short getKerning(int scid, int cid) {
		return this.source.getKerning(scid, cid);
	}

	public int getLigature(int gid, int cid) {
		return this.source.getLigature(gid, cid);
	}

	public short getAdvance(int gid) {
		return this.source.getAdvance(gid);
	}

	public short getWidth(int gid) {
		return this.getAdvance(gid);
	}

	public String getName() {
		return this.name;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		if (gc instanceof PDFGC) {
			// PDF
			PDFGraphicsOutput out = ((PDFGC) gc).getPDFGraphicsOutput();
			int glyphCount = text.getGlyphCount();
			int[] glyphIds = text.getGlyphIds();
			double[] xadvances = text.getXAdvances(false);
			double size = text.getFontMetrics().getFontSize();
			out.startArray();
			int pgid = 0;
			StringBuffer buff = new StringBuffer();
			for (int j = 0; j < glyphCount; ++j) {
				int gid = glyphIds[j];
				short kerning = this.source.getKerning(gid, pgid);
				if (xadvances != null) {
					if (j == 0) {
						double xadvance = xadvances[j];
						if (xadvance != 0) {
							out.writeReal(-xadvance * FontSource.DEFAULT_UNITS_PER_EM / size);
						}
					} else {
						kerning += xadvances[j] * FontSource.DEFAULT_UNITS_PER_EM / size;
					}
				}
				if (kerning != 0) {
					out.writeString(buff.toString());
					buff.delete(0, buff.length());
					out.writeInt(-kerning);
				}
				buff.append((char) gid);
				pgid = gid;
			}
			out.writeString(buff.toString());
			out.endArray();
			out.writeOperator("TJ");
		} else {
			PDFFontUtils.drawAwtFont(gc, this.source, this.source.getAwtFont(), text);
		}
	}

	public void writeTo(PDFFragmentOutput out, XRef xref) throws IOException {
		out.startObject(this.fontRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("Font");
		out.lineBreak();
		out.writeName("Subtype");
		out.writeName("Type1");
		out.lineBreak();
		if (this.encoding != null) {
			out.writeName("Encoding");
			out.writeName(this.encoding);
			out.lineBreak();
		}
		out.writeName("Name");
		out.writeName(this.name);
		out.lineBreak();
		out.writeName("BaseFont");
		out.writeName(this.source.getFontName());
		out.lineBreak();
		out.endHash();
		out.endObject();
	}

	public String toString() {
		return super.toString() + ":[PDFName=" + this.getName() + " source=" + this.getFontSource() + "]";
	}
}