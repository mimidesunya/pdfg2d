package net.zamasoft.pdfg2d.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jp.cssj.rsr.impl.StreamRandomBuilder;
import net.zamasoft.pdfg2d.demo.utils.TextInspector;
import net.zamasoft.pdfg2d.g2d.gc.BridgeGraphics2D;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

public class TextRenderingTest {

    @Test
    public void testTextAttributes() throws Exception {
        File tempFile = File.createTempFile("test-text-rendering", ".pdf");
        tempFile.deleteOnExit();

        // 1. Generate PDF with text
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            StreamRandomBuilder builder = new StreamRandomBuilder(out);
            PDFWriter pdf = new PDFWriterImpl(builder, new PDFParams());

            try (PDFGraphicsOutput page = pdf.nextPage(400, 400)) {
                Graphics2D g = new BridgeGraphics2D(new PDFGC(page));

                g.setColor(Color.BLACK);
                g.setFont(new Font("Serif", Font.PLAIN, 12));
                g.drawString("TestString", 50, 100);
            }
            pdf.close();
            builder.close();
        }

        // 2. Verify with TextInspector
        try (PDDocument document = Loader.loadPDF(tempFile)) {
            TextInspector inspector = new TextInspector();
            String fullText = inspector.getText(document);
            assertTrue(fullText.contains("TestString"), "Text content missing");

            List<TextInspector.TextInfo> infos = inspector.getTextInfos();
            // Filter info for our string
            List<TextInspector.TextInfo> targetInfos = infos.stream()
                    .filter(i -> i.text.equals("T")) // Check first char position roughly
                    .collect(Collectors.toList());

            assertFalse(targetInfos.isEmpty(), "Character info not found");

            // Check Y position
            float y = targetInfos.get(0).y;
            assertEquals(100.0f, y, 5.0f, "Text Y position should be roughly 100");
        }
    }
}
