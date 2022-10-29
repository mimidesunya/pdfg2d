package jp.cssj.resolver.helpers;

import jp.cssj.resolver.SourceValidity;

/**
 * 常にUNKNOWNを返すSourceValidityです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: UnknownSourceValidity.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class UnknownSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	public static final UnknownSourceValidity SHARED_INSTANCE = new UnknownSourceValidity();

	private UnknownSourceValidity() {
		// ignore
	}

	public int getValid() {
		return UNKNOWN;
	}

	public int getValid(SourceValidity validity) {
		return UNKNOWN;
	}
}
