package jp.cssj.resolver.helpers;

import jp.cssj.resolver.SourceValidity;

/**
 * 常にUNKNOWNを返すSourceValidityです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class UnknownSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	public static final UnknownSourceValidity SHARED_INSTANCE = new UnknownSourceValidity();

	private UnknownSourceValidity() {
		// ignore
	}

	public Validity getValid() {
		return Validity.UNKNOWN;
	}

	public Validity getValid(SourceValidity validity) {
		return Validity.UNKNOWN;
	}
}
