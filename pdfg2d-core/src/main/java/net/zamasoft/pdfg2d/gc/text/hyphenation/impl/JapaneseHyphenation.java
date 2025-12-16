package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import java.lang.Character.UnicodeBlock;

import net.zamasoft.pdfg2d.gc.text.hyphenation.Hyphenation;

/**
 * Japanese hyphenation rules.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class JapaneseHyphenation implements Hyphenation {
	private static final CharacterSet ASCII = new BitSetCharacterSet(
			"#$%&*+-/0123456789=@ABCDEFGHIJKLMNOPQRSTUVWXYZ\\^_abcdefghijklmnopqrstuvwxyz|~");

	/**
	 * Half-width alphanumeric characters.
	 */
	private static final CharacterSet LATIN_OR_DIGIT = c -> {
		if (c > 0xFF) {
			return false;
		}
		if (c > 0x7F) {
			return true;
		}
		return ASCII.contains(c);
	};

	/**
	 * Prohibited characters at the start of a line (SPEC JIS-X4051 8.1.1 + custom).
	 */
	private static final String GYOTO_KINSOKU = "～〜ヽヾゝゞ ー ァ ィ ゥ ェ ォ ッ ャ ュ ョ ヮ ヵ ヶ ぁ ぃ ぅ ぇ ぉ っ ゃ ゅ ょ ゎ ゕ ゖ ㇰ ㇱ ㇳ ㇲ ㇳ ㇴ ㇵ ㇶ ㇷ ㇸ ㇹ ㇺ ㇻ ㇼ ㇽ ㇾ ㇿ 々 〻\u3000・”";

	/**
	 * Exclude emphatic dots that are mistakenly treated as line-start prohibited.
	 */
	private static final String GYOTO_KINSOKU_EX = "\uFE45\uFE46";

	/**
	 * Line-start prohibition processing. SPEC JIS-X4051 7.3
	 * 
	 * @param c the character to check
	 * @return the character set required before the character
	 */
	protected CharacterSet requiresBefore(final char c) {
		// Line-start prohibition
		if (GYOTO_KINSOKU.indexOf(c) != -1) {
			return CharacterSet.ALL;
		}
		if (GYOTO_KINSOKU_EX.indexOf(c) != -1) {
			return CharacterSet.NOTHING;
		}
		final int type = Character.getType(c);

		return switch (type) {
			case Character.END_PUNCTUATION, Character.OTHER_PUNCTUATION, Character.MODIFIER_LETTER,
					Character.MODIFIER_SYMBOL ->
				// Characters required before closing parenthesis, delimiters, modifier letters,
				// or modifier symbols.
				CharacterSet.ALL;
			default -> CharacterSet.NOTHING;
		};
	}

	/**
	 * Line-end prohibition processing. SPEC JIS-X4051 7.4
	 * 
	 * @param c the character to check
	 * @return the character set required after the character
	 */
	protected CharacterSet requiresAfter(final char c) {
		final int type = Character.getType(c);

		if (c <= 0xFF || LATIN_OR_DIGIT.contains(c)) {
			// Half-width alphanumeric
			switch (type) {
				case Character.START_PUNCTUATION:
					// Some character is required after an opening parenthesis.
					return CharacterSet.ALL;

				case Character.END_PUNCTUATION:
					// No character is required after a closing parenthesis.
					return CharacterSet.NOTHING;

				default:
					if (c == '\u0020' || c == '-' || c == '!' || c == '?') {
						// Can wrap after a delimiter.
						return CharacterSet.NOTHING;
					}
					// Alphanumeric characters require alphanumeric characters after them.
					return LATIN_OR_DIGIT;
			}
		} else {
			// Other characters
			switch (type) {
				case Character.START_PUNCTUATION:
					// Some character is required after an opening parenthesis.
					return CharacterSet.ALL;
			}
			// Dash, etc.
			if (c == '─' || c == '“') {
				return CharacterSet.ALL;
			}
		}
		return CharacterSet.NOTHING;
	}

	@Override
	public boolean atomic(final char c1, final char c2) {
		return this.requiresAfter(c1).contains(c2) || this.requiresBefore(c2).contains(c1);
	}

	@Override
	public boolean canSeparate(final char c1, final char c2) {
		if (Character.isWhitespace(c1) || Character.isWhitespace(c2)) {
			return c1 != '\u3000';
		}
		if (this.isCJK(c1) || this.isCJK(c2)) {
			return true;
		}
		return false;
	}

	protected boolean isCJK(final char c) {
		final UnicodeBlock b = UnicodeBlock.of(c);
		if (b == null) {
			return true;
		}
		if (b == UnicodeBlock.CJK_COMPATIBILITY) {
			return true;
		}
		if (b == UnicodeBlock.CJK_COMPATIBILITY_FORMS) {
			return true;
		}
		if (b == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
			return true;
		}
		if (b == UnicodeBlock.CJK_RADICALS_SUPPLEMENT) {
			return true;
		}
		if (b == UnicodeBlock.CJK_STROKES) {
			return true;
		}
		if (b == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION) {
			return true;
		}
		if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
			return true;
		}
		if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
			return true;
		}
		if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) {
			return true;
		}
		if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C) {
			return true;
		}
		if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D) {
			return true;
		}
		if (b == UnicodeBlock.HIRAGANA) {
			return true;
		}
		if (b == UnicodeBlock.KATAKANA) {
			return true;
		}
		if (b == UnicodeBlock.KANBUN) {
			return true;
		}
		if (b == UnicodeBlock.HANGUL_SYLLABLES) {
			return true;
		}
		if (b == UnicodeBlock.HANGUL_JAMO) {
			return true;
		}
		if (b == UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
			return true;
		}
		if (b == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
}
