package net.zamasoft.pdfg2d.gc.font;

/**
 * フォントの属性です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontStyle.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface FontStyle {
	/**
	 * 描画方向です。DIRECTION_XXXの値を使用します。
	 */
	public byte getDirection();

	/** 左から右に書くフォントです。 */
	public static final byte DIRECTION_LTR = 1;

	/** 右から左に書くフォントです。 */
	public static final byte DIRECTION_RTL = 2;

	/** 縦書きフォントです */
	public static final byte DIRECTION_TB = 3;

	/**
	 * フォントのウェイトです。WIGHT_XXX定数を使用します。
	 */
	public short getWeight();

	/** 最も細いフォントウェイトです。 */
	public static final short FONT_WEIGHT_100 = 100;

	public static final short FONT_WEIGHT_200 = 200;

	public static final short FONT_WEIGHT_300 = 300;

	/** 標準のフォントウェイトです。 */
	public static final short FONT_WEIGHT_400 = 400;

	public static final short FONT_WEIGHT_500 = 500;

	public static final short FONT_WEIGHT_600 = 600;

	public static final short FONT_WEIGHT_700 = 700;

	public static final short FONT_WEIGHT_800 = 800;

	/** 最も太いフォントウェイトです。 */
	public static final short FONT_WEIGHT_900 = 900;

	/**
	 * フォントのスタイルです。STYLE_XXX定数を使用します。
	 */
	public byte getStyle();

	/** 標準字体です。 */
	public static final byte FONT_STYLE_NORMAL = 1;

	/**
	 * 斜体文字です。 この指定は斜体用フォントの使用を最優先します。
	 * 例えば、太字-斜体という指定の際に太字-斜体のフォントが存在せず、細字-斜体のフォントがある場合は細字-斜体のフォントが使用されます。
	 */
	public static final byte FONT_STYLE_ITALIC = 2;

	/**
	 * 斜体文字です。 この指定は単純に文字を傾ける方法を優先します。
	 * 例えば、太字-斜体という指定の際に太字-斜体のフォントが存在せず、細字-斜体のフォントがある場合は太字-標準自体のフォントを傾けて利用します。
	 */
	public static final byte FONT_STYLE_OBLIQUE = 3;

	/**
	 * フォントファミリです。
	 */
	public FontFamilyList getFamily();

	/**
	 * フォントの大きさです。
	 */
	public double getSize();

	/**
	 * フォントの埋め込みポリシーです。
	 * 
	 * @return
	 */
	public FontPolicyList getPolicy();
}
