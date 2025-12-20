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
 * Implementation of the PDF cross-reference table (xref).
 * This class tracks object positions and generates the trailer and xref table
 * during the finalization of the PDF document.
 * 
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
	 * Creates and returns the next object reference.
	 * 
	 * @return A new ObjectRefImpl instance.
	 */
	public ObjectRef nextObjectRef() {
		final var ref = new ObjectRefImpl(this.xref.size() + 1);
		this.xref.add(ref);
		return ref;
	}

	/**
	 * Finalizes the PDF by writing the xref table and trailer.
	 * 
	 * @param posInfo   Position information of fragments.
	 * @param infoRef   Reference to the Info dictionary.
	 * @param fileid    The document IDs.
	 * @param encrypter Encryption settings, if any.
	 * @throws IOException If an I/O error occurs.
	 */
	void close(final PositionInfo posInfo, final ObjectRef infoRef, final byte[][] fileid,
			final Encryption encrypter) throws IOException {
		// Calculate the starting position of the xref table
		final int xrefPosition = (int) posInfo.getPosition(this.mainFlow.getId()) + this.mainFlow.getLength();

		// Generate trailer content in a memory buffer first
		final var trailerBytes = new ByteArrayOutputStream();
		try (final var trailerFlow = new PDFOutput(trailerBytes, "ISO-8859-1")) {
			trailerFlow.writeOperator("trailer");
			trailerFlow.startHash();

			trailerFlow.writeName("Size");
			trailerFlow.writeInt(this.xref.size() + 1);
			trailerFlow.lineBreak();

			trailerFlow.writeName("Root");
			trailerFlow.writeObjectRef(this.rootRef);
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
		final var trailer = trailerBytes.toString("ISO-8859-1");

		// Write xref table header
		this.mainFlow.writeOperator("xref");
		this.mainFlow.lineBreak();
		this.mainFlow.writeInt(0);
		this.mainFlow.writeInt(this.xref.size() + 1);

		// First entry is always the free object at generation 65535
		this.writeXrefEntry(this.mainFlow, 0, 65535, false);

		// Write actual object positions
		for (final var ref : this.xref) {
			final var impl = (ObjectRefImpl) ref;
			this.writeXrefEntry(this.mainFlow, impl.getPosition(posInfo), impl.generationNumber(), true);
		}

		// Append trailer content
		this.mainFlow.write(trailer);
	}

	private final byte[] work = new byte[10];

	/**
	 * Writes a 20-byte cross-reference table entry.
	 * format: nnnnnnnnnn ggggg f/n[EOL]
	 * 
	 * @param out           Output target.
	 * @param byteOffset    Byte offset of the object.
	 * @param generationNum Generation number.
	 * @param inUse         Whether the object is in use ('n') or free ('f').
	 * @throws IOException If an I/O error occurs.
	 */
	private void writeXrefEntry(final PDFFragmentOutputImpl out, final long byteOffset, final int generationNum,
			final boolean inUse) throws IOException {
		out.breakBefore();

		// Write 10-digit offset with leading zeros
		this.writeFixedNumber(out, byteOffset, 10);
		out.write(' ');

		// Write 5-digit generation number with leading zeros
		this.writeFixedNumber(out, generationNum, 5);
		out.write(' ');

		out.write(inUse ? 'n' : 'f');
		out.lineBreak();
	}

	/**
	 * Helper to write a zero-padded number of fixed width.
	 */
	private void writeFixedNumber(final PDFFragmentOutputImpl out, long val, final int width) throws IOException {
		for (var i = width - 1; i >= 0; --i) {
			this.work[i] = (byte) ('0' + (val % 10));
			val /= 10;
		}
		out.write(this.work, 0, width);
	}

	public Object getAttribute(final String key) {
		return (this.attributes == null) ? null : this.attributes.get(key);
	}

	public void setAttribute(final String key, final Object value) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		this.attributes.put(key, value);
	}
}
