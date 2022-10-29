package net.zamasoft.pdfg2d.gc;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: GraphicsException.java 1565 2018-07-04 11:51:25Z miyabe $
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
