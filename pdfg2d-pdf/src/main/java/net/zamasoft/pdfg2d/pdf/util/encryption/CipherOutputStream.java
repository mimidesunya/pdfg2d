package net.zamasoft.pdfg2d.pdf.util.encryption;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;

public class CipherOutputStream extends FilterOutputStream {
	private final Cipher cipher;
	private byte[] single = new byte[1];

	public CipherOutputStream(OutputStream out, Cipher cipher) {
		super(out);
		this.cipher = cipher;
	}

	public void write(int x) throws IOException {
		this.single[0] = (byte) x;
		this.write(this.single, 0, 1);
	}

	public void write(byte[] bytes) throws IOException {
		this.write(bytes, 0, bytes.length);
	}

	public void write(byte[] bytes, int off, int len) throws IOException {
		byte[] block = this.cipher.update(bytes, off, len);
		if (block != null) {
			this.out.write(block);
		}
	}

	public void close() throws IOException {
		Exception ex = null;
		try {
			byte[] block = this.cipher.doFinal();
			if (block != null) {
				this.out.write(block);
			}
		} catch (Exception e) {
			ex = e;
		} finally {
			this.out.close();
		}
		if (ex != null) {
			IOException ioe = new IOException();
			ioe.initCause(ex);
			throw ioe;
		}
	}
}
