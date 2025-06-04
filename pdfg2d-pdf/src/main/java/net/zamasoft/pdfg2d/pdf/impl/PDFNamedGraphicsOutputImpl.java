package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.PDFNamedGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PDFPatternOutputImpl.java,v 1.1 2005/06/02 10:09:25 harumanx
 *          Exp $
 */
class PDFNamedGraphicsOutputImpl extends PDFNamedGraphicsOutput {
	private final PDFFragmentOutput patternFlow;

	private final ResourceFlow resourceFlow;

	private final String name;

	public PDFNamedGraphicsOutputImpl(PDFWriter pdfWriter, OutputStream out, PDFFragmentOutput patternFlow,
			ResourceFlow resourceFlow, double width, double height, String name) throws IOException {
		super(pdfWriter, out, width, height);
		this.patternFlow = patternFlow;
		this.resourceFlow = resourceFlow;
		this.name = name;
	}

	private PDFWriterImpl getPDFWriterImpl() {
		return (PDFWriterImpl) this.pdfWriter;
	}

	public void useResource(String type, String name) throws IOException {
		ResourceFlow resourceFlow = this.resourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		@SuppressWarnings("resource")
		Map<String, ObjectRef> nameToResourceRef = this.getPDFWriterImpl().nameToResourceRef;

		ObjectRef objectRef = (ObjectRef) nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	public String getName() {
		return this.name;
	}

	public void close() throws IOException {
		super.close();
		this.patternFlow.close();
		this.resourceFlow.close();
	}
}