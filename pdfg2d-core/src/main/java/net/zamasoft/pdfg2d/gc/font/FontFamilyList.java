package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a list of font families.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record FontFamilyList(FontFamily[] families) implements Serializable {

	public static final FontFamilyList SERIF = new FontFamilyList(FontFamily.SERIF_VALUE);

	public static final FontFamilyList SANS_SERIF = new FontFamilyList(FontFamily.SANS_SERIF_VALUE);

	public static final FontFamilyList CURSIVE = new FontFamilyList(FontFamily.CURSIVE_VALUE);

	public static final FontFamilyList FANTASY = new FontFamilyList(FontFamily.FANTASY_VALUE);

	public static final FontFamilyList MONOSPACE = new FontFamilyList(FontFamily.MONOSPACE_VALUE);

	/**
	 * Creates a FontFamilyList from a name.
	 * 
	 * @param name the font family name
	 * @return the created FontFamilyList
	 */
	public static FontFamilyList create(final String name) {
		if (name == null) {
			return FontFamilyList.SERIF;
		}
		return switch (name.toLowerCase(java.util.Locale.ROOT)) {
			case "serif" -> FontFamilyList.SERIF;
			case "cursive" -> FontFamilyList.CURSIVE;
			case "fantasy" -> FontFamilyList.FANTASY;
			case "monospace" -> FontFamilyList.MONOSPACE;
			case "sans-serif" -> FontFamilyList.SANS_SERIF;
			default -> new FontFamilyList(new FontFamily(name));
		};
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
		return java.util.Arrays.stream(this.families)
				.map(f -> f.isGenericFamily() ? f.getName() : "'" + f.getName() + "'")
				.collect(java.util.stream.Collectors.joining(" "));
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof FontFamilyList l && Arrays.equals(this.families, l.families);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.families);
	}
}