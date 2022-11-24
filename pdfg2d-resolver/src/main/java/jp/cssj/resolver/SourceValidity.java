package jp.cssj.resolver;

import java.io.Serializable;

/**
 * データの更新情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface SourceValidity extends Serializable {
	public static enum Validity {
	/** データが更新されている。 */
	INVALID,
	/** データが更新されているかどうか検証できない。 */
	UNKNOWN,
	/** データが更新されていない。 */
	VALID;
	}

	/**
	 * このSourceValidityを取得後にデータが更新されたかどうかを返します。
	 * 
	 * @return INVALID, UNKNOWN, VALID定数のいずれか。
	 */
	public Validity getValid();

	/**
	 * 与えられたSourceValidityとこのSourceValidityが異なるかどうか検証します。
	 * 
	 * @param validity
	 *            別に取得したSourceValidity。
	 * @return INVALID, UNKNOWN, VALID定数のいずれか。
	 */
	public Validity getValid(SourceValidity validity);
}
