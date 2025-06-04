package net.zamasoft.pdfg2d.gc.image;

/**
 * 画像を内包する画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class WrappedImage implements Image {
	protected final Image image;

	public WrappedImage(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return this.image;
	}
}
