package net.zamasoft.pdfg2d.pdf.util.encryption;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ArcfourEncryptor implements Encryptor {
	private final int[] orgSalt = new int[256];

	class ArcfourOutputStream extends FilterOutputStream {
		private final int[] salt;

		private final int[] bc = { 0, 0 };

		public ArcfourOutputStream(OutputStream out) {
			super(out);
			this.salt = (int[]) ArcfourEncryptor.this.orgSalt.clone();
		}

		public void write(int x) throws IOException {
			this.out.write(ArcfourEncryptor.this.encrypt(this.salt, this.bc, (byte) x));
		}

		public void write(byte[] bytes) throws IOException {
			for (int i = 0; i < bytes.length; ++i) {
				this.write(bytes[i]);
			}
		}

		public void write(byte[] bytes, int off, int len) throws IOException {
			for (int i = 0; i < len; ++i) {
				this.write(bytes[i + off]);
			}
		}
	}

	public ArcfourEncryptor(byte[] key, int len) {
		if (len < 0 || len > 32) {
			throw new IllegalArgumentException("The key length is limited to 1 to 32.");
		}
		for (int i = 0; i < this.orgSalt.length; i++) {
			this.orgSalt[i] = i;
		}

		int keyIndex = 0;
		int saltIndex = 0;
		for (int i = 0; i < this.orgSalt.length; i++) {
			byte x = key[keyIndex];
			saltIndex = ((x < 0 ? 256 + x : x) + this.orgSalt[i] + saltIndex) % 256;
			this.swap(this.orgSalt, i, saltIndex);
			keyIndex = (keyIndex + 1) % len;
		}
	}

	public final void fastEncrypt(byte[] data, int off, int len) {
		int[] salt = (int[]) this.orgSalt.clone();
		int[] bc = { 0, 0 };
		for (int i = 0; i < len; ++i) {
			data[i + off] = this.encrypt(salt, bc, data[i + off]);
		}
	}

	private final void swap(int[] salt, int firstIndex, int secondIndex) {
		int tmp = salt[firstIndex];
		salt[firstIndex] = salt[secondIndex];
		salt[secondIndex] = tmp;
	}

	private byte encrypt(int[] salt, int[] bc, byte x) {
		bc[0] = (bc[0] + 1) % 256;
		bc[1] = (salt[bc[0]] + bc[1]) % 256;
		this.swap(salt, bc[0], bc[1]);
		int saltIndex = (salt[bc[0]] + salt[bc[1]]) % 256;
		return (byte) (x ^ (byte) salt[saltIndex]);
	}

	public OutputStream getOutputStream(OutputStream out) {
		return new ArcfourOutputStream(out);
	}

	public byte[] encrypt(byte[] data) {
		this.fastEncrypt(data, 0, data.length);
		return data;
	}

	public boolean isBlock() {
		return false;
	}

	public byte[] blockEncrypt(byte[] data, int off, int len) {
		throw new UnsupportedOperationException();
	}
}