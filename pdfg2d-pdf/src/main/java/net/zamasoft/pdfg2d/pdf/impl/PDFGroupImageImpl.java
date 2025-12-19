package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Offscreen image.
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
		final ResourceFlow resourceFlow = this.resourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		@SuppressWarnings("resource")
		final Map<String, ObjectRef> nameToResourceRef = this.getPDFWriterImpl().nameToResourceRef;

		final ObjectRef objectRef = nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	public void close() throws IOException {
		if (this.ocgFlags != 0) {
			final PDFWriterImpl pdfWriter = this.getPDFWriterImpl();
			if (pdfWriter.getParams().getVersion().v < PDFParams.Version.V_1_5.v) {
				throw new UnsupportedOperationException("OCG feature requires PDF >= 1.5.");
			}

			// intent
			this.formFlow.writeName("OC");
			final ObjectRef ocgRef = pdfWriter.nextOCG();
			this.formFlow.writeObjectRef(ocgRef);
			this.formFlow.lineBreak();
			this.formFlow.close();

			final PDFFragmentOutputImpl objectsFlow = pdfWriter.objectsFlow;
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
			objectsFlow.writeName(((this.ocgFlags & VIEW_OFF) != 0) ? "OFF" : "ON");
			objectsFlow.endHash();
			objectsFlow.writeName("Print");
			objectsFlow.startHash();
			objectsFlow.writeName("PrintState");
			objectsFlow.writeName(((this.ocgFlags & PRINT_OFF) != 0) ? "OFF" : "ON");
			objectsFlow.endHash();
			objectsFlow.endHash();
			objectsFlow.endHash();
			objectsFlow.endObject();
		}

		super.close();
		this.groupFlow.close();
		this.resourceFlow.close();
	}
}
