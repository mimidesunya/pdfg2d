package net.zamasoft.pdfg2d.font;

import java.io.IOException;

/**
 * PDFのフォント格納オブジェクトです。
 * 
 * このオブジェクトはスレッドセーフではありません。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FontStore {
	public Font useFont(FontSource metaFont) throws IOException;
}
