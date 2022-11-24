package net.zamasoft.pdfg2d.gc.font;

import java.awt.Font;

import jp.cssj.resolver.Source;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;

public class FontFace {
	public Source src = null;
	public int index = 0;
	public Font local = null;
	public FontFamilyList fontFamily = null;
	public Weight fontWeight = Weight.W_400;
	public Style fontStyle = Style.NORMAL;
	public UnicodeRangeList unicodeRange = null;
	public Panose panose = null;
	public String cmap = null, vcmap = null;

	public String toString() {
		return "src=" + this.src + "/local=" + this.local + "/index=" + this.index + "/fontFamily=" + this.fontFamily
				+ "/fontWeight=" + this.fontWeight + "/fontStyle=" + this.fontStyle + "/unicodeRange="
				+ this.unicodeRange + "/panose=" + this.panose + "/cmap=" + this.cmap + "/vcmap=" + this.vcmap;
	}
}
