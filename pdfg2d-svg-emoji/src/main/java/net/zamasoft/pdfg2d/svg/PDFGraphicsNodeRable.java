package net.zamasoft.pdfg2d.svg;

import java.awt.Graphics2D;
import java.io.IOException;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;

import net.zamasoft.pdfg2d.g2d.gc.BridgeGraphics2D;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

class PDFGraphicsNodeRable extends GraphicsNodeRable8Bit {
	private final boolean forceVector;

	PDFGraphicsNodeRable(GraphicsNode node, boolean forceVector) {
		super(node);
		this.forceVector = forceVector;
	}

	public boolean paintRable(Graphics2D g2d) {
		if (!(g2d instanceof BridgeGraphics2D)) {
			return super.paintRable(g2d);
		}
		GC gc = ((BridgeGraphics2D) g2d).getGC();
		if (!(gc instanceof PDFGC)) {
			return super.paintRable(g2d);
		}

		@SuppressWarnings("resource")
		PDFGC pgc = (PDFGC) gc;
		try {
			PDFGraphicsOutput pdfgo = pgc.getPDFGraphicsOutput();
			if (!this.forceVector) {
				PDFParams.Version pdfVersion = pdfgo.getPdfWriter().getParams().getVersion();
				if (pdfVersion.v < PDFParams.Version.V_1_4.v || pdfVersion.v == PDFParams.Version.V_PDFA1B.v
						|| pdfVersion.v == PDFParams.Version.V_PDFX1A.v) {
					// 透明化処理がサポートされない場合。
					return super.paintRable(g2d);
				}
			}

			try (PDFGroupImage image2 = pdfgo.getPdfWriter().createGroupImage(pdfgo.getWidth(), pdfgo.getHeight())) {
				PDFGC gc2 = new PDFGC(image2);
				gc2.begin();
				Graphics2D g2d2 = new BridgeGraphics2D(gc2, g2d.getDeviceConfiguration());
				GraphicsNode gn = getGraphicsNode();
				if (getUsePrimitivePaint()) {
					gn.primitivePaint(g2d2);
				} else {
					gn.paint(g2d2);
				}
				gc2.end();
				gc.drawImage(image2);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}
}
