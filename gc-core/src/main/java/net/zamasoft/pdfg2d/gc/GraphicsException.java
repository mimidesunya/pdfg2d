package net.zamasoft.pdfg2d.gc;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class GraphicsException extends RuntimeException {
	private static final long serialVersionUID = 0;

	public GraphicsException(String message, Throwable t) {
		super(message, t);
	}

	public GraphicsException(String message) {
		super(message);
	}

	public GraphicsException(Throwable t) {
		super(t);
	}
}
