package net.zamasoft.pdfg2d.pdf.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import jp.cssj.rsr.helpers.RandomBuilderOutputStream;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfFragmentOutput;
import net.zamasoft.pdfg2d.pdf.params.PdfParams;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCII85OutputStream;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCIIHexOutputStream;
import net.zamasoft.pdfg2d.pdf.util.encryption.Encryptor;
import net.zamasoft.pdfg2d.pdf.util.io.FastBufferedOutputStream;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PDFFragmentOutputImpl.java,v 1.2 2005/06/02 10:40:30 harumanx
 *          Exp $
 */
class PdfFragmentOutputImpl extends PdfFragmentOutput {
	private final PdfWriterImpl pdfWriter;

	/** 自分自身と次の断片ID。 */
	private int id, anchorId = -1;

	/** 出力バイト数。 */
	private int length = 0;

	/** 書き込み中のストリーム。 */
	private PdfFragmentOutputImpl streamLengthFlow = null;

	/** ストリームの開始位置。 */
	private int startStreamPosition = 0;

	/** 現在のオブジェクトのリファレンス。 */
	private ObjectRef currentRef;

	private byte[] buff = null;

	public PdfFragmentOutputImpl(OutputStream out, PdfWriterImpl pdfWriter, int id, int nextId, ObjectRef currentRef)
			throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		this.pdfWriter = pdfWriter;
		this.id = id;
		this.anchorId = nextId;
		this.currentRef = currentRef;
	}

	protected byte[] getBuff() {
		if (this.buff == null) {
			this.buff = new byte[PdfWriterImpl.BUFFER_SIZE];
		}
		return this.buff;
	}

	protected PdfFragmentOutputImpl forkFragment() throws IOException {
		this.close();
		int id = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			this.pdfWriter.builder.addBlock();
		} else {
			this.pdfWriter.builder.insertBlockBefore(this.anchorId);
		}
		OutputStream out = new RandomBuilderOutputStream(this.pdfWriter.builder, id);
		this.id = this.pdfWriter.nextId();
		if (this.anchorId == -1) {
			this.pdfWriter.builder.addBlock();
		} else {
			this.pdfWriter.builder.insertBlockBefore(this.anchorId);
		}
		PdfFragmentOutputImpl newFragOut = new PdfFragmentOutputImpl(out, this.pdfWriter, id, this.id, this.currentRef);
		this.out = new RandomBuilderOutputStream(this.pdfWriter.builder, this.id);
		this.length = 0;
		return newFragOut;
	}

	/**
	 * オブジェクトの開始を出力します。
	 * 
	 * @param ref
	 * @throws IOException
	 */
	public void startObject(ObjectRef ref) throws IOException {
		this.breakBefore();
		((ObjectRefImpl) ref).setPosition(this.id, this.getLength());
		this.writeInt(ref.objectNumber);
		this.writeInt(ref.generationNumber);
		this.writeOperator("obj");
		this.lineBreak();
		if (this.currentRef != null) {
			throw new IllegalStateException("既にオブジェクトの中です:" + this.currentRef);
		}
		this.currentRef = ref;
	}

	/**
	 * オブジェクトの終端を出力します。
	 * 
	 * @throws IOException
	 */
	public void endObject() throws IOException {
		this.writeLine("endobj");
		if (this.currentRef == null) {
			throw new IllegalStateException("既にオブジェクトの外です");
		}
		this.currentRef = null;
	}

	/**
	 * ストリームの開始を出力します。
	 * 
	 * @throws IOException
	 */
	public OutputStream startStream(short mode) throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("ストリームをネストすることはできません:" + this.streamLengthFlow);
		}
		this.startHash();

		return this.startStreamFromHash(mode);
	}

	@SuppressWarnings("resource")
	public OutputStream startStreamFromHash(short mode) throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("ストリームをネストすることはできません:" + this.streamLengthFlow);
		}

		switch (mode) {
		case STREAM_RAW:
			break;

		case STREAM_ASCII:
			switch (this.pdfWriter.params.getCompression()) {
			case PdfParams.COMPRESSION_NONE:
				break;
			case PdfParams.COMPRESSION_ASCII:
				this.writeName("Filter");
				this.startArray();
				this.writeName("ASCII85Decode");
				this.writeName("FlateDecode");
				this.endArray();
				this.breakBefore();
				break;
			case PdfParams.COMPRESSION_BINARY:
				this.writeName("Filter");
				this.startArray();
				this.writeName("FlateDecode");
				this.endArray();
				this.breakBefore();
				break;
			}
			break;

		case STREAM_BINARY:
			switch (this.pdfWriter.params.getCompression()) {
			case PdfParams.COMPRESSION_NONE:
				this.writeName("Filter");
				this.startArray();
				this.writeName("ASCIIHexDecode");
				this.endArray();
				this.breakBefore();
				break;
			case PdfParams.COMPRESSION_ASCII:
				this.writeName("Filter");
				this.startArray();
				this.writeName("ASCII85Decode");
				this.writeName("FlateDecode");
				this.endArray();
				this.breakBefore();
				break;
			case PdfParams.COMPRESSION_BINARY:
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
			public void close() throws IOException {
				PdfFragmentOutputImpl.this.endStream();
			}
		};

		// 暗号化
		if (this.pdfWriter.encryption != null) {
			out = this.pdfWriter.encryption.getEncryptor(this.currentRef).getOutputStream(out);
		}

		// 各種符号化
		switch (mode) {
		case STREAM_RAW:
			break;

		case STREAM_ASCII:
			switch (this.pdfWriter.params.getCompression()) {
			case PdfParams.COMPRESSION_NONE:
				break;
			case PdfParams.COMPRESSION_ASCII:
				out = new DeflaterOutputStream(new ASCII85OutputStream(out));
				break;
			case PdfParams.COMPRESSION_BINARY:
				out = new DeflaterOutputStream(out);
				break;
			default:
				throw new IllegalArgumentException();
			}
			out = new FastBufferedOutputStream(out, this.getBuff());
			break;

		case STREAM_BINARY:
			switch (this.pdfWriter.params.getCompression()) {
			case PdfParams.COMPRESSION_NONE:
				out = new ASCIIHexOutputStream(out);
				break;
			case PdfParams.COMPRESSION_ASCII:
				out = new DeflaterOutputStream(new ASCII85OutputStream(out));
				break;
			case PdfParams.COMPRESSION_BINARY:
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
	 * ストリームの終端を出力します。
	 * 
	 * @throws IOException
	 */
	protected void endStream() throws IOException {
		this.streamLengthFlow.writeInt(this.getLength() - this.startStreamPosition);
		this.streamLengthFlow.close();
		this.streamLengthFlow = null;
		this.startStreamPosition = 0;
		this.lineBreak(); // PDF/A-1ではendstreamの前にEOLを入れ、この長さはLengthに含めない
		this.writeLine("endstream");
	}

	public void writeBytes16(int c) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeBytes16(c);
			return;
		}
		byte[] data = new byte[2];
		data[0] = (byte) ((c >> 8) & 0xFF);
		data[1] = (byte) (c & 0xFF);
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	public void writeBytes16(int[] a, int off, int len) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeBytes16(a, off, len);
			return;
		}
		byte[] data = new byte[len * 2];
		for (int i = 0; i < len; ++i) {
			int c = a[i + off];
			data[i * 2] = (byte) ((c >> 8) & 0xFF);
			data[i * 2 + 1] = (byte) (c & 0xFF);
		}
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	protected void writeEncryptedBytes8(byte[] data, int off, int len) throws IOException {
		Encryptor e = this.pdfWriter.encryption.getEncryptor(this.currentRef);
		if (e.isBlock()) {
			data = e.blockEncrypt(data, off, len);
			off = 0;
			len = data.length;
		} else {
			e.fastEncrypt(data, off, len);
		}
		super.writeBytes8(data, off, len);
	}

	public void writeString(String str) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeString(str);
			return;
		}
		byte[] data = str.getBytes("iso-8859-1");
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	public void writeText(String text) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeText(text);
			return;
		}
		this.writeUTF16(text);
	}

	public void writeUTF16(String text) throws IOException {
		if (this.pdfWriter.encryption == null) {
			super.writeUTF16(text);
			return;
		}
		byte[] data = new byte[text.length() * 2 + 2];
		data[0] = (byte) 0xFE;
		data[1] = (byte) 0xFF;
		for (int i = 0; i < text.length(); ++i) {
			char c = text.charAt(i);
			data[i * 2 + 2] = (byte) ((c >> 8) & 0xFF);
			data[i * 2 + 3] = (byte) (c & 0xFF);
		}
		this.writeEncryptedBytes8(data, 0, data.length);
	}

	/**
	 * 現在の書き込みバイト数を返します。
	 * 
	 * @return
	 */
	protected int getLength() {
		return this.length;
	}

	protected int getId() {
		return this.id;
	}

	public void write(byte[] buff, int off, int len) throws IOException {
		super.write(buff, off, len);
		this.length += len;
	}

	public void write(byte[] buff) throws IOException {
		super.write(buff);
		this.length += buff.length;
	}

	public void write(int c) throws IOException {
		super.write(c);
		this.length++;
	}

	public void close() throws IOException {
		if (this.streamLengthFlow != null) {
			throw new IllegalStateException("ストリームが閉じられていません");
		}
		if (this.out == null) {
			throw new IllegalStateException("既に閉じられています");
		}
		super.close();
		this.out = null;
	}
}