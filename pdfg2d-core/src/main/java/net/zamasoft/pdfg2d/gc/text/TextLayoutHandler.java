package net.zamasoft.pdfg2d.gc.text;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import java.util.Set;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontListMetrics;
import net.zamasoft.pdfg2d.gc.font.FontPolicyList;
import net.zamasoft.pdfg2d.gc.font.FontPolicyList.FontPolicy;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;
import net.zamasoft.pdfg2d.gc.font.FontStyleImpl;
import net.zamasoft.pdfg2d.gc.text.breaking.TextBreakingRules;
import net.zamasoft.pdfg2d.gc.text.breaking.impl.TextAtomizer;
import net.zamasoft.pdfg2d.gc.text.layout.FilterCharacterHandler;
import net.zamasoft.pdfg2d.gc.text.layout.control.LineBreak;
import net.zamasoft.pdfg2d.gc.text.layout.control.Tab;
import net.zamasoft.pdfg2d.gc.text.util.TextUtils;

public class TextLayoutHandler extends FilterCharacterHandler {
	private final GC gc;

	private boolean styleChanged = true;

	private FontFamilyList fontFamilies = FontFamilyList.SERIF;

	private double size = 12.0;

	private Style style = Style.NORMAL;

	private Weight weight = Weight.W_400;

	private Direction direction = Direction.LTR;

	public static final FontPolicyList DEFAULT_FONT_POLICY = new FontPolicyList(
			new FontPolicy[] { FontPolicy.CID_KEYED, FontPolicy.EMBEDDED,
					FontPolicy.CID_IDENTITY });

	private FontPolicyList fontPolicy = DEFAULT_FONT_POLICY;

	private FontStyle fontStyle;

	public TextLayoutHandler(final GC gc, final TextBreakingRules rules, final GlyphHandler glyphHandler) {
		this.gc = gc;
		final FilterGlyphHandler textAtomizer = new TextAtomizer(rules);
		textAtomizer.setGlyphHandler(glyphHandler);
		final TextShaper glypher = gc.getFontManager().getTextShaper();
		glypher.setGlyphHandler(textAtomizer);
		this.setCharacterHandler(glypher);
	}

	@Override
	public void fontStyle(final FontStyle fontStyle) {
		this.fontStyle = fontStyle;
		this.styleChanged = false;
		super.fontStyle(fontStyle);
	}

	public FontStyle getFontStyle() {
		return this.fontStyle;
	}

	private char[] ch = new char[10];
	private static final Set<TextAttribute> ATTRIBUTES = Set.of(
			TextAttribute.FAMILY, TextAttribute.WEIGHT, TextAttribute.SIZE, TextAttribute.POSTURE);

	public void characters(final AttributedCharacterIterator aci) {
		final FontFamilyList defaultFamily = this.fontFamilies;
		final double defaultSize = this.size;
		final Weight defaultWeight = this.weight;
		final Style defaultStyle = this.style;
		final Direction defaultDirection = this.direction;
		while (aci.current() != CharacterIterator.DONE) {
			this.setFontFamilies(
					TextUtils.toFontFamilyList((String) aci.getAttribute(TextAttribute.FAMILY), defaultFamily));
			this.setFontWeight(TextUtils.toFontWeight((Float) aci.getAttribute(TextAttribute.WEIGHT), defaultWeight));
			this.setFontSize(TextUtils.toFontSize((Float) aci.getAttribute(TextAttribute.SIZE), defaultSize));
			this.setFontStyle(TextUtils.toFontStyle((Float) aci.getAttribute(TextAttribute.POSTURE), defaultStyle));
			final Direction direction = (Direction) aci.getAttribute(TextUtils.WRITING_MODE);
			this.setDirection(direction == null ? defaultDirection : direction);

			final int nextRun = aci.getRunLimit(ATTRIBUTES);
			final int len = nextRun - aci.getIndex();
			if (len > this.ch.length) {
				this.ch = new char[len];
			}
			for (int i = 0; aci.getIndex() != nextRun; ++i) {
				this.ch[i] = aci.current();
				aci.next();
			}

			this.characters(-1, this.ch, 0, len);
		}
		this.setFontFamilies(defaultFamily);
		this.setFontSize(defaultSize);
		this.setFontWeight(defaultWeight);
		this.setFontStyle(defaultStyle);
		this.setDirection(defaultDirection);
	}

	public void applyStyle() {
		if (this.styleChanged) {
			this.fontStyle(new FontStyleImpl(this.fontFamilies, this.size, this.style, this.weight, this.direction,
					this.fontPolicy));
		}
	}

	@Override
	public void characters(final int charOffset, final char[] ch, final int off, final int len) {
		if (len == 0) {
			return;
		}
		this.applyStyle();
		int ooff = 0;
		for (int i = 0; i < len; ++i) {
			final char c = ch[i + off];
			TextControl control = null;
			if (Character.isISOControl(c)) {
				final FontListMetrics flm = this.gc.getFontManager().getFontListMetrics(this.fontStyle);
				control = switch (c) {
					case '\n' -> new LineBreak(flm, i);
					case '\t' -> new Tab(flm, i);
					default -> null;
				};
				if (control != null) {
					if (i > ooff) {
						super.characters(charOffset + ooff, ch, off + ooff, i - ooff);
					}
					ooff = i + 1;
					super.control(control);
					continue;
				}
			}
		}
		if (len > ooff) {
			super.characters(charOffset + ooff, ch, off + ooff, len - ooff);
		}
	}

	private void changed() {
		this.flush();
		this.styleChanged = true;
	}

	public void setFontFamilies(final FontFamilyList families) {
		if (this.fontFamilies.equals(families)) {
			return;
		}
		this.changed();
		this.fontFamilies = families;
	}

	public void setFontSize(final double size) {
		if (this.size == size) {
			return;
		}
		this.changed();
		this.size = size;
	}

	public void setFontStyle(final Style style) {
		if (this.style == style) {
			return;
		}
		this.changed();
		this.style = style;
	}

	public void setFontWeight(final Weight weight) {
		if (this.weight == weight) {
			return;
		}
		this.changed();
		this.weight = weight;
	}

	public void setDirection(final Direction direction) {
		if (this.direction == direction) {
			return;
		}
		this.changed();
		this.direction = direction;
	}

	public void setFontPolicy(final FontPolicyList fontPolicy) {
		if (this.fontPolicy.equals(fontPolicy)) {
			return;
		}
		this.changed();
		this.fontPolicy = fontPolicy;
	}

	public void characters(final String text) throws GraphicsException {
		final char[] ch = text.toCharArray();
		this.characters(-1, ch, 0, ch.length);
	}
}
