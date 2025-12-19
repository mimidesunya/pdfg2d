package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Named output.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFNamedOutput extends PDFOutput {
	public PDFNamedOutput(final OutputStream out, final String nameEncoding) throws IOException {
		super(out, nameEncoding);
	}

	public abstract String getName();
}