package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Named graphics output.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFNamedGraphicsOutput extends PDFGraphicsOutput {
	protected PDFNamedGraphicsOutput(final PDFWriter pdfWriter, final OutputStream out, final double width,
			final double height) throws IOException {
		super(pdfWriter, out, width, height);
	}

	/**
	 * Returns the name of the graphics.
	 * 
	 * @return the name
	 */
	public abstract String getName();
}