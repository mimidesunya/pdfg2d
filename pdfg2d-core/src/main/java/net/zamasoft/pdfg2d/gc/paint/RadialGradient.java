package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

public class RadialGradient implements Paint {
	protected final double cx, cy;
	protected final double radius;
	protected final double fx, fy;
	protected final Color[] colors;
	protected final double[] fractions;
	protected final AffineTransform transform;

	public RadialGradient(double cx, double cy, double radius, double fx, double fy, double[] fractions, Color[] colors,
			AffineTransform transform) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
		this.fx = fx;
		this.fy = fy;
		this.colors = colors;
		this.fractions = fractions;
		this.transform = transform;
	}

	public Paint.Type getPaintType() {
		return Paint.Type.RADIAL_GRADIENT;
	}

	public double getCX() {
		return this.cx;
	}

	public double getCY() {
		return this.cy;
	}

	public double getRadius() {
		return this.radius;
	}

	public double getFX() {
		return this.fx;
	}

	public double getFY() {
		return this.fy;
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
}
