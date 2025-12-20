package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Implementation of an offscreen group image (Form XObject).
 * This class handles resource management and Optional Content Group (OCG)
 * features for group-based PDF content.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFGroupImageImpl extends PDFGroupImage {
	private final PDFFragmentOutput groupFlow, formFlow;
	private final ResourceFlow resourceFlow;

	public PDFGroupImageImpl(final PDFWriterImpl pdfWriter, final OutputStream out, final PDFFragmentOutput groupFlow,
			final ResourceFlow resourceFlow, final double width, final double height, final String name,
			final ObjectRef objectRef, final PDFFragmentOutput formFlow) throws IOException {
		super(pdfWriter, out, width, height, name, objectRef);
		this.groupFlow = groupFlow;
		this.formFlow = formFlow;
		this.resourceFlow = resourceFlow;
	}

	private PDFWriterImpl getPDFWriterImpl() {
		return (PDFWriterImpl) this.pdfWriter;
	}

	public void useResource(final String type, final String name) throws IOException {
		if (this.resourceFlow.contains(name)) {
			return;
		}
		final var nameToResourceRef = this.getPDFWriterImpl().nameToResourceRef;
		final var objectRef = nameToResourceRef.get(name);
		this.resourceFlow.put(type, name, objectRef);
	}

	public void close() throws IOException {
		if (this.ocgFlags != 0) {
			final var pdfWriter = this.getPDFWriterImpl();
			if (pdfWriter.getParams().getVersion().v < PDFParams.Version.V_1_5.v) {
				throw new UnsupportedOperationException("OCG feature requires PDF >= 1.5.");
			}

			// Add Optional Content reference to the Form Dictionary
			this.formFlow.writeName("OC");
			final var ocgRef = pdfWriter.nextOCG();
			this.formFlow.writeObjectRef(ocgRef);
			this.formFlow.lineBreak();
			this.formFlow.close();

			// Define the Optional Content Group object
			final var objectsFlow = pdfWriter.objectsFlow;
			objectsFlow.startObject(ocgRef);
			objectsFlow.startHash();
			objectsFlow.writeName("Type");
			objectsFlow.writeName("OCG");
			objectsFlow.writeName("Name");
			objectsFlow.writeText("WATERMARK");
			objectsFlow.writeName("Usage");
			objectsFlow.startHash();

			objectsFlow.writeName("View");
			objectsFlow.startHash();
			objectsFlow.writeName("ViewState");
			objectsFlow.writeName((this.ocgFlags & VIEW_OFF) != 0 ? "OFF" : "ON");
			objectsFlow.endHash();

			objectsFlow.writeName("Print");
			objectsFlow.startHash();
			objectsFlow.writeName("PrintState");
			objectsFlow.writeName((this.ocgFlags & PRINT_OFF) != 0 ? "OFF" : "ON");
			objectsFlow.endHash();

			objectsFlow.endHash(); // End Usage
			objectsFlow.endHash(); // End OCG
			objectsFlow.endObject();
		}

		super.close();
		this.groupFlow.close();
		this.resourceFlow.close();
	}
}
