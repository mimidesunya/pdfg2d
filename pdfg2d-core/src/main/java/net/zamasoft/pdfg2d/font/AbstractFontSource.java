package net.zamasoft.pdfg2d.font;

import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;

/**
 * An abstract implementation of {@link FontSource}.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class AbstractFontSource implements FontSource {
	private static final long serialVersionUID = 1L;
	/**
	 * Default ascent.
	 */
	protected static final short DEFAULT_ASCENT = 860;

	/**
	 * Default descent.
	 */
	protected static final short DEFAULT_DESCENT = 140;

	/**
	 * Default x-height.
	 */
	protected static final short DEFAULT_X_HEIGHT = 500;

	/**
	 * Default cap-height.
	 */
	protected static final short DEFAULT_CAP_HEIGHT = 700;

	private static final String[] EMPTY_STRINGS = new String[0];

	protected String[] aliases;

	protected Weight weight = Weight.W_400;

	protected boolean isItalic = false;

	/**
	 * Constructs a new AbstractFontSource.
	 */
	public AbstractFontSource() {
		this.aliases = EMPTY_STRINGS;
	}

	@Override
	public String[] getAliases() {
		return this.aliases;
	}

	/**
	 * Sets the italic status of this font source.
	 * 
	 * @param isItalic true if italic, false otherwise
	 */
	public final void setItalic(final boolean isItalic) {
		this.isItalic = isItalic;
	}

	@Override
	public final boolean isItalic() {
		return this.isItalic;
	}

	/**
	 * Sets the weight of this font source.
	 * 
	 * @param weight the font weight
	 */
	public final void setWeight(final Weight weight) {
		this.weight = weight;
	}

	@Override
	public final Weight getWeight() {
		return this.weight;
	}

	@Override
	public String toString() {
		final var buff = new StringBuilder(this.getFontName());
		final var aliases = this.getAliases();
		if (aliases != null && aliases.length > 0) {
			buff.append("; ").append(String.join("; ", aliases));
		}
		buff.append(this.getClass());
		return buff.toString();
	}
}
