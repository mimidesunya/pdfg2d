package net.zamasoft.pdfg2d.demo;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import net.zamasoft.pdfg2d.PDFGraphics2D;
import net.zamasoft.pdfg2d.io.impl.AbstractTempFileOutput;
import net.zamasoft.pdfg2d.io.impl.FileFragmentedOutput;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Demonstrates the performance of recording PDF content using PdfBoxGraphics2D.
 * <p>
 * This demo downloads a large PDF report (OECD Education at a Glance 2024),
 * renders each page onto a PdfBoxGraphics2D context, and saves the result as a
 * new PDF.
 * This effectively measures the time taken to re-record PDF drawing commands.
 * </p>
 */
public class PdfBoxGraphics2dPerformanceDemo {
    private static final String PDF_URL = "https://www.oecd.org/content/dam/oecd/en/publications/reports/2024/09/education-at-a-glance-2024_5ea68448/c00cad36-en.pdf";
    private static final String FILE_NAME = "education-at-a-glance-2024.pdf";

    public static void main(final String[] args) throws IOException {
        final var file = new File(DemoUtils.getOutputDir(), FILE_NAME);
        if (!file.exists()) {
            System.out.println("Downloading " + PDF_URL + "...");
            try (final var in = java.net.URI.create(PDF_URL).toURL().openStream()) {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Downloaded to " + file.getAbsolutePath());
        } else {
            System.out.println("Using existing file " + file.getAbsolutePath());
        }

        measureZamasoftPDFGraphics2D(file);
        System.out.println("--------------------------------------------------");
        measureRototorPdfBoxGraphics2D(file);
    }

    private static void measureRototorPdfBoxGraphics2D(final File file) throws IOException {
        System.out.println("Measuring de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D...");
        final var outFile = new File(DemoUtils.getOutputDir(), "performance-test-rototor.pdf");

        try (final var sourceDoc = Loader.loadPDF(file);
                final var destDoc = new PDDocument()) {

            final var renderer = new PDFRenderer(sourceDoc);
            final int pageCount = sourceDoc.getNumberOfPages();

            System.out.println("Starting rendering of " + pageCount + " pages...");
            final long startTime = System.currentTimeMillis();

            for (int i = 0; i < pageCount; i++) {
                final var sourcePage = sourceDoc.getPage(i);
                final var mediaBox = sourcePage.getMediaBox();

                // Create graphics
                final var g2d = new PdfBoxGraphics2D(destDoc, mediaBox.getWidth(), mediaBox.getHeight());

                // Render source to graphics
                // Note: renderPageToGraphics renders the page content to the Graphics2D.
                // Since PdfBoxGraphics2D is a vector graphics context, this effectively
                // re-records the PDF drawing commands.
                renderer.renderPageToGraphics(i, g2d);
                g2d.dispose();

                // Create form object from the recorded graphics
                final var xFormObject = g2d.getXFormObject();

                // Create destination page with the same size
                final var destPage = new PDPage(mediaBox);
                destDoc.addPage(destPage);

                // Draw the form object onto the destination page
                try (final var contentStream = new PDPageContentStream(destDoc, destPage)) {
                    contentStream.drawForm(xFormObject);
                }

                System.out.print("\rRototor Rendered page " + (i + 1) + "/" + pageCount);
            }
            System.out.println(); // Newline after progress

            final long endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;

            destDoc.save(outFile);

            System.out.println("Completed in " + duration + "ms");
            System.out.println("Output saved to " + outFile.getAbsolutePath());
        }
    }

    private static void measureZamasoftPDFGraphics2D(final File file) throws IOException {
        System.out.println("Measuring net.zamasoft.pdfg2d.PDFGraphics2D...");
        final var outFile = new File(DemoUtils.getOutputDir(), "performance-test-zamasoft.pdf");

        try (final var sourceDoc = Loader.loadPDF(file)) {
            final var params = new PDFParams();

            final var config = new AbstractTempFileOutput.Config(
                    2 * 1024 * 1024, // fragmentBufferSize: 16MB
                    512 * 1024 * 1024, // totalBufferSize: 512MB
                    2 * 1024 * 1024, // threshold: 16MB
                    8192 // segmentSize: 16MB
            );

            try (final var pdfWriter = new PDFWriterImpl(new FileFragmentedOutput(outFile, config), params)) {
                final var renderer = new PDFRenderer(sourceDoc);
                final int pageCount = sourceDoc.getNumberOfPages();

                System.out.println("Starting rendering of " + pageCount + " pages...");
                final long startTime = System.currentTimeMillis();

                for (int i = 0; i < pageCount; i++) {
                    final var sourcePage = sourceDoc.getPage(i);
                    final var mediaBox = sourcePage.getMediaBox();

                    // Create graphics for the new page
                    try (final var g2d = new PDFGraphics2D(
                            pdfWriter.nextPage(mediaBox.getWidth(), mediaBox.getHeight()))) {
                        // Render source to graphics
                        renderer.renderPageToGraphics(i, g2d);
                    }

                    System.out.print("\rZamasoft Rendered page " + (i + 1) + "/" + pageCount);
                }
                System.out.println(); // Newline after progress

                final long endTime = System.currentTimeMillis();
                final long duration = endTime - startTime;

                System.out.println("Completed in " + duration + "ms");
                System.out.println("Output saved to " + outFile.getAbsolutePath());
            } catch (Exception e) {
                // PDFWriterImpl throws Exception on close but it's AutoCloseable as well with
                // strict exceptions?
                // Actually PDFWriterImpl throws Exception on close() because PDFWriter
                // interface extends AutoCloseable but throws Exception.
                // We wrap IOExceptions or handle them.
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else {
                    throw new IOException(e);
                }
            }
        }
    }
}
