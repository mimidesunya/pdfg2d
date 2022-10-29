package net.zamasoft.pdfg2d.gc.font;

import java.io.IOException;
import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.text.Glypher;

/**
 * テキストを複数のグリフに分解します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontManager.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface FontManager extends Serializable {
	public void addFontFace(FontFace face) throws IOException;

	public FontListMetrics getFontListMetrics(FontStyle fontStyle);

	public Glypher getGlypher();
}