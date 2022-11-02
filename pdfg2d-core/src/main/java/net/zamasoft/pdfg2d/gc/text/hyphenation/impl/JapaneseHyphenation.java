package net.zamasoft.pdfg2d.gc.text.hyphenation.impl;

import java.lang.Character.UnicodeBlock;

import net.zamasoft.pdfg2d.gc.text.hyphenation.Hyphenation;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class JapaneseHyphenation implements Hyphenation {
	private static final CharacterSet ASCII = new BitSetCharacterSet(
			"#$%&*+-/0123456789=@ABCDEFGHIJKLMNOPQRSTUVWXYZ\\^_abcdefghijklmnopqrstuvwxyz|~");

	/**
	 * 半角英数字。
	 */
	private static final CharacterSet LATIN_OR_DIGIT = new CharacterSet() {
		public boolean contains(char c) {
			if (c > 0xFF) {
				return false;
			}
			if (c > 0x7F) {
				return true;
			}
			return ASCII.contains(c);
		}
	};

	/** 行頭禁則和字(SPEC JIS-X4051 8.1.1 + 独自。 */
	private static final String GYOTO_KINSOKU = "～〜ヽヾゝゞ ー ァ ィ ゥ ェ ォ ッ ャ ュ ョ ヮ ヵ ヶ ぁ ぃ ぅ ぇ ぉ っ ゃ ゅ ょ ゎ ゕ ゖ ㇰ ㇱ ㇳ ㇲ ㇳ ㇴ ㇵ ㇶ ㇷ ㇸ ㇹ ㇺ ㇻ ㇼ ㇽ ㇾ ㇿ 々 〻\u3000・”";
	/** 間違って行頭禁則とされてしまう圏点記号を除外。 */
	private static final String GYOTO_KINSOKU_EX = "\uFE45\uFE46";

	/**
	 * 行頭禁則処理。 SPEC JIS-X4051 7.3
	 */
	protected CharacterSet requiresBefore(char c) {
		// 行頭禁則
		if (GYOTO_KINSOKU.indexOf(c) != -1) {
			return CharacterSet.ALL;
		}
		if (GYOTO_KINSOKU_EX.indexOf(c) != -1) {
			return CharacterSet.NOTHING;
		}
		int type = Character.getType(c);

		switch (type) {
		case Character.END_PUNCTUATION:
		case Character.OTHER_PUNCTUATION:
		case Character.MODIFIER_LETTER:
		case Character.MODIFIER_SYMBOL:
			// 終了括弧、区切り記号、修飾文字、修飾記号の前には文字が必要。
			return CharacterSet.ALL;
		}
		return CharacterSet.NOTHING;
	}

	/**
	 * 行末禁則処理。 SPEC JIS-X4051 7.4
	 */
	protected CharacterSet requiresAfter(char c) {
		int type = Character.getType(c);

		if (c <= 0xFF || LATIN_OR_DIGIT.contains(c)) {
			// 半角英数
			switch (type) {
			case Character.START_PUNCTUATION:
				// 開始括弧の後には何らかの文字が必要。
				return CharacterSet.ALL;

			case Character.END_PUNCTUATION:
				// 終了括弧の後には文字が不要。
				return CharacterSet.NOTHING;

			default:
				if (c == '\u0020' || c == '-' || c == '!' || c == '?') {
					// 区切りの後ではラップできる。
					return CharacterSet.NOTHING;
				}
				// 英数字の後には英数字が必要。
				return LATIN_OR_DIGIT;
			}
		} else {
			// その他の文字
			switch (type) {
			case Character.START_PUNCTUATION:
				// 開始括弧の後には何らかの文字が必要。
				return CharacterSet.ALL;
			}
			// ダッシュ等
			if (c == '─' || c == '“') {
				return CharacterSet.ALL;
			}
		}
		return CharacterSet.NOTHING;
	}

	public boolean atomic(char c1, char c2) {
		return this.requiresAfter(c1).contains(c2) || this.requiresBefore(c2).contains(c1);
	}

	public boolean canSeparate(char c1, char c2) {
		if (Character.isWhitespace(c1) || Character.isWhitespace(c2)) {
			return c1 != '\u3000';
		}
		if (this.isCJK(c1) || this.isCJK(c2)) {
			return true;
		}
		return false;
	}

	protected boolean isCJK(char c) {
		UnicodeBlock b = UnicodeBlock.of(c);
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
