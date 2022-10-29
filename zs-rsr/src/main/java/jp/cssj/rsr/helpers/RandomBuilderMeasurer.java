package jp.cssj.rsr.helpers;

import java.io.IOException;

import jp.cssj.rsr.RandomBuilder;

/**
 * 構築中のデータ全体の大きさを計測します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderMeasurer.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class RandomBuilderMeasurer extends RandomBuilderWrapper {
	protected long length;

	public RandomBuilderMeasurer(RandomBuilder builder) {
		super(builder);
	}

	public void dispose() {
		super.dispose();
		this.length = 0;
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		super.write(id, b, off, len);
		this.length += len;
	}

	/**
	 * コンテンツの大きさを返します。
	 * 
	 * @return 追加済みデータの合計バイト数。
	 */
	public long getLength() {
		return this.length;
	}
}
