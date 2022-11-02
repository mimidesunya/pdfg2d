package net.zamasoft.pdfg2d.gc.paint;

public interface Paint {
	public static final short COLOR = 1;
	public static final short PATTERN = 2;
	public static final short LINEAR_GRADIENT = 3;
	public static final short RADIAL_GRADIENT = 4;

	public short getPaintType();
}
