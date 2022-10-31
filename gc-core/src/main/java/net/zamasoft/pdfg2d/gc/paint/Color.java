package net.zamasoft.pdfg2d.gc.paint;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Color extends Paint {
	public static final short RGB = 1;

	public static final short CMYK = 2;

	public static final short GRAY = 3;

	public static final short RGBA = 4;

	public short getColorType();

	public float getRed();

	public float getGreen();

	public float getBlue();

	public float getAlpha();

	public float getComponent(int i);
}