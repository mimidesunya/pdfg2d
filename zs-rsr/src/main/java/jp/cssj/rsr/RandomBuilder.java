package jp.cssj.rsr;

import java.io.IOException;

/**
 * <p>
 * 断片化されたデータを構築するためのインターフェースです。
 * </p>
 * 
 * <p>
 * addBlock または insertBlockBefore により新たな断片を追加します。 断片は追加順に0,1,2,3...というIDが振られます。
 * IDは断片の挿入または断片にデータを追加する際の識別のために使うことができます。
 * </p>
 * 
 * <p>
 * インスタンスが Sequential インターフェースを実装している場合は、 もともと順次的なデータであり、insertBlockBefore
 * を使う必要がないことを示します。 この場合、addBlock を呼び出さずに Sequential
 * インターフェースのメソッドを呼ぶことで効率良くデータを構築することができます。
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilder.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface RandomBuilder {
	/**
	 * 断片の位置情報です。
	 */
	public static interface PositionInfo {
		/**
		 * <p>
		 * 断片の先頭の位置を返します。
		 * </p>
		 * <p>
		 * これは getPositionInfo が呼び出された時点の値で、 その後の RandomBuilder に対する操作は影響しません。
		 * </p>
		 * 
		 * @param id
		 *            断片のID。
		 * @return 断片の先頭の位置。
		 */
		public long getPosition(int id);
	}

	/**
	 * 断片を末尾に追加します。
	 * 
	 * @throws IOException
	 */
	public void addBlock() throws IOException;

	/**
	 * 指定した断片の直前に断片を挿入します。
	 * 
	 * @param anchorId
	 *            断片のID。
	 * @throws IOException
	 */
	public void insertBlockBefore(int anchorId) throws IOException;

	/**
	 * 断片にデータを追加します。
	 * 
	 * @param id
	 *            断片のID。
	 * @param b
	 *            バイト列。
	 * @param off
	 *            バイト列中のデータの開始位置。
	 * @param len
	 *            バイト列中のデータの長さ。
	 * @throws IOException
	 */
	public void write(int id, byte[] b, int off, int len) throws IOException;

	/**
	 * 位置情報をサポートしているかどうかを返します。
	 * 
	 * @return 位置情報をサポートしていればtrue。
	 */
	public boolean supportsPositionInfo();

	/**
	 * 構築中の各断片の先頭位置を得るためのオブジェクトを返します。
	 * 
	 * @return 位置情報。
	 */
	public PositionInfo getPositionInfo() throws UnsupportedOperationException;

	/**
	 * 断片への書き込みを終了します。 この呼び出しは必須ではありませんが、データの構築を効率化する可能性があります。
	 * 
	 * @param id
	 *            断片のID。
	 * @throws IOException
	 */
	public void closeBlock(int id) throws IOException;

	/**
	 * データの構築を完了します。
	 * 
	 * @throws IOException
	 */
	public void finish() throws IOException;

	/**
	 * <p>
	 * 構築のためのリソースを破棄します。
	 * </p>
	 * <p>
	 * RandomBuilder は処理のために一時ファイルなどのリソースを使用するため、
	 * 不要となったオブジェクトに対しては必ずdispose()を呼び出してください。
	 * </p>
	 */
	public void dispose();
}