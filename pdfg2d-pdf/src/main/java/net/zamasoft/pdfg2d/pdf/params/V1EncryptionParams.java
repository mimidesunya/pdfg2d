package net.zamasoft.pdfg2d.pdf.params;

public class V1EncryptionParams extends EncryptionParams {
	private final R2Permissions permissions = new R2Permissions();

	public Type getType() {
		return Type.V1;
	}

	public R2Permissions getPermissions() {
		return this.permissions;
	}
}
