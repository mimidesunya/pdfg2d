package net.zamasoft.pdfg2d.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Color extends Paint {
	public static enum Type {
		RGB, CMYK, GRAY, RGBA
	}

	public Type getColorType();

	public float getRed();

	public float getGreen();

	public float getBlue();

	public float getAlpha();

	public float getComponent(int i);
}