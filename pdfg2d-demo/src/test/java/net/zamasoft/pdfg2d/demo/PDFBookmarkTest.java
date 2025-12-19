package net.zamasoft.pdfg2d.demo;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.zamasoft.pdfg2d.io.impl.StreamSequentialOutput;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

public class PDFBookmarkTest {

    @Test
    public void testBookmarks() throws Exception {
        final var tempFile = File.createTempFile("test-bookmarks", ".pdf");
        tempFile.deleteOnExit();

        final var params = new PDFParams();
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

        try (final var out = new FileOutputStream(tempFile)) {
            final var builder = new StreamSequentialOutput(out);
            final var pdf = new PDFWriterImpl(builder, params);

            try (final var gfx = pdf.nextPage(595, 842)) {
                // Add bookmark
                if (gfx instanceof final PDFPageOutput page) {
                    // Add bookmark pointing to (0, 842) - top of page
                    page.startBookmark("Chapter 1", new java.awt.geom.Point2D.Double(0, 842));
                    page.endBookmark();
                }
            }
            pdf.close();
            builder.close();
        }

        try (final var doc = Loader.loadPDF(tempFile)) {
            final var outline = doc.getDocumentCatalog().getDocumentOutline();
            Assertions.assertNotNull(outline, "Document outline should exist");
            final var item = outline.getFirstChild();
            Assertions.assertNotNull(item, "Should have at least one bookmark");
            Assertions.assertEquals("Chapter 1", item.getTitle());
        }
    }
}
