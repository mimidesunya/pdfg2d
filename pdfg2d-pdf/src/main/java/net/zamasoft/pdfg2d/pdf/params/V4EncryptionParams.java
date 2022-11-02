package net.zamasoft.pdfg2d.pdf.params;

public class V4EncryptionParams extends EncryptionParams {
	public enum CFM {
		V2("V2"), AESV2("AESV2");

		public final String name;

		private CFM(String name) {
			this.name = name;
		}
	}

	private final R4Permissions permissions = new R4Permissions();

	private int length = 128;

	private CFM cfm = CFM.V2;

	private boolean enctyptMetadata = true;

	public Type getType() {
		return Type.V4;
	}

	public R4Permissions getPermissions() {
		return this.permissions;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) throws IllegalArgumentException {
		if (length < 40 || length > 128 || (length % 8) != 0) {
			throw new IllegalArgumentException("V2暗号化の長さは40-128ビットの範囲で8刻みです。: " + length);
		}
		this.length = length;
	}

	public CFM getCFM() {
		return this.cfm;
	}

	public void setCFM(CFM cfm) {
		this.cfm = cfm;
	}

	public boolean getEncryptMetadata() {
		return this.enctyptMetadata;
	}

	public void setEncryptMetadata(boolean enctyptMetadata) {
		this.enctyptMetadata = enctyptMetadata;
	}

}
