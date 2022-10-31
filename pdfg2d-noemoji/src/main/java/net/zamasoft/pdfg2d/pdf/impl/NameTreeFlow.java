package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
abstract class NameTreeFlow {
	public final PDFFragmentOutputImpl out;

	private final PDFWriterImpl pdfWriter;

	private final NameDictionaryFlow nameDict;

	private final String key;

	private SortedMap<String, Object> nameToEntry = null;

	public NameTreeFlow(PDFWriterImpl pdfWriter, String key) throws IOException {
		this.pdfWriter = pdfWriter;
		this.key = key;

		PDFFragmentOutputImpl mainFlow = pdfWriter.mainFlow;
		this.out = mainFlow.forkFragment();
		this.nameDict = pdfWriter.nameDict;
	}

	public void addEntry(String name, Object entry) {
		if (this.nameToEntry == null) {
			this.nameToEntry = new TreeMap<String, Object>();
		}
		this.nameToEntry.put(name, entry);
	}

	public void close() throws IOException {
		if (this.nameToEntry != null) {
			ObjectRef destsRef = this.pdfWriter.xref.nextObjectRef();
			this.nameDict.addEntry(this.key, destsRef);

			this.out.startObject(destsRef);
			this.out.startHash();

			// PDF 1.2以前ではトップレベルのNames配列がサポートされていない
			if (this.pdfWriter.params.getVersion() <= PDFParams.VERSION_1_2) {
				this.out.writeName("Kids");
				this.out.startArray();
				ObjectRef destsKidRef = this.pdfWriter.xref.nextObjectRef();
				this.out.writeObjectRef(destsKidRef);
				this.out.endArray();
				this.out.lineBreak();

				this.out.endHash();
				this.out.endObject();

				this.out.startObject(destsKidRef);
				this.out.startHash();

				this.out.writeName("Limits");
				this.out.startArray();
				this.out.writeText((String) this.nameToEntry.firstKey());
				this.out.writeText((String) this.nameToEntry.lastKey());
				this.out.endArray();
				this.out.lineBreak();
			}

			this.out.writeName("Names");
			this.out.startArray();
			for (Iterator<Map.Entry<String, Object>> i = this.nameToEntry.entrySet().iterator(); i.hasNext();) {
				Map.Entry<String, Object> entry = i.next();
				this.out.writeText((String) entry.getKey());
				this.writeEntry(entry.getValue());
			}
			this.out.endArray();
			this.out.lineBreak();

			this.out.endHash();
			this.out.endObject();
		}
		this.out.close();
	}

	protected abstract void writeEntry(Object entry) throws IOException;
}
