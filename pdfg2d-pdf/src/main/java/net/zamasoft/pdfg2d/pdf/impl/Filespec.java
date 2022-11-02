package net.zamasoft.pdfg2d.pdf.impl;

import net.zamasoft.pdfg2d.pdf.Attachment;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

class Filespec {
	final Attachment attachment;

	final String name;

	final ObjectRef ref;

	Filespec(Attachment attachment, String name, ObjectRef ref) {
		this.attachment = attachment;
		this.name = name;
		this.ref = ref;
	}
}
