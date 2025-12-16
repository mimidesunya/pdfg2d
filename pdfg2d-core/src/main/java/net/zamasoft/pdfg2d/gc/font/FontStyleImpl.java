package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.util.FontUtils;

/**
 * Implementation of FontStyle.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontStyleImpl implements FontStyle, Serializable {
	private static final long serialVersionUID = 0L;

	private final FontFamilyList families;

	private final double size;

	private final Style style;

	private final Weight weight;

	private final Direction direction;

	private final FontPolicyList policy;

	/**
	 * Creates a new FontStyleImpl.
	 * 
	 * @param families  the font family list
	 * @param size      the font size
	 * @param style     the font style
	 * @param weight    the font weight
	 * @param direction the writing direction
	 * @param policy    the font policy list
	 */
	public FontStyleImpl(final FontFamilyList families, final double size, final Style style, final Weight weight,
			final Direction direction, final FontPolicyList policy) {
		this.families = families;
		this.size = size;
		this.style = style;
		this.weight = weight;
		this.direction = direction;
		this.policy = policy;
	}

	@Override
	public FontFamilyList getFamily() {
		return this.families;
	}

	@Override
	public double getSize() {
		return this.size;
	}

	@Override
	public Style getStyle() {
		return this.style;
	}

	@Override
	public Weight getWeight() {
		return this.weight;
	}

	@Override
	public Direction getDirection() {
		return this.direction;
	}

	@Override
	public FontPolicyList getPolicy() {
		return this.policy;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FontStyle)) {
			return false;
		}
		final var b = (FontStyle) o;
		return FontUtils.equals(this, b);
	}

	@Override
	public int hashCode() {
		return FontUtils.hashCode(this);
	}

	@Override
	public String toString() {
		return super.toString() + "[families=" + this.getFamily() + ",size=" + this.getSize() + ",style="
				+ this.getStyle() + ",weight=" + this.getWeight() + ",writingMode=" + this.getDirection() + ",policy="
				+ this.getPolicy() + "]";
	}
}
