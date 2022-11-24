package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.util.FontUtils;

public class FontFamily implements Serializable {
	private static final long serialVersionUID = 0;

	public static enum GenericFamily {
		NONE, SERIF, SANS_SERIF, CURSIVE, FANTASY, MONOSPACE;
	}

	public static final FontFamily SERIF_VALUE = new FontFamily(GenericFamily.SERIF, "serif");

	public static final FontFamily SANS_SERIF_VALUE = new FontFamily(GenericFamily.SANS_SERIF, "sans-serif");

	public static final FontFamily CURSIVE_VALUE = new FontFamily(GenericFamily.CURSIVE, "cursive");

	public static final FontFamily FANTASY_VALUE = new FontFamily(GenericFamily.FANTASY, "fantasy");

	public static final FontFamily MONOSPACE_VALUE = new FontFamily(GenericFamily.MONOSPACE, "monospace");

	private final GenericFamily genericFamily;

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

	private FontFamily(GenericFamily genericFamily, String name) {
		this.genericFamily = genericFamily;
		this.name = name;
	}

	public FontFamily(String name) {
		this.genericFamily = GenericFamily.NONE;
		this.name = name;
	}

	/**
	 * 一般ファミリならtrueを返します。
	 * 
	 * @return
	 */
	public boolean isGenericFamily() {
		return this.genericFamily != GenericFamily.NONE;
	}

	/**
	 * 一般ファミリコードを返します。
	 * 
	 * @return
	 */
	public GenericFamily getGenericFamily() {
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
		if (a.isGenericFamily ()!= this.isGenericFamily()) {
			return false;
		}
		if (a.isGenericFamily()) {
			return a.genericFamily == this.genericFamily;
		}
		return FontUtils.normalizeName(a.name).equals(FontUtils.normalizeName(this.name));
	}

	public int hashCode() {
		if (this.isGenericFamily()) {
			return this.genericFamily.ordinal();
		}
		return FontUtils.normalizeName(this.name).hashCode();
	}
}
