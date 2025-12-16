package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * 1つまたは複数のフォントファミリーです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
/**
 * Represents a list of font families.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontFamilyList implements Serializable {
	private static final long serialVersionUID = 0;

	public static final FontFamilyList SERIF = new FontFamilyList(FontFamily.SERIF_VALUE);

	public static final FontFamilyList SANS_SERIF = new FontFamilyList(FontFamily.SANS_SERIF_VALUE);

	public static final FontFamilyList CURSIVE = new FontFamilyList(FontFamily.CURSIVE_VALUE);

	public static final FontFamilyList FANTASY = new FontFamilyList(FontFamily.FANTASY_VALUE);

	public static final FontFamilyList MONOSPACE = new FontFamilyList(FontFamily.MONOSPACE_VALUE);

	private final FontFamily[] families;

	/**
	 * Creates a FontFamilyList from a name.
	 * 
	 * @param name the font family name
	 * @return the created FontFamilyList
	 */
	public static FontFamilyList create(final String name) {
		if (name == null || name.equalsIgnoreCase("serif")) {
			return FontFamilyList.SERIF;
		} else if (name.equalsIgnoreCase("cursive")) {
			return FontFamilyList.CURSIVE;
		} else if (name.equalsIgnoreCase("fantasy")) {
			return FontFamilyList.FANTASY;
		} else if (name.equalsIgnoreCase("monospace")) {
			return FontFamilyList.MONOSPACE;
		} else if (name.equalsIgnoreCase("sans-serif")) {
			return FontFamilyList.SANS_SERIF;
		}
		return new FontFamilyList(new FontFamily(name));
	}

	/**
	 * Creates a new FontFamilyList with the given families.
	 * 
	 * @param families the font families
	 */
	public FontFamilyList(final FontFamily[] families) {
		this.families = families;
	}

	/**
	 * Creates a new FontFamilyList with one family.
	 * 
	 * @param f1 the first font family
	 */
	public FontFamilyList(final FontFamily f1) {
		this(new FontFamily[] { f1 });
	}

	/**
	 * Creates a new FontFamilyList with two families.
	 * 
	 * @param f1 the first font family
	 * @param f2 the second font family
	 */
	public FontFamilyList(final FontFamily f1, final FontFamily f2) {
		this(new FontFamily[] { f1, f2 });
	}

	/**
	 * Creates a new FontFamilyList with three families.
	 * 
	 * @param f1 the first font family
	 * @param f2 the second font family
	 * @param f3 the third font family
	 */
	public FontFamilyList(final FontFamily f1, final FontFamily f2, final FontFamily f3) {
		this(new FontFamily[] { f1, f2, f3 });
	}

	/**
	 * Returns the font family at the specified index.
	 * 
	 * @param index the index of the font family
	 * @return the font family
	 */
	public FontFamily get(final int index) {
		return this.families[index];
	}

	/**
	 * Returns the number of font families in this list.
	 * 
	 * @return the number of font families
	 */
	public int getLength() {
		return this.families.length;
	}

	@Override
	public String toString() {
		if (this.families.length == 0) {
			return "";
		}
		final var buffer = new StringBuilder();
		for (int i = 0; i < this.families.length; ++i) {
			final var entry = this.families[i];
			if (entry.isGenericFamily()) {
				buffer.append(entry.getName()).append(' ');
			} else {
				buffer.append('\'').append(entry.getName()).append("' ");
			}
		}
		return buffer.substring(0, buffer.length() - 1);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FontFamilyList)) {
			return false;
		}
		final var a = ((FontFamilyList) o).families;
		final var b = this.families;
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; ++i) {
			if (!a[i].equals(b[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < this.families.length; ++i) {
			h = 31 * h + this.families[i].hashCode();
		}
		return h;
	}
}