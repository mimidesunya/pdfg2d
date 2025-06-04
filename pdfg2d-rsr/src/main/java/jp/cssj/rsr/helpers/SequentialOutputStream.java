package jp.cssj.rsr.helpers;

import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.rsr.Sequential;

/**
 * SerialSupport へデータを追加するストリームです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SerialSupportOutputStream.java 608 2011-08-28 06:26:47Z miyabe
 *          $
 */
public class SequentialOutputStream extends OutputStream {
	private final Sequential builder;

	private final byte[] buff = new byte[1];

	public SequentialOutputStream(Sequential builder) {
		this.builder = builder;
	}

	public void write(int b) throws IOException {
		this.buff[0] = (byte) b;
		this.builder.write(this.buff, 0, 1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.builder.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.builder.write(b, 0, b.length);
	}

	/**
	 * このメソッドは 何もしません。
	 */
	public void close() throws IOException {
		// ignore
	}
}