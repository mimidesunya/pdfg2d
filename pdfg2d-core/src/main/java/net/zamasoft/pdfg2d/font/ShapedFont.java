package net.zamasoft.pdfg2d.font;

import java.awt.Shape;

/**
 * A font that can return the shape of a glyph.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface ShapedFont extends DrawableFont {
	/**
	 * Returns the shape of a glyph.
	 * 
	 * @param gid the glyph ID
	 * @return the shape of the glyph
	 */
	public abstract Shape getShapeByGID(int gid);
}
