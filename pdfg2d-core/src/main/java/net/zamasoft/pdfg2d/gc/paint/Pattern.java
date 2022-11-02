package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * 画像によるパターンです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class Pattern implements Paint {
	protected final Image image;

	protected final AffineTransform at;

	public Pattern(Image image, AffineTransform at) {
		assert at == null || at.getScaleX() != 0;
		assert at == null || at.getScaleY() != 0;
		this.image = image;
		this.at = at;
	}

	public short getPaintType() {
		return PATTERN;
	}

	public AffineTransform getTransform() {
		return this.at;
	}

	public Image getImage() {
		return this.image;
	}

	public String toString() {
		return super.toString() + "/image=" + this.image + ",at=" + this.at;
	}
}
