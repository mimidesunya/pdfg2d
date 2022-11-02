package net.zamasoft.pdfg2d.gc.font;

import java.awt.Font;

import jp.cssj.resolver.Source;

public class FontFace {
	public Source src = null;
	public int index = 0;
	public Font local = null;
	public FontFamilyList fontFamily = null;
	public short fontWeight = FontStyle.FONT_WEIGHT_400;
	public short fontStyle = FontStyle.FONT_STYLE_NORMAL;
	public UnicodeRangeList unicodeRange = null;
	public Panose panose = null;
	public String cmap = null, vcmap = null;

	public String toString() {
		return "src=" + this.src + "/local=" + this.local + "/index=" + this.index + "/fontFamily=" + this.fontFamily
				+ "/fontWeight=" + this.fontWeight + "/fontStyle=" + this.fontStyle + "/unicodeRange="
				+ this.unicodeRange + "/panose=" + this.panose + "/cmap=" + this.cmap + "/vcmap=" + this.vcmap;
	}
}
