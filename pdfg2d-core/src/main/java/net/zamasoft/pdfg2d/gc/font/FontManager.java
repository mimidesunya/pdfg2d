package net.zamasoft.pdfg2d.gc.font;

import java.io.IOException;
import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.text.Glypher;

/**
 * テキストを複数のグリフに分解します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FontManager extends Serializable {
	public void addFontFace(FontFace face) throws IOException;

	public FontListMetrics getFontListMetrics(FontStyle fontStyle);

	public Glypher getGlypher();
}