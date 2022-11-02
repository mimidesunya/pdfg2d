package net.zamasoft.pdfg2d.gc.text.util;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;

import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

public final class TextUtils {
	private TextUtils() {
		// ignore
	}

	static class WritingMode extends Attribute {
		private static final long serialVersionUID = 1L;

		protected WritingMode() {
			super("WRITING_MODE");
		}
	}

	public static final Attribute WRITING_MODE = new WritingMode();
	public static final Byte WRITING_MODE_HORIZONTAL = Byte.valueOf(FontStyle.DIRECTION_LTR);
	public static final Byte WRITING_MODE_VERTICAL = Byte.valueOf(FontStyle.DIRECTION_TB);

	public static final FontFamilyList toFontFamilyList(String awtFamily, FontFamilyList defaultFamily) {
		if (awtFamily == null) {
			return defaultFamily;
		}
		return FontFamilyList.create(awtFamily);
	}

	public static final short toFontWeight(Float awtWeight, short defaultWeight) {
		if (awtWeight == null) {
			return defaultWeight;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_EXTRA_LIGHT) <= 0) {
			return FontStyle.FONT_WEIGHT_100;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_LIGHT) <= 0) {
			return FontStyle.FONT_WEIGHT_200;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_DEMILIGHT) <= 0) {
			return FontStyle.FONT_WEIGHT_300;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_REGULAR) <= 0) {
			return FontStyle.FONT_WEIGHT_400;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_SEMIBOLD) <= 0) {
			return FontStyle.FONT_WEIGHT_500;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_MEDIUM) <= 0) {
			return FontStyle.FONT_WEIGHT_500;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_DEMIBOLD) <= 0) {
			return FontStyle.FONT_WEIGHT_600;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_BOLD) <= 0) {
			return FontStyle.FONT_WEIGHT_700;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_HEAVY) <= 0) {
			return FontStyle.FONT_WEIGHT_700;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_EXTRABOLD) <= 0) {
			return FontStyle.FONT_WEIGHT_800;
		}
		return FontStyle.FONT_WEIGHT_900;
	}

	public static final double toFontSize(Float awtSize, double defaultSize) {
		return awtSize != null ? awtSize.doubleValue() : defaultSize;
	}

	public static final byte toFontStyle(Float awtPosture, byte defaultStyle) {
		if (awtPosture == null) {
			return defaultStyle;
		} else if (awtPosture.equals(TextAttribute.POSTURE_OBLIQUE)) {
			return FontStyle.FONT_STYLE_OBLIQUE;
		}
		return FontStyle.FONT_STYLE_NORMAL;
	}
}
