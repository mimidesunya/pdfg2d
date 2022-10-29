package net.zamasoft.pdfg2d.font;

import java.io.Serializable;

/**
 * TTFやシステムフォント等PDFフォントの元となるフォントです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontSource.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface FontSource extends Serializable {
	/**
	 * フォント名を返します。
	 * 
	 * @return
	 */
	public String getFontName();

	/**
	 * フォントの別名を列挙します。
	 * 
	 * @return
	 */
	public String[] getAliases();

	/**
	 * 描画方向を返します。
	 * 
	 * @return FontStyle.DIRECTION_XX値。
	 */
	public byte getDirection();

	/**
	 * @return 斜体であればtrue。
	 */
	public boolean isItalic();

	/**
	 * @return フォントのウェイト。
	 */
	public short getWeight();

	/**
	 * デフォルトの1em当たりのユニット数です。 CFFの出力もこのユニット数を基準にします。
	 */
	public static final short DEFAULT_UNITS_PER_EM = 1000;

	/**
	 * 文字を表示可能であればtrueを返します。
	 * 
	 * @param c
	 * @return
	 */
	public boolean canDisplay(int c);

	/**
	 * @return バウンディングボックス。
	 */
	public BBox getBBox();

	/**
	 * ベースラインの上の高さを返します。
	 * 
	 * @return
	 */
	public short getAscent();

	/**
	 * 大文字の高さ(em)を返します。
	 * 
	 * @return
	 */
	public short getCapHeight();

	/**
	 * ベースラインより下の高さを返します。
	 * 
	 * @return
	 */
	public short getDescent();

	/**
	 * @return this.Returns the stemh.
	 */
	public short getStemH();

	/**
	 * @return this.Returns the stemv.
	 */
	public short getStemV();

	/**
	 * 小文字の高さ(ex)を返します。
	 * 
	 * @return
	 */
	public short getXHeight();

	/**
	 * 空白文字の幅を返します。
	 * 
	 * @return
	 */
	public short getSpaceAdvance();

	/**
	 * ドキュメント中のフォントを作成します。
	 * 
	 * @return
	 */
	public Font createFont();
}
