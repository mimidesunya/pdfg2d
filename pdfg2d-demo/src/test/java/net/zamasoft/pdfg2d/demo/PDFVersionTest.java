package net.zamasoft.pdfg2d.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.params.PDFParams.Version;

public class PDFVersionTest {

    @ParameterizedTest
    @MethodSource("provideVersions")
    public void testPDFVersions(Version version) throws Exception {
        File tempFile = File.createTempFile("test-version-" + version, ".pdf");
        tempFile.deleteOnExit();

        PDFParams params = new PDFParams();
        params.setVersion(version);

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            OutputFragmentedStream builder = new OutputFragmentedStream(out);
            PDFWriter pdf = new PDFWriterImpl(builder, params);
            try (PDFGraphicsOutput page = pdf.nextPage(595, 842)) {
                // Empty page is enough to check version header
            }
            pdf.close();
            builder.close();
        }

        try (PDDocument doc = Loader.loadPDF(tempFile)) {
            float pdfVersion = doc.getVersion();
            // Map our enum to float version
            float expected = switch (version) {
                case V_1_2 -> 1.2f;
                case V_1_3 -> 1.3f;
                case V_1_4, V_PDFA1B, V_PDFX1A -> 1.4f; // PDF/A-1b and PDF/X-1a are based on 1.4
                case V_1_5 -> 1.5f;
                case V_1_6 -> 1.6f;
                case V_1_7 -> 1.7f;
            };

            Assertions.assertEquals(expected, pdfVersion, 0.001f, "PDF Version mismatch for " + version);
        }
    }

    private static Stream<Version> provideVersions() {
        return Stream.of(
                Version.V_1_2,
                Version.V_1_3,
                Version.V_1_4,
                Version.V_1_5,
                Version.V_1_6,
                Version.V_1_7,
                Version.V_PDFA1B,
                Version.V_PDFX1A);
    }
}
