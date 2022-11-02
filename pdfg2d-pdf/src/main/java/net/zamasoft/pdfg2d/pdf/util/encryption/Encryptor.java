package net.zamasoft.pdfg2d.pdf.util.encryption;

import java.io.IOException;
import java.io.OutputStream;

public interface Encryptor {
	public boolean isBlock();

	public void fastEncrypt(byte[] data, int off, int len);

	public byte[] blockEncrypt(byte[] data, int off, int len);

	public byte[] encrypt(byte[] data);

	public OutputStream getOutputStream(OutputStream out) throws IOException;
}
