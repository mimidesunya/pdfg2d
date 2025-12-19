package net.zamasoft.pdfg2d.pdf.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.zamasoft.pdfg2d.io.FragmentedOutput.PositionInfo;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.XRef;
import net.zamasoft.pdfg2d.pdf.util.encryption.Encryption;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class XRefImpl implements XRef {
	/** Cross-reference table. */
	private final List<ObjectRef> xref = new ArrayList<>();

	private final ObjectRef rootRef;

	protected final PDFFragmentOutputImpl mainFlow;

	private Map<String, Object> attributes;

	private static final byte[] EOF = { '%', '%', 'E', 'O', 'F' };

	XRefImpl(final PDFFragmentOutputImpl mainFlow) throws IOException {
		this.mainFlow = mainFlow;
		this.rootRef = this.nextObjectRef();
		this.mainFlow.startObject(rootRef);
	}

	/**
	 * Returns the next object ID.
	 * 
	 * @return the next object reference
	 */
	public ObjectRef nextObjectRef() {
		final var ref = new ObjectRefImpl(this.xref.size() + 1);
		this.xref.add(ref);
		return ref;
	}

	void close(final PositionInfo posInfo, final ObjectRef infoRef, final byte[][] fileid, final Encryption encrypter)
			throws IOException {
		// startxref
		int xrefPosition = (int) posInfo.getPosition(this.mainFlow.getId()) + this.mainFlow.getLength();

		// Trailer
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		try (PDFOutput trailerFlow = new PDFOutput(buff, "ISO-8859-1")) {
			trailerFlow.writeOperator("trailer");
			trailerFlow.startHash();

			trailerFlow.writeName("Size");
			trailerFlow.writeInt(this.xref.size() + 1);
			trailerFlow.lineBreak();

			trailerFlow.writeName("Root");
			trailerFlow.writeObjectRef(rootRef);
			trailerFlow.lineBreak();

			if (infoRef != null) {
				trailerFlow.writeName("Info");
				trailerFlow.writeObjectRef(infoRef);
				trailerFlow.lineBreak();
			}

			if (fileid != null) {
				trailerFlow.writeName("ID");
				trailerFlow.startArray();
				trailerFlow.writeBytes8(fileid[0], 0, fileid[0].length);
				trailerFlow.writeBytes8(fileid[1], 0, fileid[1].length);
				trailerFlow.endArray();
				trailerFlow.lineBreak();
			}

			if (encrypter != null) {
				trailerFlow.writeName("Encrypt");
				trailerFlow.writeObjectRef(encrypter.getObjectRef());
				trailerFlow.lineBreak();
			}

			trailerFlow.endHash();

			trailerFlow.writeOperator("startxref");
			trailerFlow.lineBreak();
			trailerFlow.writeInt(xrefPosition);

			trailerFlow.lineBreak();
			trailerFlow.write(EOF);
			trailerFlow.lineBreak();
		}
		String trailer = new String(buff.toByteArray(), "ISO-8859-1");

		// Cross-reference
		// Cross-reference
		this.mainFlow.writeOperator("xref");
		this.mainFlow.lineBreak();
		this.mainFlow.writeInt(0);
		this.mainFlow.writeInt(this.xref.size() + 1);
		writeXrefEntry(this.mainFlow, 0, 65535, false);
		// Offset is trailer length + xref length
		// Each xref entry is 20 bytes
		for (int i = 0; i < this.xref.size(); ++i) {
			ObjectRefImpl ref = (ObjectRefImpl) this.xref.get(i);
			writeXrefEntry(this.mainFlow, ref.getPosition(posInfo), ref.generationNumber(), true);
		}

		this.mainFlow.write(trailer);
	}

	private final byte[] work = new byte[10];

	private void writeXrefEntry(PDFFragmentOutputImpl out, long byteOffset, int generationNum, boolean inUse)
			throws IOException {
		out.breakBefore();
		{
			String str = String.valueOf(byteOffset);
			int off = 10 - str.length();
			for (int i = 0; i < off; ++i) {
				this.work[i] = '0';
			}
			for (int i = 0; i < str.length(); ++i) {
				this.work[i + off] = (byte) str.charAt(i);
			}
		}
		out.write(this.work, 0, 10);
		out.write(' ');

		{
			String str = String.valueOf(generationNum);
			int off = 5 - str.length();
			for (int i = 0; i < off; ++i) {
				this.work[i] = '0';
			}
			for (int i = 0; i < str.length(); ++i) {
				this.work[i + off] = (byte) str.charAt(i);
			}
		}
		out.write(this.work, 0, 5);
		out.write(' ');

		out.write(inUse ? 'n' : 'f');
		out.lineBreak();
	}

	public Object getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	public void setAttribute(String key, Object value) {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Object>();
		}
		this.attributes.put(key, value);
	}
}
