package net.zamasoft.pdfg2d.pdf.font.cid;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CMapException.java 1565 2018-07-04 11:51:25Z miyabe $
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
