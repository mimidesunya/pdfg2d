package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 名前付グラフィックスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfNamedGraphicsOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfNamedGraphicsOutput extends PdfGraphicsOutput {
	protected PdfNamedGraphicsOutput(PdfWriter pdfWriter, OutputStream out, double width, double height)
			throws IOException {
		super(pdfWriter, out, width, height);
	}

	/**
	 * グラフィックスの名前を返します。
	 * 
	 * @return
	 */
	public abstract String getName();
}