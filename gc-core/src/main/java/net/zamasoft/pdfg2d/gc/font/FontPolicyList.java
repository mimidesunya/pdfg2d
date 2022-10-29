package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;
import java.util.Arrays;

public class FontPolicyList implements Serializable {
	private static final long serialVersionUID = 0;

	/**
	 * コア14フォントだけを使います。
	 */
	public static final byte FONT_POLICY_CORE = 0;

	/**
	 * CID-Keyedフォントだけを使います。
	 */
	public static final byte FONT_POLICY_CID_KEYED = 1;

	/**
	 * 外部フォントを使います。
	 */
	public static final byte FONT_POLICY_CID_IDENTITY = 2;

	/**
	 * 埋め込みフォントを使います。
	 */
	public static final byte FONT_POLICY_EMBEDDED = 3;

	/**
	 * パスに変換して描画します。
	 */
	public static final byte FONT_POLICY_OUTLINES = 4;

	public static final FontPolicyList FONT_POLICY_CORE_CID_KEYED_VALUE = new FontPolicyList(
			new byte[] { FONT_POLICY_CORE, FONT_POLICY_CID_KEYED });

	private final byte[] policies;

	public FontPolicyList(byte[] policies) {
		assert policies.length > 0;
		this.policies = policies;
	}

	public int getLength() {
		return this.policies.length;
	}

	public byte get(int i) {
		return this.policies[i];
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FontPolicyList)) {
			return false;
		}
		FontPolicyList a = (FontPolicyList) o;
		return Arrays.equals(this.policies, a.policies);
	}

	public int hashCode() {
		int h = this.policies[0];
		for (int i = 1; i < this.policies.length; ++i) {
			h = 31 * h + this.policies[i];
		}
		return h;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < this.policies.length; ++i) {
			switch (this.policies[i]) {
			case FONT_POLICY_CORE:
				buff.append("core ");
				break;
			case FONT_POLICY_CID_KEYED:
				buff.append("cid-keyed ");
				break;
			case FONT_POLICY_CID_IDENTITY:
				buff.append("cid-identity ");
				break;
			case FONT_POLICY_EMBEDDED:
				buff.append("embedded ");
				break;
			case FONT_POLICY_OUTLINES:
				buff.append("outlines ");
				break;
			default:
				throw new IllegalStateException();
			}
		}
		return buff.toString();
	}
}
