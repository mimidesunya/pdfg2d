package net.zamasoft.pdfg2d.demo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;

import net.zamasoft.pdfg2d.resolver.protocol.file.FileSource;
import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.g2d.gc.G2DGC;

import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.svg.SVGImage;

/**
 * Demonstrates rendering of SVG content into a PDF.
 * <p>
 * This demo loads a compressed SVG file (.svgz), parses it using Apache Batik,
 * and renders it into the PDF page using the {@link PDFGC}.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SVGRenderingDemo {
	public static void main(String[] args) throws Exception {
		final PDFParams params = new PDFParams();
		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "svg.pdf")))) {
			OutputFragmentedStream builder = new OutputFragmentedStream(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			UserAgent userAgent = new UserAgentAdapter();
			DocumentLoader loader = new DocumentLoader(userAgent);
			FileSource source = new FileSource(DemoUtils.getResourceFile("flower.svgz"));
			Document doc;
			try (InputStream in = new GZIPInputStream(source.getInputStream())) {
				doc = loader.loadDocument(source.getURI().toString(), in);
			}
			BridgeContext ctx = new BridgeContext(userAgent);
			ctx.setDynamicState(BridgeContext.STATIC);
			GVTBuilder gvtbuilder = new GVTBuilder();
			final GraphicsNode gvtRoot = gvtbuilder.build(ctx, doc);
			Dimension2D dim = ctx.getDocumentSize();
			final SVGImage svg = new SVGImage(gvtRoot, dim.getWidth(), dim.getHeight());

			try (PDFGraphicsOutput page = pdf.nextPage(width, height)) {
				PDFGC gc = new PDFGC(page);
				gc.drawImage(svg);
			}

			JFrame frame = new JFrame("Graphics") {
				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g) {
					super.paint(g);
					Graphics2D g2d = (Graphics2D) g;
					G2DGC gc = new G2DGC(g2d, pdf.getFontManager());
					try {
						gc.drawImage(svg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			frame.setSize((int) dim.getWidth(), (int) dim.getHeight());
			frame.setVisible(true);
			pdf.close();
			builder.close();
		}
	}
}


