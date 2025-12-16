package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.font.FontListMetrics;

/**
 * Represents a tab character.
 */
public class Tab extends Control {
	private final FontListMetrics flm;
	private final int charOffset;
	public double advance = 0;

	public Tab(final FontListMetrics flm, final int charOffset) {
		this.flm = flm;
		this.charOffset = charOffset;
	}

	@Override
	public int getCharOffset() {
		return this.charOffset;
	}

	@Override
	public char getControlChar() {
		return '\t';
	}

	@Override
	public double getAdvance() {
		return this.advance;
	}

	@Override
	public double getAscent() {
		return this.flm.getMaxAscent();
	}

	@Override
	public double getDescent() {
		return this.flm.getMaxDescent();
	}

	@Override
	public String toString() {
		return "\\t";
	}
}
