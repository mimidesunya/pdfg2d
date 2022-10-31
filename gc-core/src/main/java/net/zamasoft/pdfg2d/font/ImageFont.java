package net.zamasoft.pdfg2d.font;

import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.GC;

/**
 * グラフィックコンテキストに直接描画されるフォントです。
 * これは絵文字の描画を想定したものであり、テキストの装飾、色、変形は適用しません。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface ImageFont extends DrawableFont {
	/**
	 * グリフを描画します。
	 * 
	 * @param gc
	 * @param gid
	 * @param at
	 */
	public abstract void drawGlyphForGid(GC gc, int gid, AffineTransform at);
}
