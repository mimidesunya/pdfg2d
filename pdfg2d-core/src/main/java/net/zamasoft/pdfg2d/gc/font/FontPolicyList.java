package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a list of font policies.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontPolicyList implements Serializable {
	private static final long serialVersionUID = 0;

	/**
	 * Represents a font policy.
	 */
	public enum FontPolicy {
		/**
		 * Use only the Core 14 fonts.
		 */
		CORE,

		/**
		 * Use only CID-Keyed fonts.
		 */
		CID_KEYED,

		/**
		 * Use external fonts (CID-Identity).
		 */
		CID_IDENTITY,

		/**
		 * Use embedded fonts.
		 */
		EMBEDDED,
		/**
		 * Convert text to outlines (paths).
		 */
		OUTLINES;
	}

	public static final FontPolicyList FONT_POLICY_CORE_CID_KEYED_VALUE = new FontPolicyList(
			new FontPolicy[] { FontPolicy.CORE, FontPolicy.CID_KEYED });

	private final FontPolicy[] policies;

	/**
	 * Creates a new FontPolicyList.
	 * 
	 * @param policies the array of font policies
	 */
	public FontPolicyList(final FontPolicy[] policies) {
		assert policies.length > 0;
		this.policies = policies;
	}

	/**
	 * Returns the number of font policies.
	 * 
	 * @return the number of font policies
	 */
	public int getLength() {
		return this.policies.length;
	}

	/**
	 * Returns the font policy at the specified index.
	 * 
	 * @param i the index
	 * @return the font policy
	 */
	public FontPolicy get(final int i) {
		return this.policies[i];
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof FontPolicyList)) {
			return false;
		}
		final var a = (FontPolicyList) o;
		return Arrays.equals(this.policies, a.policies);
	}

	@Override
	public int hashCode() {
		int h = this.policies[0].ordinal();
		for (int i = 1; i < this.policies.length; ++i) {
			h = 31 * h + this.policies[i].ordinal();
		}
		return h;
	}

	@Override
	public String toString() {
		final var buff = new StringBuilder();
		for (int i = 0; i < this.policies.length; ++i) {
			switch (this.policies[i]) {
				case CORE -> buff.append("core ");
				case CID_KEYED -> buff.append("cid-keyed ");
				case CID_IDENTITY -> buff.append("cid-identity ");
				case EMBEDDED -> buff.append("embedded ");
				case OUTLINES -> buff.append("outlines ");
				default -> throw new IllegalStateException();
			}
		}
		return buff.toString();
	}
}
