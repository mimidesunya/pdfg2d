package net.zamasoft.pdfg2d.pdf.params;

public abstract class Permissions {
	public static enum Type {
		R2(2), R3(3), R4(4);

		public final int r;

		private Type(int r) {
			this.r = r;
		}
	}

	protected int flags = 0xFFFFFFFC;

	public abstract Type getType();

	public abstract int getFlags();
}
