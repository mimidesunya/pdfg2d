package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Base class for managing PDF name trees.
 * Name trees are used to map string keys to PDF objects (e.g., Destinations,
 * embedded files).
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
abstract class NameTreeFlow {
	public final PDFFragmentOutputImpl out;

	private final PDFWriterImpl pdfWriter;

	private final NameDictionaryFlow nameDict;

	private final String key;

	private SortedMap<String, Object> nameToEntry = null;

	public NameTreeFlow(final PDFWriterImpl pdfWriter, final String key) throws IOException {
		this.pdfWriter = pdfWriter;
		this.key = key;

		final var mainFlow = pdfWriter.mainFlow;
		this.out = mainFlow.forkFragment();
		this.nameDict = pdfWriter.nameDict;
	}

	public void addEntry(final String name, final Object entry) {
		if (this.nameToEntry == null) {
			this.nameToEntry = new TreeMap<>();
		}
		this.nameToEntry.put(name, entry);
	}

	public void close() throws IOException {
		if (this.nameToEntry != null) {
			final var xref = this.pdfWriter.xref;
			final var rootRef = xref.nextObjectRef();
			this.nameDict.addEntry(this.key, rootRef);

			this.out.startObject(rootRef);
			this.out.startHash();

			// PDF 1.2 or earlier does not support top-level Names array in the root node.
			// It requires a Kids array even if there is only one page-level node.
			final var version = this.pdfWriter.params.getVersion();
			if (version.v <= PDFParams.Version.V_1_2.v) {
				this.out.writeName("Kids");
				this.out.startArray();
				final var kidRef = xref.nextObjectRef();
				this.out.writeObjectRef(kidRef);
				this.out.endArray();
				this.out.lineBreak();

				this.out.endHash();
				this.out.endObject();

				this.out.startObject(kidRef);
				this.out.startHash();

				this.out.writeName("Limits");
				this.out.startArray();
				this.out.writeText(this.nameToEntry.firstKey());
				this.out.writeText(this.nameToEntry.lastKey());
				this.out.endArray();
				this.out.lineBreak();
			}

			this.out.writeName("Names");
			this.out.startArray();
			for (final var entry : this.nameToEntry.entrySet()) {
				this.out.writeText(entry.getKey());
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
