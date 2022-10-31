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

	private final Map<String, PDFFragmentOutputImpl> typeToFlow = new TreeMap<String, PDFFragmentOutputImpl>();
	private final List<PDFFragmentOutputImpl> flowList = new ArrayList<PDFFragmentOutputImpl>();
	private final Map<String, ObjectRef> idToObjectRef = new HashMap<String, ObjectRef>();

	public ResourceFlow(PDFFragmentOutputImpl flow) throws IOException {
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

	private PDFFragmentOutputImpl getFlow(String type) throws IOException {
		PDFFragmentOutputImpl flow = (PDFFragmentOutputImpl) this.typeToFlow.get(type);
		if (flow == null) {
			flow = this.out.forkFragment();
			this.typeToFlow.put(type, flow);
			this.flowList.add(flow);
			flow.writeName(type);
			flow.startHash();
		}
		return flow;
	}

	public boolean contains(String name) {
		return this.idToObjectRef.containsKey(name);
	}

	/**
	 * オブジェクトを追加します。 既に同じ名前のIDがあれば何もしません。
	 * 
	 * @param type
	 *            タイプ("Font","XObject"など)
	 * @param name
	 *            参照に使う名前
	 * @param objectRef
	 * @throws IOException
	 */
	public void put(String type, String name, ObjectRef objectRef) throws IOException {
		assert !this.contains(name);
		PDFFragmentOutputImpl flow = this.getFlow(type);
		flow.writeName(name);
		flow.writeObjectRef(objectRef);
		this.idToObjectRef.put(name, objectRef);
	}

	public void close() throws IOException {
		for (int i = 0; i < this.flowList.size(); ++i) {
			try (PDFFragmentOutputImpl flow = (PDFFragmentOutputImpl) this.flowList.get(i)) {
				flow.endHash();
			}
		}
	}
}
