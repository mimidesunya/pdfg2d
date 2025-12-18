package net.zamasoft.pdfg2d.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.demo.utils.GraphicsOperatorInspector;
import net.zamasoft.pdfg2d.g2d.gc.BridgeGraphics2D;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

public class GraphicsDrawingTest {

    @Test
    public void testShapeDrawingOperators() throws Exception {
        File tempFile = File.createTempFile("test-graphics-shapes", ".pdf");
        tempFile.deleteOnExit();

        // 1. Generate PDF with specific shapes
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            OutputFragmentedStream builder = new OutputFragmentedStream(out);
            PDFWriter pdf = new PDFWriterImpl(builder, new PDFParams());

            try (PDFGraphicsOutput page = pdf.nextPage(400, 400)) {
                Graphics2D g = new BridgeGraphics2D(new PDFGC(page));

                // Draw a red rectangle
                g.setColor(Color.RED);
                g.fillRect(10, 20, 100, 50);

                // Draw a blue line
                g.setColor(Color.BLUE);
                g.setStroke(new BasicStroke(2.0f));
                g.drawLine(200, 200, 300, 300);
            }
            pdf.close();
            builder.close();
        }

        // 2. Verify with Inspector
        try (PDDocument document = Loader.loadPDF(tempFile)) {
            GraphicsOperatorInspector inspector = new GraphicsOperatorInspector(document.getPage(0));
            inspector.run();
            List<GraphicsOperatorInspector.ShapeCommand> commands = inspector.getCommands();

            // Check for rectangle fill (usually 're' followed by 'f' or 'f*')
            boolean hasRedFill = commands.stream()
                    .anyMatch(cmd -> (cmd.operation.equals("f") || cmd.operation.equals("f*")) &&
                            cmd.currentColor[0] == 1.0f && cmd.currentColor[1] == 0.0f && cmd.currentColor[2] == 0.0f);
            assertTrue(hasRedFill, "Should have a red fill operation");

            // Check for blue stroke OR blue fill (if stroke is converted to filled shape)
            boolean hasBlue = commands.stream().anyMatch(
                    cmd -> (cmd.operation.equals("S") || cmd.operation.equals("f") || cmd.operation.equals("f*")) &&
                            cmd.currentColor[0] == 0.0f && cmd.currentColor[1] == 0.0f && cmd.currentColor[2] == 1.0f);
            assertTrue(hasBlue, "Should have a blue drawing operation");
        }
    }
}
