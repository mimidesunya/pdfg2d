package net.zamasoft.pdfg2d.pdf.util.encryption;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AESEncryptor.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class AESEncryptor implements Encryptor {
	private final Key key;

	public AESEncryptor(byte[] key, int len) {
		if (len < 0 || len > 32) {
			throw new IllegalArgumentException("The key length is limited to 1 to 32.");
		}
		this.key = new SecretKeySpec(key, 0, len, "AES");

	}

	private Cipher createCipher() {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, AESEncryptor.this.key);
			return cipher;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final byte[] blockEncrypt(byte[] data, int off, int len) {
		try {
			Cipher cipher = this.createCipher();
			byte[] iv = cipher.getIV();
			byte[] code = cipher.doFinal(data, off, len);
			byte[] result = new byte[iv.length + code.length];
			System.arraycopy(iv, 0, result, 0, iv.length);
			System.arraycopy(code, 0, result, iv.length, code.length);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public OutputStream getOutputStream(OutputStream out) throws IOException {
		Cipher cipher = this.createCipher();
		byte[] iv = cipher.getIV();
		out.write(iv);
		return new CipherOutputStream(out, cipher);
	}

	public byte[] encrypt(byte[] data) {
		return this.blockEncrypt(data, 0, data.length);
	}

	public boolean isBlock() {
		return true;
	}

	public void fastEncrypt(byte[] data, int off, int len) {
		throw new UnsupportedOperationException();
	}
}