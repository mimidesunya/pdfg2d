package net.zamasoft.pdfg2d.font;

import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;

public abstract class AbstractFontSource implements FontSource {
	private static final long serialVersionUID = 1L;
	/**
	 * デフォルトのアセント。
	 */
	protected static final short DEFAULT_ASCENT = 860;

	/**
	 * デフォルトのディセント。
	 */
	protected static final short DEFAULT_DESCENT = 140;

	/**
	 * デフォルトのx-height。
	 */
	protected static final short DEFAULT_X_HEIGHT = 500;

	/**
	 * デフォルトのcap-height。
	 */
	protected static final short DEFAULT_CAP_HEIGHT = 700;

	private static final String[] EMPTY_STRINGS = new String[0];

	protected String[] aliases = EMPTY_STRINGS;

	protected Weight weight = Weight.W_400;

	protected boolean isItalic = false;

	public String[] getAliases() {
		return this.aliases;
	}

	public final void setItalic(boolean isItalic) {
		this.isItalic = isItalic;
	}

	public final boolean isItalic() {
		return this.isItalic;
	}

	public final void setWeight(Weight weight) {
		this.weight = weight;
	}

	public final Weight getWeight() {
		return this.weight;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer(this.getFontName());
		String[] aliases = this.getAliases();
		for (int i = 0; i < aliases.length; ++i) {
			buff.append("; ").append(aliases[i]);
		}
		buff.append(this.getClass());
		return buff.toString();
	}
}
