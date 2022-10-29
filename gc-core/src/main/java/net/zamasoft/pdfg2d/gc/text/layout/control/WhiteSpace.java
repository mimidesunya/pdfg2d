package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.font.FontListMetrics;

public class WhiteSpace extends Control {
	private final FontListMetrics flm;
	private final int charOffset;
	private double advance = 0;

	public WhiteSpace(FontListMetrics flm, int charOffset) {
		this.flm = flm;
		this.advance = this.flm.getFontMetrics(0).getSpaceAdvance();
		this.charOffset = charOffset;
	}

	public int getCharOffset() {
		return this.charOffset;
	}

	public char getControlChar() {
		return '\u0020';
	}

	public double getAdvance() {
		return advance;
	}

	public void setWordSpacing(double wordSpacing) {
		this.advance = wordSpacing + this.flm.getFontMetrics(0).getSpaceAdvance();
	}

	public void collapse() {
		this.advance = 0;
	}

	public double getAscent() {
		return this.flm.getMaxAscent();
	}

	public double getDescent() {
		return this.flm.getMaxDescent();
	}

	public String toString() {
		return "[SPACE]";
	}
}
