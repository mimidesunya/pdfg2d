package net.zamasoft.pdfg2d.pdf.font.cid;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CMapException extends Exception {
	private static final long serialVersionUID = 0;

	public CMapException(String message) {
		super(message);
	}

	public CMapException(String message, Throwable t) {
		super(message, t);
	}
}
