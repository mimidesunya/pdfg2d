package net.zamasoft.pdfg2d.demo;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.pdf.PDFMetaInfo;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.params.ViewerPreferences;

/**
 * Demonstrates various viewer preferences.
 * <p>
 * This demo sets PDF viewer preferences such as full screen mode,
 * window centering, and display options like hiding the toolbar or menubar.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ViewerPreferencesDemo {
	public static void main(String[] args) throws Exception {
		PDFParams params = new PDFParams();
		params.setCompression(PDFParams.Compression.NONE);
		params.setVersion(PDFParams.Version.V_1_7);

		PDFMetaInfo meta = params.getMetaInfo();
		meta.setTitle("タイトル");

		ViewerPreferences prefs = params.getViewerPreferences();
		prefs.setNonFullScreenPageMode(ViewerPreferences.NonFullScreenPageMode.THUMBS);
		prefs.setPickTrayByPDFSize(true);
		prefs.setPrintPageRange(new int[] { 2, 3, 5, 7, 8, 9 });
		prefs.setNumCopies(4);
		prefs.setViewArea(ViewerPreferences.AreaBox.BLEED);
		prefs.setViewClip(ViewerPreferences.AreaBox.BLEED);
		prefs.setPrintScaling(ViewerPreferences.PrintScaling.NONE);
		params.setViewerPreferences(prefs);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(DemoUtils.getOutputDir(), "viewer-preferences.pdf")))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PDFWriter pdf = new PDFWriterImpl(builder, params);

			for (int i = 0; i < 10; ++i) {
				try (PDFPageOutput page = pdf.nextPage(width, height)) {
					page.setBleedBox(new Rectangle2D.Double(10, 10, 280, 280));

					PDFGC gc = new PDFGC(page);
					gc.fill(new Rectangle2D.Double(10, 10, 280, 280));
				}
			}

			pdf.close();
			builder.close();
		}
	}
}
