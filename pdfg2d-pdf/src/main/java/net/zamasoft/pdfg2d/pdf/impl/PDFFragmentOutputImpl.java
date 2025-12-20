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
import net.zamasoft.pdfg2d.pdf.util.io.FastBufferedOutputStream;

/**
 * Concrete implementation of PDFFragmentOutput.
 * This class handles the actual writing of PDF objects, streams, and encrypted
 * content
 * into fragmented output segments.
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

	public PDFFragmentOutputImpl(final OutputStream out, final PDFWriterImpl pdfWriter, final int id,
			final int nextId, final ObjectRef currentRef) throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		this.setPrecision(pdfWriter.getParams().getPrecision());
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

	/**
	 * Creates a new fragment and links it to the current output structure.
	 * 
	 * @return A new PDFFragmentOutputImpl instance.
	 * @throws IOException If an I/O error occurs.
	 */
	protected PDFFragmentOutputImpl forkFragment() throws IOException {
		this.close();
		final var builder = this.pdfWriter.builder;
		final var nextId = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			builder.addFragment();
		} else {
			builder.insertFragmentBefore(this.anchorId);
		}
		final var streamOut = new FragmentOutputAdapter(builder, nextId);
		this.id = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			builder.addFragment();
		} else {
			builder.insertFragmentBefore(this.anchorId);
		}
		final var newFragOut = new PDFFragmentOutputImpl(streamOut, this.pdfWriter, nextId, this.id, this.currentRef);
		this.out = new FragmentOutputAdapter(builder, this.id);
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
	public OutputStream startStreamFromHash(final Mode mode) throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("Cannot nest streams: " + this.streamLengthFlow);
		}

		final var compression = this.pdfWriter.params.getCompression();
		switch (mode) {
			case RAW -> {
			}
			case ASCII -> {
				switch (compression) {
					case ASCII -> {
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCII85Decode");
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
					}
					case BINARY -> {
						this.writeName("Filter");
						this.startArray();
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
					}
					default -> {
					}
				}
			}
			case BINARY -> {
				switch (compression) {
					case NONE -> {
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCIIHexDecode");
						this.endArray();
						this.breakBefore();
					}
					case ASCII -> {
						this.writeName("Filter");
						this.startArray();
						this.writeName("ASCII85Decode");
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
					}
					case BINARY -> {
						this.writeName("Filter");
						this.startArray();
						this.writeName("FlateDecode");
						this.endArray();
						this.breakBefore();
					}
				}
			}
		}

		this.writeName("Length");
		this.write(' ');
		this.streamLengthFlow = this.forkFragment();
		this.lineBreak();
		this.endHash();
		this.writeLine("stream");
		this.flush();
		this.startStreamPosition = this.getLength();

		var flowOut = (OutputStream) new FilterOutputStream(this) {
			@Override
			public void close() throws IOException {
				PDFFragmentOutputImpl.this.endStream();
			}
		};

		// Apply encryption if enabled
		if (this.pdfWriter.encryption != null) {
			flowOut = this.pdfWriter.encryption.getEncryptor(this.currentRef).getOutputStream(flowOut);
		}

		// Apply final output encoding/compression based on mode and configuration
		final var output = switch (mode) {
			case RAW -> flowOut;
			case ASCII -> {
				final var encodedOut = switch (compression) {
					case ASCII -> new DeflaterOutputStream(new ASCII85OutputStream(flowOut));
					case BINARY -> new DeflaterOutputStream(flowOut);
					default -> flowOut;
				};
				yield new FastBufferedOutputStream(encodedOut, this.getBuff());
			}
			case BINARY -> {
				final var encodedOut = switch (compression) {
					case NONE -> new ASCIIHexOutputStream(flowOut);
					case ASCII -> new DeflaterOutputStream(new ASCII85OutputStream(flowOut));
					case BINARY -> new DeflaterOutputStream(flowOut);
				};
				yield new FastBufferedOutputStream(encodedOut, this.getBuff());
			}
		};
		return output;
	}

	/**
	 * Finalizes the current stream fragment, calculating its length and writing
	 * the end-of-stream markers.
	 * 
	 * @throws IOException If an I/O error occurs.
	 */
	protected void endStream() throws IOException {
		this.streamLengthFlow.writeInt(this.getLength() - this.startStreamPosition);
		this.streamLengthFlow.close();
		this.streamLengthFlow = null;
		this.startStreamPosition = 0;
		// Required EOL before endstream in some PDF profiles
		this.lineBreak();
		this.writeLine("endstream");
	}

	@Override
	public void writeBytes16(final int c) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeBytes16(c);
			return;
		}
		final var data = new byte[2];
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
		final var data = new byte[len * 2];
		for (var i = 0; i < len; ++i) {
			final var c = a[i + off];
			data[i * 2] = (byte) ((c >> 8) & 0xFF);
			data[i * 2 + 1] = (byte) (c & 0xFF);
		}
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	/**
	 * Writes encrypted bytes. If the encryptor uses block encryption, it encrypts
	 * the entire block; otherwise, it encrypts in-place.
	 * 
	 * @param data Byte array to encrypt and write.
	 * @param off  Offset in the buffer.
	 * @param len  Length of data.
	 * @throws IOException If an I/O error occurs.
	 */
	protected void writeEncryptedBytes8(final byte[] data, final int off, final int len) throws IOException {
		final var encryptor = this.pdfWriter.encryption.getEncryptor(this.currentRef);
		final byte[] outData;
		final int outOff;
		final int outLen;
		if (encryptor.isBlock()) {
			outData = encryptor.blockEncrypt(data, off, len);
			outOff = 0;
			outLen = outData.length;
		} else {
			encryptor.fastEncrypt(data, off, len);
			outData = data;
			outOff = off;
			outLen = len;
		}
		super.writeBytes8(outData, outOff, outLen);
	}

	@Override
	public void writeString(final String str) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeString(str);
			return;
		}
		final var data = str.getBytes(this.nameEncoding);
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
		final var data = new byte[text.length() * 2 + 2];
		data[0] = (byte) 0xFE;
		data[1] = (byte) 0xFF;
		for (var i = 0; i < text.length(); ++i) {
			final var c = text.charAt(i);
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