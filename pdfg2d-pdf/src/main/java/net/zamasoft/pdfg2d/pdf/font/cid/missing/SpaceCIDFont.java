package net.zamasoft.pdfg2d.pdf.font.cid.missing;

import java.io.IOException;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

class SpaceCIDFont extends MissingCIDFont {
	private static final long serialVersionUID = 1L;

	public SpaceCIDFont(MissingCIDFontSource source, String name, ObjectRef fontRef) {
		super(source, name, fontRef);
	}

	public short getAdvance(int gid) {
		int c = this.unicodes.get(gid);
		switch (c) {
			// Space characters
			case 0x007F:
			case 0x0020:
			case 0x00A0:
			case 0x2028:
			case 0x2029:
			case 0x202F:
				return (short) 500;
		}
		return (short) 0;
	}

	public short getWidth(int gid) {
		return (short) 0;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		// ignore
	}
}
