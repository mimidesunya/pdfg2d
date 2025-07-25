package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.util.FontUtils;

public class FontStyleImpl implements FontStyle, Serializable {
	private static final long serialVersionUID = 0L;

	private final FontFamilyList families;

	private final double size;

	private final Style style;

	private final Weight weight;

	private final Direction direction;

	private final FontPolicyList policy;

	public FontStyleImpl(FontFamilyList families, double size, Style style, Weight weight, Direction direction,
			FontPolicyList policy) {
		this.families = families;
		this.size = size;
		this.style = style;
		this.weight = weight;
		this.direction = direction;
		this.policy = policy;
	}

	public FontFamilyList getFamily() {
		return this.families;
	}

	public double getSize() {
		return this.size;
	}

	public Style getStyle() {
		return this.style;
	}

	public Weight getWeight() {
		return this.weight;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public FontPolicyList getPolicy() {
		return this.policy;
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FontStyle)) {
			return false;
		}
		FontStyle b = (FontStyle) o;
		return FontUtils.equals(this, b);
	}

	public int hashCode() {
		return FontUtils.hashCode(this);
	}

	public String toString() {
		return super.toString() + "[families=" + this.getFamily() + ",size=" + this.getSize() + ",style="
				+ this.getStyle() + ",weight=" + this.getWeight() + ",writingMode=" + this.getDirection() + ",policy="
				+ this.getPolicy() + "]";
	}
}
