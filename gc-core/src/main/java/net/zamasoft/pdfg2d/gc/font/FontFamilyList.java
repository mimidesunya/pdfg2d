package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * 1つまたは複数のフォントファミリーです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontFamilyList.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class FontFamilyList implements Serializable {
	private static final long serialVersionUID = 0;

	public static final FontFamilyList SERIF = new FontFamilyList(FontFamily.SERIF_VALUE);

	public static final FontFamilyList SANS_SERIF = new FontFamilyList(FontFamily.SANS_SERIF_VALUE);

	public static final FontFamilyList CURSIVE = new FontFamilyList(FontFamily.CURSIVE_VALUE);

	public static final FontFamilyList FANTASY = new FontFamilyList(FontFamily.FANTASY_VALUE);

	public static final FontFamilyList MONOSPACE = new FontFamilyList(FontFamily.MONOSPACE_VALUE);

	private final FontFamily[] families;

	public static FontFamilyList create(String name) {
		FontFamilyList family;
		if (name == null || name.equalsIgnoreCase("serif")) {
			family = FontFamilyList.SERIF;
		} else if (name.equalsIgnoreCase("cursive")) {
			family = FontFamilyList.CURSIVE;
		} else if (name.equalsIgnoreCase("fantasy")) {
			family = FontFamilyList.FANTASY;
		} else if (name.equalsIgnoreCase("monospace")) {
			family = FontFamilyList.MONOSPACE;
		} else if (name.equalsIgnoreCase("sans-serif")) {
			family = FontFamilyList.SANS_SERIF;
		} else {
			family = new FontFamilyList(new FontFamily(name));
		}
		return family;
	}

	/**
	 * 複数のフォントを指定してフォントリストを構築します。
	 * 
	 * @param families
	 */
	public FontFamilyList(FontFamily[] families) {
		this.families = families;
	}

	public FontFamilyList(FontFamily f1) {
		this(new FontFamily[] { f1 });
	}

	public FontFamilyList(FontFamily f1, FontFamily f2) {
		this(new FontFamily[] { f1, f2 });
	}

	public FontFamilyList(FontFamily f1, FontFamily f2, FontFamily f3) {
		this(new FontFamily[] { f1, f2, f3 });
	}

	/**
	 * 指定されたインデックスのファミリを返します。
	 * 
	 * @param index
	 * @return
	 */
	public FontFamily get(int index) {
		return this.families[index];
	}

	/**
	 * 候補となっているフォントファミリーの数を返します。
	 * 
	 * @return
	 */
	public int getLength() {
		return this.families.length;
	}

	public String toString() {
		if (this.families.length == 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < this.families.length; ++i) {
			FontFamily entry = this.families[i];
			if (entry.isGenericFamily()) {
				buffer.append(entry.getName()).append(' ');
			} else {
				buffer.append('\'').append(entry.getName()).append("' ");
			}
		}
		return buffer.substring(0, buffer.length() - 1);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FontFamilyList)) {
			return false;
		}
		FontFamily[] a = ((FontFamilyList) o).families;
		FontFamily[] b = this.families;
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

	public int hashCode() {
		int h = 0;
		for (int i = 0; i < this.families.length; ++i) {
			h = 31 * h + this.families[i].hashCode();
		}
		return h;
	}
}