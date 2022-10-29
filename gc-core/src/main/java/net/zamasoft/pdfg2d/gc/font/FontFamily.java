package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.util.FontUtils;

public class FontFamily implements Serializable {
	private static final long serialVersionUID = 0;

	public static final short SERIF = 1;

	public static final short SANS_SERIF = 2;

	public static final short CURSIVE = 3;

	public static final short FANTASY = 4;

	public static final short MONOSPACE = 5;

	public static final FontFamily SERIF_VALUE = new FontFamily(SERIF, "serif");

	public static final FontFamily SANS_SERIF_VALUE = new FontFamily(SANS_SERIF, "sans-serif");

	public static final FontFamily CURSIVE_VALUE = new FontFamily(CURSIVE, "cursive");

	public static final FontFamily FANTASY_VALUE = new FontFamily(FANTASY, "fantasy");

	public static final FontFamily MONOSPACE_VALUE = new FontFamily(MONOSPACE, "monospace");

	private final boolean isGenericFamily;

	private final short genericFamily;

	private final String name;

	public static FontFamily create(String name) {
		FontFamily family;
		if (name == null || name.equalsIgnoreCase("serif")) {
			family = FontFamily.SERIF_VALUE;
		} else if (name.equalsIgnoreCase("cursive")) {
			family = FontFamily.CURSIVE_VALUE;
		} else if (name.equalsIgnoreCase("fantasy")) {
			family = FontFamily.FANTASY_VALUE;
		} else if (name.equalsIgnoreCase("monospace")) {
			family = FontFamily.MONOSPACE_VALUE;
		} else if (name.equalsIgnoreCase("sans-serif")) {
			family = FontFamily.SANS_SERIF_VALUE;
		} else {
			family = new FontFamily(name);
		}
		return family;
	}

	private FontFamily(short genericFamily, String name) {
		this.isGenericFamily = true;
		this.genericFamily = genericFamily;
		this.name = name;
	}

	public FontFamily(String name) {
		this.isGenericFamily = false;
		this.genericFamily = 0;
		this.name = name;
	}

	/**
	 * 一般ファミリならtrueを返します。
	 * 
	 * @return
	 */
	public boolean isGenericFamily() {
		return this.isGenericFamily;
	}

	/**
	 * 一般ファミリコードを返します。
	 * 
	 * @return
	 */
	public short getGenericFamily() {
		return this.genericFamily;
	}

	/**
	 * ファミリ名を返します。
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.getName();
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FontFamily)) {
			return false;
		}
		FontFamily a = (FontFamily) o;
		if (a.isGenericFamily != this.isGenericFamily) {
			return false;
		}
		if (a.isGenericFamily) {
			return a.genericFamily == this.genericFamily;
		}
		return FontUtils.normalizeName(a.name).equals(FontUtils.normalizeName(this.name));
	}

	public int hashCode() {
		if (this.isGenericFamily) {
			return this.genericFamily;
		}
		return FontUtils.normalizeName(this.name).hashCode();
	}
}
