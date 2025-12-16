package net.zamasoft.pdfg2d.gc.text.util;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;

import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;

/**
 * Utility class for text processing.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
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

	/**
	 * Attribute for writing mode.
	 */
	public static final Attribute WRITING_MODE = new WritingMode();

	/**
	 * Converts AWT font family name to {@link FontFamilyList}.
	 * 
	 * @param awtFamily     the AWT font family name
	 * @param defaultFamily the default family to return if awtFamily is null
	 * @return the FontFamilyList
	 */
	public static FontFamilyList toFontFamilyList(final String awtFamily, final FontFamilyList defaultFamily) {
		if (awtFamily == null) {
			return defaultFamily;
		}
		return FontFamilyList.create(awtFamily);
	}

	/**
	 * Converts AWT font weight to {@link Weight}.
	 * 
	 * @param awtWeight     the AWT font weight
	 * @param defaultWeight the default weight to return if awtWeight is null
	 * @return the Weight
	 */
	public static Weight toFontWeight(final Float awtWeight, final Weight defaultWeight) {
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

	/**
	 * Decodes numeric weight value to {@link Weight}.
	 * 
	 * @param weight the numeric weight
	 * @return the Weight
	 */
	public static Weight decodeFontWeight(final short weight) {
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

	/**
	 * Converts AWT font size to double.
	 * 
	 * @param awtSize     the AWT font size
	 * @param defaultSize the default size to return if awtSize is null
	 * @return the font size
	 */
	public static double toFontSize(final Float awtSize, final double defaultSize) {
		return awtSize != null ? awtSize.doubleValue() : defaultSize;
	}

	/**
	 * Converts AWT posture to {@link Style}.
	 * 
	 * @param awtPosture   the AWT posture
	 * @param defaultStyle the default style to return if awtPosture is null
	 * @return the Style
	 */
	public static Style toFontStyle(final Float awtPosture, final Style defaultStyle) {
		if (awtPosture == null) {
			return defaultStyle;
		} else if (awtPosture.equals(TextAttribute.POSTURE_OBLIQUE)) {
			return Style.OBLIQUE;
		}
		return Style.NORMAL;
	}
}
