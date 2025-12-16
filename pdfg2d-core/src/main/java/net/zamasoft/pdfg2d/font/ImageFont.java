package net.zamasoft.pdfg2d.font;

import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.GC;

/**
 * A font that draws directly to the graphics context.
 * This is intended for drawing emojis and does not apply text decorations,
 * colors, or transformations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface ImageFont extends DrawableFont {
	/**
	 * Draws a glyph.
	 * 
	 * @param gc  the graphics context
	 * @param gid the glyph ID
	 * @param at  the affine transform
	 */
	public abstract void drawGlyphForGid(GC gc, int gid, AffineTransform at);
}
