package net.zamasoft.pdfg2d.gc.text;

/**
 * 文字列からグリフに変換するために文字列を処理します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Glypher extends CharacterHandler {
	public void setGlyphHander(GlyphHandler glyphHandler);
}
