package net.zamasoft.pdfg2d.pdf.params;

public abstract class Permissions {
	public static final short TYPE_R2 = 2;

	public static final short TYPE_R3 = 3;

	public static final short TYPE_R4 = 4;

	protected int flags = 0xFFFFFFFC;

	public abstract short getType();

	public abstract int getFlags();
}
