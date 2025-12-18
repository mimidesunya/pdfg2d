package net.zamasoft.pdfg2d.demo;

import java.awt.geom.Dimension2D;
import java.io.File;
import java.net.URL;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import net.zamasoft.pdfg2d.io.impl.FileStream;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;
import net.zamasoft.pdfg2d.svg.SVGImage;

/**
 * Demonstrates rendering of a complex SVG image (Ghostscript Tiger) fetched
 * from a URL.
 * <p>
 * This app downloads the Ghostscript Tiger SVG from Wikimedia Commons
 * and renders it into a PDF page.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 */
public class SVGTigerApp {
	public static void main(String[] args) throws Exception {
		String url = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Ghostscript_Tiger.svg";
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		SVGDocument doc = f.createSVGDocument(url, new URL(url).openStream());
		SVGOMSVGElement root = (SVGOMSVGElement) doc.getDocumentElement();
		String width = String.valueOf(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM));
		root.setAttribute("width", width);
		root.setAttribute("height", width);
		GVTBuilder gvt = new GVTBuilder();
		BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
		final GraphicsNode gvtRoot = gvt.build(ctx, doc);
		Dimension2D dim = ctx.getDocumentSize();
		final SVGImage image = new SVGImage(gvtRoot, dim.getWidth(), dim.getHeight());

		try (PDFWriter pdf = new PDFWriterImpl(
				new FileStream(new File(DemoUtils.getOutputDir(), "svg-tiger.pdf")));
				PDFGC gc = new PDFGC(pdf.nextPage(PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
						PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)))) {
			gc.drawImage(image);
		}
	}
}
