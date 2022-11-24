package jp.cssj.resolver.helpers;

import jp.cssj.resolver.SourceValidity;

/**
 * 常にVALIDを返すSourceValidityです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ValidSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	public static final ValidSourceValidity SHARED_INSTANCE = new ValidSourceValidity();

	private ValidSourceValidity() {
		// ignore
	}

	public Validity getValid() {
		return Validity.VALID;
	}

	public Validity getValid(SourceValidity validity) {
		return Validity.VALID;
	}

}
