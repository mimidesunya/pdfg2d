package net.zamasoft.pdfg2d.gc.image.util;

import java.awt.geom.AffineTransform;

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
		return this.image.getWidth() * this.at.getScaleX();
	}

	public double getHeight() {
		return this.image.getHeight() * this.at.getScaleY();
	}

	public String toString() {
		return super.toString() + "/image=" + this.image + ",at=" + this.at;
	}
}