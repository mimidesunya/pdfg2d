package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.text.Quad;

/**
 * Abstract class representing a control character in the text layout.
 */
public abstract class Control extends Quad {
	public abstract int getCharOffset();

	public abstract char getControlChar();

	public abstract double getAscent();

	public abstract double getDescent();

	@Override
	public final String getString() {
		return BREAK;
	}

	@Override
	public String toString() {
		return String.valueOf(this.getControlChar());
	}
}
