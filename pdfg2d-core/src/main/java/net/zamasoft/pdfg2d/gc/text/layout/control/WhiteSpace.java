package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.font.FontListMetrics;

/**
 * Represents a white space character.
 */
public class WhiteSpace extends Control {
	private final FontListMetrics flm;
	private final int charOffset;
	private double advance = 0;

	public WhiteSpace(final FontListMetrics flm, final int charOffset) {
		this.flm = flm;
		this.advance = this.flm.getFontMetrics(0).getSpaceAdvance();
		this.charOffset = charOffset;
	}

	@Override
	public int getCharOffset() {
		return this.charOffset;
	}

	@Override
	public char getControlChar() {
		return '\u0020';
	}

	@Override
	public double getAdvance() {
		return this.advance;
	}

	/**
	 * Sets the word spacing.
	 * 
	 * @param wordSpacing the word spacing to set
	 */
	public void setWordSpacing(final double wordSpacing) {
		this.advance = wordSpacing + this.flm.getFontMetrics(0).getSpaceAdvance();
	}

	/**
	 * Collapses the white space (sets advance to 0).
	 */
	public void collapse() {
		this.advance = 0;
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
		return "[SPACE]";
	}
}
