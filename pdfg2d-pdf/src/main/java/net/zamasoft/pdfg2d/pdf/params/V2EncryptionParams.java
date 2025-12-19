package net.zamasoft.pdfg2d.pdf.params;

/**
 * Parameters for V2 encryption (PDF 1.4).
 */
public final class V2EncryptionParams extends EncryptionParams {
	private final R3Permissions permissions = new R3Permissions();

	private int length = 128;

	@Override
	public Type getType() {
		return Type.V2;
	}

	public R3Permissions getPermissions() {
		return this.permissions;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(final int length) throws IllegalArgumentException {
		if (length < 40 || length > 128 || (length % 8) != 0) {
			throw new IllegalArgumentException(
					"V2 encryption length must be between 40 and 128 bits, in increments of 8: " + length);
		}
		this.length = length;
	}
}
