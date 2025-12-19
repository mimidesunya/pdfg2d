package net.zamasoft.pdfg2d.pdf.params;

/**
 * Base class for encryption parameters.
 */
public abstract class EncryptionParams {
	public enum Type {
		V1(1), V2(2), V4(4);

		public final int v;

		Type(final int v) {
			this.v = v;
		}
	}

	private String userPassword = "";
	private String ownerPassword = "";

	public abstract Type getType();

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
