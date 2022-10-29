package net.zamasoft.pdfg2d.font;

import java.awt.Shape;

/**
 * フォントの字形を得られるフォントです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ShapedFont.java 1565 2018-07-04 11:51:25Z miyabe $
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
