package net.zamasoft.pdfg2d.gc.text;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontListMetrics;
import net.zamasoft.pdfg2d.gc.font.FontPolicyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.FontStyleImpl;
import net.zamasoft.pdfg2d.gc.text.hyphenation.Hyphenation;
import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.TextUnitizer;
import net.zamasoft.pdfg2d.gc.text.layout.FilterCharacterHandler;
import net.zamasoft.pdfg2d.gc.text.layout.control.LineBreak;
import net.zamasoft.pdfg2d.gc.text.layout.control.Tab;
import net.zamasoft.pdfg2d.gc.text.util.TextUtils;

public class TextLayoutHandler extends FilterCharacterHandler {
	private final GC gc;

	private boolean styleChanged = true;

	private FontFamilyList fontFamilies = FontFamilyList.SERIF;

	private double size = 12.0;

	private byte style = FontStyle.FONT_STYLE_NORMAL;

	private short weight = FontStyle.FONT_WEIGHT_400;

	private byte direction = FontStyle.DIRECTION_LTR;

	public static final FontPolicyList DEFAULT_FONT_POLICY = new FontPolicyList(
			new byte[] { FontPolicyList.FONT_POLICY_CID_KEYED, FontPolicyList.FONT_POLICY_EMBEDDED,
					FontPolicyList.FONT_POLICY_CID_IDENTITY });

	private FontPolicyList fontPolicy = DEFAULT_FONT_POLICY;

	private FontStyle fontStyle;

	public TextLayoutHandler(GC gc, Hyphenation hyphenation, GlyphHandler glyphHandler) {
		this.gc = gc;
		FilterGlyphHandler textUnitizer = new TextUnitizer(hyphenation);
		textUnitizer.setGlyphHandler(glyphHandler);
		Glypher glypher = gc.getFontManager().getGlypher();
		glypher.setGlyphHander(textUnitizer);
		this.setCharacterHandler(glypher);
	}

	public void fontStyle(FontStyle fontStyle) {
		this.fontStyle = fontStyle;
		this.styleChanged = false;
		super.fontStyle(fontStyle);
	}

	public FontStyle getFontStyle() {
		return this.fontStyle;
	}

	private char[] ch = new char[10];
	private static final Set<TextAttribute> ATTRIBUTES = new HashSet<TextAttribute>(Arrays.asList(new TextAttribute[] {
			TextAttribute.FAMILY, TextAttribute.WEIGHT, TextAttribute.SIZE, TextAttribute.POSTURE }));

	public void characters(AttributedCharacterIterator aci) {
		FontFamilyList defaultFamily = this.fontFamilies;
		double defaultSize = this.size;
		short defaultWeight = this.weight;
		byte defaultStyle = this.style;
		byte defaultDirection = this.direction;
		while (aci.current() != CharacterIterator.DONE) {
			this.setFontFamilies(
					TextUtils.toFontFamilyList((String) aci.getAttribute(TextAttribute.FAMILY), defaultFamily));
			this.setFontWeight(TextUtils.toFontWeight((Float) aci.getAttribute(TextAttribute.WEIGHT), defaultWeight));
			this.setFontSize(TextUtils.toFontSize((Float) aci.getAttribute(TextAttribute.SIZE), defaultSize));
			this.setFontStyle(TextUtils.toFontStyle((Float) aci.getAttribute(TextAttribute.POSTURE), defaultStyle));
			Byte direction = (Byte) aci.getAttribute(TextUtils.WRITING_MODE);
			this.setDirection(direction == null ? defaultDirection : direction.byteValue());

			int nextRun = aci.getRunLimit(ATTRIBUTES);
			int len = nextRun - aci.getIndex();
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

	public void characters(int charOffset, char[] ch, int off, int len) {
		if (len == 0) {
			return;
		}
		this.applyStyle();
		int ooff = 0;
		for (int i = 0; i < len; ++i) {
			char c = ch[i + off];
			Quad quad = null;
			if (Character.isISOControl(c)) {
				FontListMetrics flm = this.gc.getFontManager().getFontListMetrics(this.fontStyle);
				switch (c) {
				case '\n':
					quad = new LineBreak(flm, i);
					break;
				case '\t':
					quad = new Tab(flm, i);
					break;
				}
				if (quad != null) {
					if (i > ooff) {
						super.characters(charOffset + ooff, ch, off + ooff, i - ooff);
					}
					ooff = i + 1;
					super.quad(quad);
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

	public void setFontFamilies(FontFamilyList families) {
		if (this.fontFamilies.equals(families)) {
			return;
		}
		this.changed();
		this.fontFamilies = families;
	}

	public void setFontSize(double size) {
		if (this.size == size) {
			return;
		}
		this.changed();
		this.size = size;
	}

	public void setFontStyle(byte style) {
		if (this.style == style) {
			return;
		}
		this.changed();
		this.style = style;
	}

	public void setFontWeight(short weight) {
		if (this.weight == weight) {
			return;
		}
		this.changed();
		this.weight = weight;
	}

	public void setDirection(byte direction) {
		if (this.direction == direction) {
			return;
		}
		this.changed();
		this.direction = direction;
	}

	public void setFontPolicy(FontPolicyList fontPolicy) {
		if (this.fontPolicy.equals(fontPolicy)) {
			return;
		}
		this.changed();
		this.fontPolicy = fontPolicy;
	}

	public void characters(String text) throws GraphicsException {
		char[] ch = text.toCharArray();
		this.characters(-1, ch, 0, ch.length);
	}
}
