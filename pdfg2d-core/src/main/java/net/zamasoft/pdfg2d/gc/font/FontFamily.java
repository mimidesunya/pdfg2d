package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.util.FontUtils;

/**
 * Represents a font family.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontFamily implements Serializable {
	private static final long serialVersionUID = 0;

	/**
	 * Represents a generic font family.
	 */
	public enum GenericFamily {
		NONE, SERIF, SANS_SERIF, CURSIVE, FANTASY, MONOSPACE
	}

	public static final FontFamily SERIF_VALUE = new FontFamily(GenericFamily.SERIF, "serif");

	public static final FontFamily SANS_SERIF_VALUE = new FontFamily(GenericFamily.SANS_SERIF, "sans-serif");

	public static final FontFamily CURSIVE_VALUE = new FontFamily(GenericFamily.CURSIVE, "cursive");

	public static final FontFamily FANTASY_VALUE = new FontFamily(GenericFamily.FANTASY, "fantasy");

	public static final FontFamily MONOSPACE_VALUE = new FontFamily(GenericFamily.MONOSPACE, "monospace");

	private final GenericFamily genericFamily;

	private final String name;

	/**
	 * Creates a FontFamily from a name.
	 * 
	 * @param name the font family name
	 * @return the created FontFamily
	 */
	public static FontFamily create(final String name) {
		if (name == null || name.equalsIgnoreCase("serif")) {
			return FontFamily.SERIF_VALUE;
		} else if (name.equalsIgnoreCase("cursive")) {
			return FontFamily.CURSIVE_VALUE;
		} else if (name.equalsIgnoreCase("fantasy")) {
			return FontFamily.FANTASY_VALUE;
		} else if (name.equalsIgnoreCase("monospace")) {
			return FontFamily.MONOSPACE_VALUE;
		} else if (name.equalsIgnoreCase("sans-serif")) {
			return FontFamily.SANS_SERIF_VALUE;
		}
		return new FontFamily(name);
	}

	private FontFamily(final GenericFamily genericFamily, final String name) {
		this.genericFamily = genericFamily;
		this.name = name;
	}

	/**
	 * Creates a new FontFamily.
	 * 
	 * @param name the font family name
	 */
	public FontFamily(final String name) {
		this.genericFamily = GenericFamily.NONE;
		this.name = name;
	}

	/**
	 * Returns whether this is a generic family.
	 * 
	 * @return true if generic family, false otherwise
	 */
	public boolean isGenericFamily() {
		return this.genericFamily != GenericFamily.NONE;
	}

	/**
	 * Returns the generic family code.
	 * 
	 * @return the generic family code
	 */
	public GenericFamily getGenericFamily() {
		return this.genericFamily;
	}

	/**
	 * Returns the font family name.
	 * 
	 * @return the family name
	 */
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FontFamily)) {
			return false;
		}
		final var a = (FontFamily) o;
		if (a.isGenericFamily() != this.isGenericFamily()) {
			return false;
		}
		if (a.isGenericFamily()) {
			return a.genericFamily == this.genericFamily;
		}
		return FontUtils.normalizeName(a.name).equals(FontUtils.normalizeName(this.name));
	}

	@Override
	public int hashCode() {
		if (this.isGenericFamily()) {
			return this.genericFamily.ordinal();
		}
		return FontUtils.normalizeName(this.name).hashCode();
	}
}
