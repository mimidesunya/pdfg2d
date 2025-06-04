package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 名前付グラフィックスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFNamedGraphicsOutput extends PDFGraphicsOutput {
	protected PDFNamedGraphicsOutput(PDFWriter pdfWriter, OutputStream out, double width, double height)
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