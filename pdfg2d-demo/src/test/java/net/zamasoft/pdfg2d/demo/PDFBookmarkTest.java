package net.zamasoft.pdfg2d.demo;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

public class PDFBookmarkTest {

    @Test
    public void testBookmarks() throws Exception {
        File tempFile = File.createTempFile("test-bookmarks", ".pdf");
        tempFile.deleteOnExit();

        PDFParams params = new PDFParams();
        params.setBookmarks(true); // Enable bookmarks generation support (if applicable directly via API)

        // Note: PDFG2D creates bookmarks usually via 'PDFFragmentOutput' or specific
        // API calls.
        // Looking at PDFWriter interface:
        // PDFGraphicsOutput has methods for outlines/bookmarks?
        // Let's check PDFWriter or PDFGraphicsOutput.
        // Actually, assuming standard flow: PDFWriter -> nextPage -> PDFGraphicsOutput.
        // If 'bookmarks' param is true, maybe it auto-generates from heading tags if
        // using high-level API?
        // But here we are using low-level G2D.
        // If there is no explicit addBookmark API in PDFGraphicsOutput, bookmarks might
        // be created
        // by structure or separate calls.

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            StreamRandomBuilder builder = new StreamRandomBuilder(out);
            PDFWriter pdf = new PDFWriterImpl(builder, params);

            try (PDFGraphicsOutput gfx = pdf.nextPage(595, 842)) {
                // Add bookmark
                if (gfx instanceof net.zamasoft.pdfg2d.pdf.PDFPageOutput) {
                    net.zamasoft.pdfg2d.pdf.PDFPageOutput page = (net.zamasoft.pdfg2d.pdf.PDFPageOutput) gfx;
                    // Add bookmark pointing to (0, 842) - top of page
                    page.startBookmark("Chapter 1", new java.awt.geom.Point2D.Double(0, 842));
                    page.endBookmark();
                }
            }
            pdf.close();
            builder.close();
        }

        try (PDDocument doc = Loader.loadPDF(tempFile)) {
            PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
            Assertions.assertNotNull(outline, "Document outline should exist");
            PDOutlineItem item = outline.getFirstChild();
            Assertions.assertNotNull(item, "Should have at least one bookmark");
            Assertions.assertEquals("Chapter 1", item.getTitle());
        }
    }
}
