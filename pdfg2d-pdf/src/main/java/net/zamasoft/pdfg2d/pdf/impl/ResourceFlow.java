package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.zamasoft.pdfg2d.pdf.ObjectRef;

class ResourceFlow {
	private final PDFFragmentOutputImpl out;

	private final Map<String, PDFFragmentOutputImpl> typeToFlow = new TreeMap<>();
	private final List<PDFFragmentOutputImpl> flowList = new ArrayList<>();
	private final Map<String, ObjectRef> idToObjectRef = new HashMap<>();

	public ResourceFlow(final PDFFragmentOutputImpl flow) throws IOException {
		flow.startHash();
		flow.writeName("ProcSet");
		flow.startArray();
		flow.writeName("PDF");
		flow.writeName("Text");
		flow.writeName("ImageB");
		flow.writeName("ImageC");
		flow.writeName("ImageI");
		flow.endArray();
		flow.lineBreak();
		this.out = flow.forkFragment();
		flow.endHash();
	}

	private PDFFragmentOutputImpl getFlow(final String type) throws IOException {
		PDFFragmentOutputImpl flow = this.typeToFlow.get(type);
		if (flow == null) {
			flow = this.out.forkFragment();
			this.typeToFlow.put(type, flow);
			this.flowList.add(flow);
			flow.writeName(type);
			flow.startHash();
		}
		return flow;
	}

	public boolean contains(final String name) {
		return this.idToObjectRef.containsKey(name);
	}

	/**
	 * Adds an object.
	 * 
	 * @param type      type ("Font", "XObject", etc.)
	 * @param name      name used for reference
	 * @param objectRef object reference
	 * @throws IOException in case of I/O error
	 */
	public void put(final String type, final String name, final ObjectRef objectRef) throws IOException {
		assert !this.contains(name);
		final PDFFragmentOutputImpl flow = this.getFlow(type);
		flow.writeName(name);
		flow.writeObjectRef(objectRef);
		this.idToObjectRef.put(name, objectRef);
	}

	public void close() throws IOException {
		for (final PDFFragmentOutputImpl flow : this.flowList) {
			try (flow) {
				flow.endHash();
			}
		}
	}
}
