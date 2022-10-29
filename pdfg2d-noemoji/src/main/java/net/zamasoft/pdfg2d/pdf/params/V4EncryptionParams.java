package net.zamasoft.pdfg2d.pdf.params;

public class V4EncryptionParams extends EncryptionParams {
	public static final short CFM_V2 = 1;
	public static final short CFM_AESV2 = 2;

	private final R4Permissions permissions = new R4Permissions();

	private int length = 128;

	private short cfm = CFM_V2;

	private boolean enctyptMetadata = true;

	public short getType() {
		return TYPE_V4;
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

	public short getCfm() {
		return this.cfm;
	}

	public void setCfm(short cfm) {
		if (cfm != CFM_V2 && cfm != CFM_AESV2) {
			throw new IllegalArgumentException("CFMはCFM_V2またはCFM_AESV2である必要があります。: " + cfm);
		}
		this.cfm = cfm;
	}

	public boolean getEncryptMetadata() {
		return this.enctyptMetadata;
	}

	public void setEncryptMetadata(boolean enctyptMetadata) {
		this.enctyptMetadata = enctyptMetadata;
	}

}
