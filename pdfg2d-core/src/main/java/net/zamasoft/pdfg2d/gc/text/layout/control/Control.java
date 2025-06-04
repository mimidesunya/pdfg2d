package net.zamasoft.pdfg2d.gc.text.layout.control;

import net.zamasoft.pdfg2d.gc.text.Quad;

public abstract class Control extends Quad {
	public abstract int getCharOffset();

	public abstract char getControlChar();

	public abstract double getAscent();

	public abstract double getDescent();

	public final String getString() {
		return BREAK;
	}

	public String toString() {
		return String.valueOf(this.getControlChar());
	}
}
