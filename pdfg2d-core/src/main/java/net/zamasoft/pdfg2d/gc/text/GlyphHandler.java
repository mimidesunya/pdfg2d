package net.zamasoft.pdfg2d.gc.text;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * グリフ単位で分解されたテキストの送り先です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface GlyphHandler {
	/**
	 * テキストランを開始します。
	 * 
	 * @param fontStyle
	 * @param fontMetrics
	 */
	public void startTextRun(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics);

	/**
	 * テキストランを終わります。
	 */
	public void endTextRun();

	/**
	 * グリフを送ります。
	 * 
	 * @param charOffset
	 * @param ch
	 * @param coff
	 * @param clen
	 * @param gid
	 */
	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid);

	/**
	 * 込め物を送ります。
	 * 
	 * @param quad
	 */
	public void quad(Quad quad);

	/**
	 * 現在までのテキストを書き出します。これを明示的に呼ぶと禁則処理を無視してテキストを分断することがあります。
	 */
	public void flush();
	
	/**
	 * テキストの書き出しを終了します。
	 */
	public void finish();
}
