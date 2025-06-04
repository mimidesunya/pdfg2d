package net.zamasoft.pdfg2d.svg;

import java.awt.geom.Dimension2D;

public class Dimension2DImpl extends Dimension2D {
	protected double width, height;

	public Dimension2DImpl(double width, double height) {
		this.setSize(width, height);
	}

	public double getHeight() {
		return this.height;
	}

	public double getWidth() {
		return this.width;
	}

	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public String toString() {
		return super.toString() + "[" + this.width + "," + this.height + "]";
	}
}
