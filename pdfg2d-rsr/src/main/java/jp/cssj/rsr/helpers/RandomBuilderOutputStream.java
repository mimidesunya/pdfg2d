package jp.cssj.rsr.helpers;

import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.rsr.RandomBuilder;

/**
 * RandomBuilder の特定の断片にデータを書き込むストリームです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderOutputStream.java 656 2011-09-03 15:42:28Z miyabe
 *          $
 */
public class RandomBuilderOutputStream extends OutputStream {
	private final RandomBuilder builder;

	private final int fragmentId;

	private final byte[] buff = new byte[1];

	/**
	 * 
	 * @param builder    書き込み先の RandomBuider。
	 * @param fragmentId 書き込み先の断片ID。
	 */
	public RandomBuilderOutputStream(RandomBuilder builder, int fragmentId) {
		if (builder == null) {
			throw new NullPointerException();
		}
		this.builder = builder;
		this.fragmentId = fragmentId;
	}

	public void write(int b) throws IOException {
		this.buff[0] = (byte) b;
		this.builder.write(this.fragmentId, this.buff, 0, 1);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.builder.write(this.fragmentId, b, off, len);
	}

	public void write(byte[] b) throws IOException {
		this.builder.write(this.fragmentId, b, 0, b.length);
	}

	/**
	 * RandomBuilder の closeBlock メソッドを呼び出します。
	 */
	public void close() throws IOException {
		this.builder.closeBlock(this.fragmentId);
	}
}