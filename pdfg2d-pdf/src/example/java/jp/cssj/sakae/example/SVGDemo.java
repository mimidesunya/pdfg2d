package jp.cssj.sakae.example;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.g2d.gc.G2dGC;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.svg.SVGImage;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SVGDemo {
	public static void main(String[] args) throws Exception {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("local/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder);
			File file = new File("src/example/flower.svgz");
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			SVGDocument doc = f.createSVGDocument(file.toURI().toString(),
					new GZIPInputStream(new FileInputStream(file)));
			SVGOMSVGElement root = (SVGOMSVGElement) doc.getDocumentElement();
			String width = root.getAttribute("width");
			String height = root.getAttribute("height");
			if ((width == null || width.length() == 0) && (height != null && height.length() > 0)) {
				root.setAttribute("width", width = height);
			} else if ((height == null || height.length() == 0) && (width != null && width.length() > 0)) {
				root.setAttribute("height", height = width);
			}
			GVTBuilder gvt = new GVTBuilder();
			BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
			final GraphicsNode gvtRoot = gvt.build(ctx, doc);
			Dimension2D dim = ctx.getDocumentSize();
			final SVGImage svg = new SVGImage(gvtRoot, dim.getWidth(), dim.getHeight());

			try (PdfGraphicsOutput page = pdf.nextPage(dim.getWidth(), dim.getHeight())) {
				GC gc = new PdfGC(page);
				gc.drawImage(svg);

				JFrame frame = new JFrame("Graphics") {
					private static final long serialVersionUID = 1L;

					@Override
					public void paint(Graphics g) {
						super.paint(g);
						Graphics2D g2d = (Graphics2D) g;
						G2dGC gc = new G2dGC(g2d, pdf.getFontManager());
						gc.drawImage(svg);
					}

				};
				frame.setSize((int) dim.getWidth(), (int) dim.getHeight());
				frame.setVisible(true);
			}
			pdf.finish();
			builder.finish();
		}
	}
}
