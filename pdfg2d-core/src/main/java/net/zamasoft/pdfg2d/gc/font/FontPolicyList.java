package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;
import java.util.Arrays;

public class FontPolicyList implements Serializable {
	private static final long serialVersionUID = 0;

	public static enum FontPolicy {
		/**
		 * コア14フォントだけを使います。
		 */
		CORE,

		/**
		 * CID-Keyedフォントだけを使います。
		 */
		CID_KEYED ,

		/**
		 * 外部フォントを使います。
		 */
		CID_IDENTITY,

		/**
		 * 埋め込みフォントを使います。
		 */
		EMBEDDED,
		/**
		 * パスに変換して描画します。
		 */
		OUTLINES;
	}

	public static final FontPolicyList FONT_POLICY_CORE_CID_KEYED_VALUE = new FontPolicyList(
			new FontPolicy[] { FontPolicy.CORE, FontPolicy.CID_KEYED });

	private final FontPolicy[] policies;

	public FontPolicyList(FontPolicy[] policies) {
		assert policies.length > 0;
		this.policies = policies;
	}

	public int getLength() {
		return this.policies.length;
	}

	public FontPolicy get(int i) {
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
		int h = this.policies[0].ordinal();
		for (int i = 1; i < this.policies.length; ++i) {
			h = 31 * h + this.policies[i].ordinal();
		}
		return h;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < this.policies.length; ++i) {
			switch (this.policies[i]) {
			case CORE:
				buff.append("core ");
				break;
			case CID_KEYED:
				buff.append("cid-keyed ");
				break;
			case CID_IDENTITY:
				buff.append("cid-identity ");
				break;
			case EMBEDDED:
				buff.append("embedded ");
				break;
			case OUTLINES:
				buff.append("outlines ");
				break;
			default:
				throw new IllegalStateException();
			}
		}
		return buff.toString();
	}
}
