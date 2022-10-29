package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfFragmentOutput;
import net.zamasoft.pdfg2d.pdf.gc.PdfGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PdfParams;

/**
 * オフスクリーン画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfGroupImageImpl.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class PdfGroupImageImpl extends PdfGroupImage {
	private final PdfFragmentOutput groupFlow, formFlow;
	private final ResourceFlow resourceFlow;

	public PdfGroupImageImpl(PdfWriterImpl pdfWriter, OutputStream out, PdfFragmentOutput groupFlow,
			ResourceFlow resourceFlow, double width, double height, String name, ObjectRef objectRef,
			PdfFragmentOutput formFlow) throws IOException {
		super(pdfWriter, out, width, height, name, objectRef);
		this.groupFlow = groupFlow;
		this.formFlow = formFlow;
		this.resourceFlow = resourceFlow;
	}

	private PdfWriterImpl getPDFWriterImpl() {
		return (PdfWriterImpl) this.pdfWriter;
	}

	public void useResource(String type, String name) throws IOException {
		ResourceFlow resourceFlow = this.resourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		Map<String, ObjectRef> nameToResourceRef = this.getPDFWriterImpl().nameToResourceRef;

		ObjectRef objectRef = (ObjectRef) nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	public void close() throws IOException {
		if (this.ocgFlags != 0) {
			PdfWriterImpl pdfWriter = this.getPDFWriterImpl();
			if (pdfWriter.getParams().getVersion() < PdfParams.VERSION_1_5) {
				throw new UnsupportedOperationException("OCG feature requres PDF >= 1.5.");
			}

			// intent
			this.formFlow.writeName("OC");
			ObjectRef ocgRef = pdfWriter.nextOCG();
			this.formFlow.writeObjectRef(ocgRef);
			this.formFlow.lineBreak();
			this.formFlow.close();

			PdfFragmentOutputImpl objectsFlow = pdfWriter.objectsFlow;
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
