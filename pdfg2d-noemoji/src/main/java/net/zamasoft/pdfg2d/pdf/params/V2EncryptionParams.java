package net.zamasoft.pdfg2d.pdf.params;

public class V2EncryptionParams extends EncryptionParams {
	private final R3Permissions permissions = new R3Permissions();

	private int length = 128;

	public short getType() {
		return TYPE_V2;
	}

	public R3Permissions getPermissions() {
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
}
