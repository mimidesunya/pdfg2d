package net.zamasoft.pdfg2d.font;

import java.awt.Shape;

/**
 * フォントの字形を得られるフォントです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface ShapedFont extends DrawableFont {
	/**
	 * フォントの字形を返します。
	 * 
	 * @param gid
	 * @return フォントの字形。
	 */
	public abstract Shape getShapeByGID(int gid);
}
