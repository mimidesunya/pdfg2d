package net.zamasoft.pdfg2d.gc.image.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.image.WrappedImage;

/**
 * UAの単位にスケールされた画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class TransformedImage extends WrappedImage {
	private final AffineTransform at;
	private Double cachedWidth = null;
	private Double cachedHeight = null;

	public TransformedImage(Image image, AffineTransform at) {
		super(image);
		assert image != null;
		assert at != null;
		this.at = at;
	}

	public AffineTransform getTransform() {
		return this.at;
	}

	public void drawTo(GC gc) {
		gc.begin();
		gc.transform(this.at);
		this.image.drawTo(gc);
		gc.end();
	}

	public String getAltString() {
		return this.image.getAltString();
	}

	public double getWidth() {
		if (cachedWidth != null) return cachedWidth;
		calculateDimensions();
		return cachedWidth;
	}

	public double getHeight() {
		if (cachedHeight != null) return cachedHeight;
		calculateDimensions();
		return cachedHeight;
	}

	private void calculateDimensions() {
		double w = image.getWidth();
		double h = image.getHeight();

		Point2D[] pts = new Point2D[] {
			new Point2D.Double(0, 0),
			new Point2D.Double(w, 0),
			new Point2D.Double(0, h),
			new Point2D.Double(w, h)
		};

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (Point2D pt : pts) {
			Point2D dst = at.transform(pt, null);
			minX = Math.min(minX, dst.getX());
			maxX = Math.max(maxX, dst.getX());
			minY = Math.min(minY, dst.getY());
			maxY = Math.max(maxY, dst.getY());
		}

		cachedWidth = Math.abs(maxX - minX);
		cachedHeight = Math.abs(maxY - minY);
	}

	public String toString() {
		return super.toString() + "/image=" + this.image + ",at=" + this.at;
	}
}