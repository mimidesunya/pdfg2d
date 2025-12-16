package net.zamasoft.pdfg2d.gc;

/**
 * Thrown when a graphics operation fails.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class GraphicsException extends RuntimeException {
	private static final long serialVersionUID = 0;

	/**
	 * Creates a new GraphicsException.
	 * 
	 * @param message the detail message
	 * @param t       the cause
	 */
	public GraphicsException(final String message, final Throwable t) {
		super(message, t);
	}

	/**
	 * Creates a new GraphicsException.
	 * 
	 * @param message the detail message
	 */
	public GraphicsException(final String message) {
		super(message);
	}

	/**
	 * Creates a new GraphicsException.
	 * 
	 * @param t the cause
	 */
	public GraphicsException(final Throwable t) {
		super(t);
	}
}
