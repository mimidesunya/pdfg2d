package net.zamasoft.pdfg2d.pdf.util.codec;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.pdf.PDFOutput;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ASCIIHexOutputStream.java,v 1.1 2005/06/07 04:33:33 harumanx
 *          Exp $
 */

public class ASCIIHexOutputStream extends FilterOutputStream {
	private int pos = 0;

	public ASCIIHexOutputStream(OutputStream out) {
		super(out);
	}

	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public void write(int b) throws IOException {
		this.out.write(HEX[((b >> 4) & 0x0F)]);
		this.out.write(HEX[(b & 0x0F)]);
		if (++this.pos > 40) {
			this.out.write(PDFOutput.EOL);
			this.pos = 0;
		}
	}
}
