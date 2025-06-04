package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.font.FontListMetrics;

public class Tab extends Control {
	private final FontListMetrics flm;
	private final int charOffset;
	public double advance = 0;

	public Tab(FontListMetrics flm, int charOffset) {
		this.flm = flm;
		this.charOffset = charOffset;
	}

	public int getCharOffset() {
		return this.charOffset;
	}

	public char getControlChar() {
		return '\t';
	}

	public double getAdvance() {
		return this.advance;
	}

	public double getAscent() {
		return this.flm.getMaxAscent();
	}

	public double getDescent() {
		return this.flm.getMaxDescent();
	}

	public String toString() {
		return "\\t";
	}
}
