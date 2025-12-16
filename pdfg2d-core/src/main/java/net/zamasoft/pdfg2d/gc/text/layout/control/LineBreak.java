package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.font.FontListMetrics;

/**
 * Represents a line break.
 */
public class LineBreak extends Control {
	private final FontListMetrics flm;
	private final int charOffset;

	public LineBreak(final FontListMetrics flm, final int charOffset) {
		this.flm = flm;
		this.charOffset = charOffset;
	}

	@Override
	public int getCharOffset() {
		return this.charOffset;
	}

	@Override
	public char getControlChar() {
		return '\n';
	}

	@Override
	public double getAdvance() {
		return 0;
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
		return "\\n";
	}

}
