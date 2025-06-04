package net.zamasoft.pdfg2d.gc.image;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;

/**
 * 画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Image {
	/**
	 * 画像の幅を返します。
	 * 
	 * @return
	 */
	public double getWidth();

	/**
	 * 画像の高さを返します。
	 * 
	 * @return
	 */
	public double getHeight();

	/**
	 * 画像を描画します。
	 * 
	 * @param gc
	 */
	public void drawTo(GC gc) throws GraphicsException;

	/**
	 * 画像に相当する文字列を返します。
	 * 
	 * @return
	 */
	public String getAltString();
}
