package net.zamasoft.pdfg2d.pdf.params;

/**
 * Base class for PDF permissions.
 */
public abstract class Permissions {
	public enum Type {
		R2(2), R3(3), R4(4);

		public final int r;

		Type(final int r) {
			this.r = r;
		}
	}

	protected int flags = 0xFFFFFFFC;

	public abstract Type getType();

	public abstract int getFlags();
}
