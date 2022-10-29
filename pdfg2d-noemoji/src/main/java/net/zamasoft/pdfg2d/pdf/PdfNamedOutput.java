package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfNamedOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfNamedOutput extends PdfOutput {
	public PdfNamedOutput(OutputStream out, String nameEncoding) throws IOException {
		super(out, nameEncoding);
	}

	public abstract String getName();
}