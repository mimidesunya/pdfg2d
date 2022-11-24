package net.zamasoft.pdfg2d.gc.font;

/**
 * フォントの属性です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FontStyle {
	/**
	 * 描画方向です。DIRECTION_XXXの値を使用します。
	 */
	public Direction getDirection();

	public static enum Direction {
		LTR, RTL, TB
	}

	/**
	 * フォントのウェイトです。WIGHT_XXX定数を使用します。
	 */
	public Weight getWeight();

	public static enum Weight {
		W_100((short)100),W_200((short)200),W_300((short)300),W_400((short)400),W_500((short)500),W_600((short)600),W_700((short)70),W_800((short)800),W_900((short)900);

		public final short w;

		private Weight(short w) {
			this.w = w;
		}
	}

	/**
	 * フォントのスタイルです。STYLE_XXX定数を使用します。
	 */
	public Style getStyle();
	
	public static enum Style {
		NORMAL, ITALIC, OBLIQUE;
	}

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
