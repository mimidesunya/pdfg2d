package jp.cssj.sakae.example;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import jp.cssj.sakae.pdf.PdfMetaInfo;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.PdfParams;
import jp.cssj.sakae.pdf.params.ViewerPreferences;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ViewerPreferencesDemo {
	public static void main(String[] args) throws Exception {
		PdfParams params = new PdfParams();
		params.setVersion(PdfParams.VERSION_1_7);
		params.setCompression(PdfParams.COMPRESSION_NONE);

		PdfMetaInfo meta = params.getMetaInfo();
		meta.setTitle("タイトル");

		ViewerPreferences vp = params.getViewerPreferences();
		// vp.setHideToolbar(true);
		// vp.setHideMenubar(true);
		// vp.setHideWindowUI(true);
		// vp.setFitWindow(true);
		// vp.setCenterWindow(true);
		// vp.setDisplayDocTitle(true);
		vp.setNonFullScreenPageMode(ViewerPreferences.NONE_FULL_SCREEN_PAGE_MODE_USE_THUMBS);
		vp.setPickTrayByPDFSize(true);

		vp.setPrintPageRange(new int[] { 2, 3, 5, 7, 8, 9 });
		vp.setNumCopies(4);
		vp.setViewArea(ViewerPreferences.AREA_BLEED_BOX);
		vp.setViewClip(ViewerPreferences.AREA_BLEED_BOX);
		vp.setPrintScaling(ViewerPreferences.PRINT_SCALING_NONE);
		params.setViewerPreferences(vp);

		final double width = 300;
		final double height = 300;

		try (OutputStream out = new BufferedOutputStream(new FileOutputStream("local/test.pdf"))) {
			StreamRandomBuilder builder = new StreamRandomBuilder(out);
			final PdfWriter pdf = new PdfWriterImpl(builder, params);

			for (int i = 0; i < 10; ++i) {
				try (PdfPageOutput page = pdf.nextPage(width, height)) {
					page.setBleedBox(new Rectangle2D.Double(10, 10, 280, 280));

					PdfGC gc = new PdfGC(page);
					gc.fill(new Rectangle2D.Double(10, 10, 280, 280));
				}
			}

			pdf.finish();
			builder.finish();
		}
	}
}
