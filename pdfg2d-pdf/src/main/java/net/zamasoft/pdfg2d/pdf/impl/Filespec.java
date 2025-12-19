package net.zamasoft.pdfg2d.pdf.impl;

import net.zamasoft.pdfg2d.pdf.Attachment;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * Represents a file specification in a PDF document.
 * 
 * @param attachment The attachment information.
 * @param name       The name of the file.
 * @param ref        The object reference.
 */
record Filespec(Attachment attachment, String name, ObjectRef ref) {
}
