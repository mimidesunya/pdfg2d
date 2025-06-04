package net.zamasoft.pdfg2d.gc.paint;

public interface Paint {
	public static enum Type {
		COLOR, PATTERN, LINEAR_GRADIENT, RADIAL_GRADIENT
	}

	public Type getPaintType();
}
