package net.zamasoft.pdfg2d.font;

import java.io.IOException;

/**
 * PDFのフォント格納オブジェクトです。
 * 
 * このオブジェクトはスレッドセーフではありません。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontStore.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface FontStore {
	public Font useFont(FontSource metaFont) throws IOException;
}
