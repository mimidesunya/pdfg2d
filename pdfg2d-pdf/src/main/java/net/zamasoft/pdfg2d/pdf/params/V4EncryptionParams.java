package net.zamasoft.pdfg2d.pdf.params;

/**
 * Parameters for V4 encryption (PDF 1.5/1.6).
 */
public final class V4EncryptionParams extends EncryptionParams {
	public enum CFM {
		V2("V2"), AESV2("AESV2");

		public final String name;

		CFM(final String name) {
			this.name = name;
		}
	}

	private final R4Permissions permissions = new R4Permissions();

	private int length = 128;

	private CFM cfm = CFM.V2;

	private boolean encryptMetadata = true;

	@Override
	public Type getType() {
		return Type.V4;
	}

	public R4Permissions getPermissions() {
		return this.permissions;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(final int length) throws IllegalArgumentException {
		if (length < 40 || length > 128 || (length % 8) != 0) {
			throw new IllegalArgumentException(
					"V4 encryption length must be between 40 and 128 bits, in increments of 8: " + length);
		}
		this.length = length;
	}

	public CFM getCFM() {
		return this.cfm;
	}

	public void setCFM(final CFM cfm) {
		this.cfm = cfm;
	}

	public boolean getEncryptMetadata() {
		return this.encryptMetadata;
	}

	public void setEncryptMetadata(final boolean encryptMetadata) {
		this.encryptMetadata = encryptMetadata;
	}

}
