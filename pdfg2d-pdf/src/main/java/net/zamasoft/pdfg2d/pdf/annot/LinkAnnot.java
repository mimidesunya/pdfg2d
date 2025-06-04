package net.zamasoft.pdfg2d.pdf.annot;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.net.URI;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Linkアノテーションです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class LinkAnnot extends Annot {
	protected URI uri;

	public URI getURI() {
		return this.uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public void writeTo(PDFOutput out, PDFPageOutput pageOut) throws IOException {
		super.writeTo(out, pageOut);

		// 境界線が表示されないようにする
		out.writeName("Border");
		out.startArray();
		out.writeInt(0);
		out.writeInt(0);
		out.writeInt(0);
		out.endArray();
		out.lineBreak();

		out.writeName("Subtype");
		out.writeName("Link");
		out.lineBreak();

		PDFParams.Version pdfVersion = pageOut.getPdfWriter().getParams().getVersion();
		if (pdfVersion.v >= PDFParams.Version.V_1_6.v && !this.shape.equals(this.shape.getBounds2D())) {
			// 矩形以外のリンク領域
			double[] cord = new double[6];

			// 平行四辺形かチェック
			int corners = 0;
			boolean bad = false, rect = true;
			double x = 0, y = 0;
			LOOP: for (PathIterator i = this.shape.getPathIterator(null); !i.isDone(); i.next()) {
				int type = i.currentSegment(cord);
				switch (type) {
				case PathIterator.SEG_LINETO:
					if (x != cord[0] && y != cord[1]) {
						rect = false;
					}
					x = cord[0];
					y = cord[1];
					++corners;
					break;
				case PathIterator.SEG_MOVETO:
					x = cord[0];
					y = cord[1];
					++corners;
					break;
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_CUBICTO:
					bad = true;
					break LOOP;
				case PathIterator.SEG_CLOSE:
					break;
				}
			}

			// 平行四辺形を配置
			if (!rect && !bad && corners == 4) {
				out.writeName("QuadPoints");
				out.startArray();
				double pageHeight = pageOut.getHeight();
				for (PathIterator i = this.shape.getPathIterator(null); !i.isDone(); i.next()) {
					int type = i.currentSegment(cord);
					if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
						x = cord[0];
						y = cord[1];
						out.writeReal(x);
						out.writeReal(pageHeight - y);
					}
				}
				out.endArray();
				out.lineBreak();
			}
		}

		if (this.uri.toString().startsWith("#")) {
			out.writeName("Dest");
			out.writeText(uri.getFragment());
			out.lineBreak();
		} else {
			out.writeName("A");
			out.startHash();
			out.writeName("S");
			out.writeName("URI");
			out.lineBreak();
			out.writeName("URI");
			out.writeString(this.uri.toASCIIString());
			out.endHash();
			out.lineBreak();
		}
	}

	public String toString() {
		return "Link: " + this.uri;
	}
}
