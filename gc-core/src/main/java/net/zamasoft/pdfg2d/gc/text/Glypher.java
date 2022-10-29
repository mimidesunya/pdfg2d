package net.zamasoft.pdfg2d.gc.text;

/**
 * 文字列からグリフに変換するために文字列を処理します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Glypher.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface Glypher extends CharacterHandler {
	public void setGlyphHander(GlyphHandler glyphHandler);
}
