package net.zamasoft.pdfg2d.gc.text;

import java.io.Closeable;

import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * 文字列からグリフに変換するために文字列を処理します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface CharacterHandler extends Closeable {
	/**
	 * カレントのスタイルを設定します。
	 * 
	 * @param fontStyle
	 */
	public void fontStyle(FontStyle fontStyle);

	/**
	 * 処理する文字列を送ります。
	 * 
	 * @param ch
	 * @param off
	 * @param len
	 */
	public void characters(int charOffset, char[] ch, int off, int len);

	/**
	 * 込め物を入れます。
	 * 
	 * @param quad
	 */
	public void quad(Quad quad);

	/**
	 * テキストを区切ります。
	 * 
	 * <b>このメソッドはwordBreak()を呼びます。</b>
	 */
	public void flush();

	/**
	 * 段落を終わります。
	 * 
	 * <b>このメソッドはwordBreak()を呼びます。</b>
	 */
	public void close();
}
