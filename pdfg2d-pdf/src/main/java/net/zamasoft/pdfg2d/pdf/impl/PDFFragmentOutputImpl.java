package net.zamasoft.pdfg2d.pdf.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import net.zamasoft.pdfg2d.io.util.FragmentOutputAdapter;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCII85OutputStream;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCIIHexOutputStream;
import net.zamasoft.pdfg2d.pdf.util.encryption.Encryptor;
import net.zamasoft.pdfg2d.pdf.util.io.FastBufferedOutputStream;

/**
 * Implementation of PDFFragmentOutput.
 * 
 * @author MIYABE Tatsuhiko
 */
class PDFFragmentOutputImpl extends PDFFragmentOutput {
	private final PDFWriterImpl pdfWriter;

	/** Self and next fragment ID. */
	private int id, anchorId = -1;

	/** Output byte count. */
	private int length = 0;

	/** Writing stream. */
	private PDFFragmentOutputImpl streamLengthFlow = null;

	/** Stream start position. */
	private int startStreamPosition = 0;

	/** Current object reference. */
	private ObjectRef currentRef;

	private byte[] buff = null;

	public PDFFragmentOutputImpl(final OutputStream out, final PDFWriterImpl pdfWriter, final int id, final int nextId,
			final ObjectRef currentRef) throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		this.pdfWriter = pdfWriter;
		this.id = id;
		this.anchorId = nextId;
		this.currentRef = currentRef;
	}

	protected byte[] getBuff() {
		if (this.buff == null) {
			this.buff = new byte[PDFWriterImpl.BUFFER_SIZE];
		}
		return this.buff;
	}

	protected PDFFragmentOutputImpl forkFragment() throws IOException {
		this.close();
		final int nextId = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			this.pdfWriter.builder.addFragment();
		} else {
			this.pdfWriter.builder.insertFragmentBefore(this.anchorId);
		}
		final OutputStream out = new FragmentOutputAdapter(this.pdfWriter.builder, nextId);
		this.id = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			this.pdfWriter.builder.addFragment();
		} else {
			this.pdfWriter.builder.insertFragmentBefore(this.anchorId);
		}
		final PDFFragmentOutputImpl newFragOut = new PDFFragmentOutputImpl(out, this.pdfWriter, nextId, this.id,
				this.currentRef);
		this.out = new FragmentOutputAdapter(this.pdfWriter.builder, this.id);
		this.length = 0;
		return newFragOut;
	}

	@Override
	public void startObject(final ObjectRef ref) throws IOException {
		this.breakBefore();
		((ObjectRefImpl) ref).setPosition(this.id, this.getLength());
		this.writeInt(ref.objectNumber());
		this.writeInt(ref.generationNumber());
		this.writeOperator("obj");
		this.lineBreak();
		if (this.currentRef != null) {
			throw new IllegalStateException("Already inside object: " + this.currentRef);
		}
		this.currentRef = ref;
	}

	@Override
	public void endObject() throws IOException {
		this.writeLine("endobj");
		if (this.currentRef == null) {
			throw new IllegalStateException("Already outside object");
		}
		this.currentRef = null;
	}

	@Override
	public OutputStream startStream(final Mode mode) throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("Cannot nest streams: " + this.streamLengthFlow);
		}
		this.startHash();

		return this.startStreamFromHash(mode);
	}

	@Override
	@SuppressWarnings("resource")
	public OutputStream startStreamFromHash(final Mode mode) throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("Cannot nest streams: " + this.streamLengthFlow);
		}

		switch (mode) {
			case RAW:
				break;

			case ASCII:
				switch (this.pdfWriter.params.getCompression()) {
					case NONE:
						break;
					case ASCII:
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCII85Decode");
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
						break;
					case BINARY:
						this.writeName("Filter");
						this.startArray();
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
						break;
				}
				break;

			case BINARY:
				switch (this.pdfWriter.params.getCompression()) {
					case NONE:
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCIIHexDecode");
						this.endArray();
						this.breakBefore();
						break;
					case ASCII:
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCII85Decode");
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
						break;
					case BINARY:
						this.writeName("Filter");
						this.startArray();
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
						break;
					default:
						throw new IllegalStateException();
				}
				break;
			default:
				throw new IllegalStateException();
		}

		this.writeName("Length");
		this.write(' ');
		this.streamLengthFlow = this.forkFragment();
		this.lineBreak();
		this.endHash();
		this.writeLine("stream");
		this.flush();
		this.startStreamPosition = this.getLength();

		OutputStream out = new FilterOutputStream(this) {
			@Override
			public void close() throws IOException {
				PDFFragmentOutputImpl.this.endStream();
			}
		};

		// Encryption
		if (this.pdfWriter.encryption != null) {
			out = this.pdfWriter.encryption.getEncryptor(this.currentRef).getOutputStream(out);
		}

		// Encodings
		switch (mode) {
			case RAW:
				break;

			case ASCII:
				switch (this.pdfWriter.params.getCompression()) {
					case NONE:
						break;
					case ASCII:
						out = new DeflaterOutputStream(new ASCII85OutputStream(out));
						break;
					case BINARY:
						out = new DeflaterOutputStream(out);
						break;
					default:
						throw new IllegalArgumentException();
				}
				out = new FastBufferedOutputStream(out, this.getBuff());
				break;

			case BINARY:
				switch (this.pdfWriter.params.getCompression()) {
					case NONE:
						out = new ASCIIHexOutputStream(out);
						break;
					case ASCII:
						out = new DeflaterOutputStream(new ASCII85OutputStream(out));
						break;
					case BINARY:
						out = new DeflaterOutputStream(out);
						break;
					default:
						throw new IllegalArgumentException();
				}
				out = new FastBufferedOutputStream(out, this.getBuff());
				break;
			default:
				throw new IllegalArgumentException();
		}

		return out;
	}

	/**
	 * Writes end of stream.
	 * 
	 * @throws IOException in case of I/O error
	 */
	protected void endStream() throws IOException {
		this.streamLengthFlow.writeInt(this.getLength() - this.startStreamPosition);
		this.streamLengthFlow.close();
		this.streamLengthFlow = null;
		this.startStreamPosition = 0;
		// In PDF/A-1, insert EOL before endstream, this length is not included in
		// Length
		this.lineBreak();
		this.writeLine("endstream");
	}

	@Override
	public void writeBytes16(final int c) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeBytes16(c);
			return;
		}
		final byte[] data = new byte[2];
		data[0] = (byte) ((c >> 8) & 0xFF);
		data[1] = (byte) (c & 0xFF);
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	@Override
	public void writeBytes16(final int[] a, final int off, final int len) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeBytes16(a, off, len);
			return;
		}
		final byte[] data = new byte[len * 2];
		for (int i = 0; i < len; ++i) {
			final int c = a[i + off];
			data[i * 2] = (byte) ((c >> 8) & 0xFF);
			data[i * 2 + 1] = (byte) (c & 0xFF);
		}
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	protected void writeEncryptedBytes8(byte[] data, int off, int len) throws IOException {
		final Encryptor e = this.pdfWriter.encryption.getEncryptor(this.currentRef);
		if (e.isBlock()) {
			data = e.blockEncrypt(data, off, len);
			off = 0;
			len = data.length;
		} else {
			e.fastEncrypt(data, off, len);
		}
		super.writeBytes8(data, off, len);
	}

	@Override
	public void writeString(final String str) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeString(str);
			return;
		}
		final byte[] data = str.getBytes("iso-8859-1");
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	@Override
	public void writeText(final String text) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeText(text);
			return;
		}
		this.writeUTF16(text);
	}

	@Override
	public void writeUTF16(final String text) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeUTF16(text);
			return;
		}
		final byte[] data = new byte[text.length() * 2 + 2];
		data[0] = (byte) 0xFE;
		data[1] = (byte) 0xFF;
		for (int i = 0; i < text.length(); ++i) {
			final char c = text.charAt(i);
			data[i * 2 + 2] = (byte) ((c >> 8) & 0xFF);
			data[i * 2 + 3] = (byte) (c & 0xFF);
		}
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	/**
	 * Returns current written byte count.
	 * 
	 * @return the length
	 */
	protected int getLength() {
		return this.length;
	}

	protected int getId() {
		return this.id;
	}

	@Override
	public void write(final byte[] buff, final int off, final int len) throws IOException {
		super.write(buff, off, len);
		this.length += len;
	}

	@Override
	public void write(final byte[] buff) throws IOException {
		super.write(buff);
		this.length += buff.length;
	}

	@Override
	public void write(final int c) throws IOException {
		super.write(c);
		this.length++;
	}

	@Override
	public void close() throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("Stream not closed");
		}
		if (this.out == null) {
			throw new IllegalStateException("Already closed");
		}
		super.close();
		this.out = null;
	}
}