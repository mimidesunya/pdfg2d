package net.zamasoft.pdfg2d.gc.text.util;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;

import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;

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

	public static final FontFamilyList toFontFamilyList(String awtFamily, FontFamilyList defaultFamily) {
		if (awtFamily == null) {
			return defaultFamily;
		}
		return FontFamilyList.create(awtFamily);
	}

	public static final Weight toFontWeight(Float awtWeight, Weight defaultWeight) {
		if (awtWeight == null) {
			return defaultWeight;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_EXTRA_LIGHT) <= 0) {
			return Weight.W_100;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_LIGHT) <= 0) {
			return Weight.W_200;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_DEMILIGHT) <= 0) {
			return Weight.W_300;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_REGULAR) <= 0) {
			return Weight.W_400;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_SEMIBOLD) <= 0) {
			return Weight.W_500;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_MEDIUM) <= 0) {
			return Weight.W_500;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_DEMIBOLD) <= 0) {
			return Weight.W_600;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_BOLD) <= 0) {
			return Weight.W_700;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_HEAVY) <= 0) {
			return Weight.W_700;
		} else if (awtWeight.compareTo(TextAttribute.WEIGHT_EXTRABOLD) <= 0) {
			return Weight.W_800;
		}
		return Weight.W_900;
	}

	public static final Weight decodeFontWeight(short weight) {
		if (weight < 149) {
			return Weight.W_100;
		} else if (weight < 249) {
			return Weight.W_200;
		} else if (weight < 349) {
			return Weight.W_300;
		} else if (weight < 449) {
			return Weight.W_400;
		} else if (weight < 549) {
			return Weight.W_500;
		} else if (weight < 649) {
			return Weight.W_600;
		} else if (weight < 749) {
			return Weight.W_700;
		} else if (weight < 849) {
			return Weight.W_800;
		}
		return Weight.W_900;
	}

	public static final double toFontSize(Float awtSize, double defaultSize) {
		return awtSize != null ? awtSize.doubleValue() : defaultSize;
	}

	public static final Style toFontStyle(Float awtPosture, Style defaultStyle) {
		if (awtPosture == null) {
			return defaultStyle;
		} else if (awtPosture.equals(TextAttribute.POSTURE_OBLIQUE)) {
			return Style.OBLIQUE;
		}
		return Style.NORMAL;
	}
}
