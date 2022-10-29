package jp.cssj.rsr;

import java.io.IOException;

/**
 * 順次的なデータです。 このインターフェースを実装した RandomBuilder インスタンスでは断片の追加を必要としません。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Sequential.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface Sequential extends RandomBuilder {
	/**
	 * データを追加します。
	 * 
	 * @param b
	 *            バイト列。
	 * @param off
	 *            バイト列中の開始位置。
	 * @param len
	 *            バイト列中のデータの長さ。
	 * @throws IOException
	 */
	public void write(byte[] b, int off, int len) throws IOException;
}
