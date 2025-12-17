package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a list of font policies.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record FontPolicyList(FontPolicy[] policies) implements Serializable {

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

	public FontPolicyList {
		assert policies.length > 0;
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
		return o instanceof FontPolicyList a && Arrays.equals(this.policies, a.policies);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.policies);
	}

	@Override
	public String toString() {
		return Arrays.stream(this.policies)
				.map(p -> switch (p) {
					case CORE -> "core";
					case CID_KEYED -> "cid-keyed";
					case CID_IDENTITY -> "cid-identity";
					case EMBEDDED -> "embedded";
					case OUTLINES -> "outlines";
				})
				.collect(java.util.stream.Collectors.joining(" "));
	}
}
