package net.zamasoft.pdfg2d.pdf.params;

public abstract class EncryptionParams {
	public static final short TYPE_V1 = 1;

	public static final short TYPE_V2 = 2;

	public static final short TYPE_V4 = 4;

	private String userPassword = "", ownerPassword = "";

	public abstract short getType();

	public String getOwnerPassword() {
		return this.ownerPassword;
	}

	public void setOwnerPassword(String ownerPassword) {
		if (ownerPassword == null) {
			ownerPassword = "";
		}
		this.ownerPassword = ownerPassword;
	}

	public String getUserPassword() {
		return this.userPassword;
	}

	public void setUserPassword(String userPassword) {
		if (userPassword == null) {
			userPassword = "";
		}
		this.userPassword = userPassword;
	}

}
