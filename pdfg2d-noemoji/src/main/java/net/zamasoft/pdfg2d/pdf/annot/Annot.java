package net.zamasoft.pdfg2d.pdf.annot;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PdfOutput;
import net.zamasoft.pdfg2d.pdf.PdfPageOutput;

/**
 * アノテーションです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Annot.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class Annot {
	protected Shape shape;
	protected String contents;

	/**
	 * アノテーションの領域を設定します。 cmオペレータ(あるいはPDF_GCのtransformメソッド)による 座標変換が適用されません。
	 * そのため、shapeに対してはアプリケーションが明示的に座標変換を適用する必要があります。
	 * 
	 * @param shape
	 *            アクティブな範囲です。ただし、PDF1.5以前では、バウンディングボックスが指定範囲となります。
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Shape getShape() {
		return shape;
	}

	public String getContents() {
		return this.contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public void writeTo(PdfOutput out, PdfPageOutput pageOut) throws IOException {
		out.writeName("Type");
		out.writeName("Annot");
		out.lineBreak();

		// 領域
		double pageHeight = pageOut.getHeight();
		out.writeName("Rect");
		out.startArray();
		Rectangle2D rect = this.getShape().getBounds2D();
		double x = rect.getX();
		double y = rect.getY();
		double width = rect.getWidth();
		double height = rect.getHeight();
		out.writeReal(x);
		out.writeReal(pageHeight - (y + height));
		out.writeReal(x + width);
		out.writeReal(pageHeight - y);
		out.endArray();
		out.lineBreak();

		// 内容
		String contents = this.getContents();
		if (contents != null) {
			out.writeName("Contents");
			out.writeText(contents);
			out.lineBreak();
		}
	}
}
