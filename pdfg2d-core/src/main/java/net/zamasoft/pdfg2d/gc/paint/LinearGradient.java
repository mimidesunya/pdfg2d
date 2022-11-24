package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

public class LinearGradient implements Paint {
	protected final double x1, y1;
	protected final double x2, y2;
	protected final Color[] colors;
	protected final double[] fractions;
	protected final AffineTransform transform;

	public LinearGradient(double x1, double y1, double x2, double y2, double[] fractions, Color[] colors,
			AffineTransform transform) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.colors = colors;
		this.fractions = fractions;
		this.transform = transform;
		if (colors == null) {
			throw new NullPointerException("Colors cannnot be null.");
		}
		if (fractions == null) {
			throw new NullPointerException("Fractions cannnot be null.");
		}
		if (transform == null) {
			throw new NullPointerException("Transform cannnot be null.");
		}
	}

	public short getPaintType() {
		return LINEAR_GRADIENT;
	}

	public double getX1() {
		return this.x1;
	}

	public double getY1() {
		return this.y1;
	}

	public double getX2() {
		return this.x2;
	}

	public double getY2() {
		return this.y2;
	}

	public Color[] getColors() {
		return this.colors;
	}

	public double[] getFractions() {
		return this.fractions;
	}

	public AffineTransform getTransform() {
		return this.transform;
	}

	public String toString() {
		return super.toString() + "[x1=" + x1 + ",y1=" + y1 + ",x2=" + x2 + ",y2=" + y2 + ",fractions=" + fractions
				+ ",colors=" + colors + ",transform=" + transform + "]";
	}
}
