package net.zamasoft.pdfg2d.gc.image;

/**
 * Represents a wrapped image.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class WrappedImage implements Image {
	protected final Image image;

	/**
	 * Creates a new WrappedImage.
	 * 
	 * @param image the image to wrap
	 */
	public WrappedImage(final Image image) {
		this.image = image;
	}

	/**
	 * Returns the wrapped image.
	 * 
	 * @return the image
	 */
	public Image getImage() {
		return this.image;
	}
}
