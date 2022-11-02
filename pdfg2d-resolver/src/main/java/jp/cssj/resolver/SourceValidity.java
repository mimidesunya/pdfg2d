package jp.cssj.resolver;

import java.io.Serializable;

/**
 * データの更新情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface SourceValidity extends Serializable {
	/** データが更新されている。 */
	public static final int INVALID = -1;
	/** データが更新されているかどうか検証できない。 */
	public static final int UNKNOWN = 0;
	/** データが更新されていない。 */
	public static final int VALID = 1;

	/**
	 * このSourceValidityを取得後にデータが更新されたかどうかを返します。
	 * 
	 * @return INVALID, UNKNOWN, VALID定数のいずれか。
	 */
	public int getValid();

	/**
	 * 与えられたSourceValidityとこのSourceValidityが異なるかどうか検証します。
	 * 
	 * @param validity
	 *            別に取得したSourceValidity。
	 * @return INVALID, UNKNOWN, VALID定数のいずれか。
	 */
	public int getValid(SourceValidity validity);
}
