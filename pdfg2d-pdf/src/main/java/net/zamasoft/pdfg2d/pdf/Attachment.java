package net.zamasoft.pdfg2d.pdf;

/**
 * Represents an attachment in a PDF document.
 * 
 * @param description The description of the attachment.
 * @param mimeType    The MIME type of the attachment.
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record Attachment(String description, String mimeType) {
    /**
     * Creates an attachment with no description or MIME type.
     */
    public Attachment() {
        this(null, null);
    }
}
