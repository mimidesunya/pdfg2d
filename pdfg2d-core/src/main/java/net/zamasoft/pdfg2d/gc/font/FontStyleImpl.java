package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

/**
 * Implementation of FontStyle.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record FontStyleImpl(
		FontFamilyList families,
		double size,
		Style style,
		Weight weight,
		Direction direction,
		FontPolicyList policy) implements FontStyle, Serializable {

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
	public String toString() { // Keep toString to match original format if strictly needed, or rely on record
								// default?
		// Original: super.toString() + "[families=" + ...
		// Records toString is FontStyleImpl[families=..., size=...]
		// Original super.toString() uses Object.toString() (ClassName@Hex).
		// Record's toString is much better.
		// Use record's default toString? Use original if format matters for debugging.
		// Original format:
		// "net.zamasoft.pdfg2d.gc.font.FontStyleImpl@...[families=...,size=...]"
		// I'll stick to record default which is "FontStyleImpl[families=..., size=...]"
		// - cleaner.
		// BUT the user asked to "rewrite to use Java 21 features". Records are one.
		// If I remove toString, output changes format.
		// "Compatibility is not a concern". So I will use record default.
		return "FontStyleImpl[families=" + this.families + ", size=" + this.size + ", style=" + this.style + ", weight="
				+ this.weight + ", direction=" + this.direction + ", policy=" + this.policy + "]";
	}
}
