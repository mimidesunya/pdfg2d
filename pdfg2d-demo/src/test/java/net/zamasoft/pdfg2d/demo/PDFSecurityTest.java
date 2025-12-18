package net.zamasoft.pdfg2d.demo;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.zamasoft.pdfg2d.io.impl.OutputFragmentedStream;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.impl.PDFWriterImpl;
import net.zamasoft.pdfg2d.pdf.params.EncryptionParams;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.params.R3Permissions;
import net.zamasoft.pdfg2d.pdf.params.V2EncryptionParams; // 128-bit RC4 (V2)

public class PDFSecurityTest {

    @Test
    public void testPermissionsAndEncryptionRC4() throws Exception {
        File tempFile = File.createTempFile("test-security-rc4", ".pdf");
        tempFile.deleteOnExit();

        PDFParams params = new PDFParams();

        // Setup RC4 128-bit encryption (V2EncryptionParams indicates V2 algorithm,
        // typically standard 40-128 bit RC4)
        V2EncryptionParams encParams = new V2EncryptionParams();
        encParams.setLength(128); // 128-bit
        encParams.setUserPassword("user123");
        encParams.setOwnerPassword("owner123");

        // Permissions
        R3Permissions perms = encParams.getPermissions();
        perms.setPrint(true);
        perms.setCopy(false); // Disallow copying
        perms.setModify(false); // Disallow msg

        params.setEncription(encParams);

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            OutputFragmentedStream builder = new OutputFragmentedStream(out);
            PDFWriter pdf = new PDFWriterImpl(builder, params);
            try (PDFGraphicsOutput page = pdf.nextPage(595, 842)) {
                // content
            }
            pdf.close();
            builder.close();
        }

        // Verify with PDFBox
        try (PDDocument doc = Loader.loadPDF(tempFile, "user123")) {
            Assertions.assertTrue(doc.isEncrypted(), "Document should be encrypted");

            AccessPermission ap = doc.getCurrentAccessPermission();
            Assertions.assertTrue(ap.canPrint(), "Printing should be allowed");
            Assertions.assertFalse(ap.canExtractContent(), "Content extraction (copy) should NOT be allowed");
            Assertions.assertFalse(ap.canModify(), "Modification should NOT be allowed");
        }
    }
}
