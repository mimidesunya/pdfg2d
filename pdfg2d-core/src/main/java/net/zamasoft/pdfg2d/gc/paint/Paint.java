package net.zamasoft.pdfg2d.gc.paint;

/**
 * Represents a paint used for drawing operations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Paint {
	public enum Type {
		COLOR, PATTERN, LINEAR_GRADIENT, RADIAL_GRADIENT
	}

	/**
	 * Returns the type of this paint.
	 * 
	 * @return the paint type
	 */
	public Type getPaintType();
}
