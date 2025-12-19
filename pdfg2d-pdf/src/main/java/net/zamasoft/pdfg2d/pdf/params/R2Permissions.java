package net.zamasoft.pdfg2d.pdf.params;

/**
 * Permissions for Revision 2 (PDF 1.1).
 */
public class R2Permissions extends Permissions {
	public static final int PRINT = 1 << 2;

	public static final int MODIFY = 1 << 3;

	public static final int COPY = 1 << 4;

	public static final int ADD = 1 << 5;

	@Override
	public Type getType() {
		return Type.R2;
	}

	@Override
	public int getFlags() {
		return this.flags;
	}

	public boolean isAdd() {
		return (this.flags & ADD) != 0;
	}

	public void setAdd(final boolean add) {
		if (add) {
			this.flags |= ADD;
		} else {
			this.flags &= ~ADD;
		}
	}

	public boolean isCopy() {
		return (this.flags & COPY) != 0;
	}

	public void setCopy(final boolean copy) {
		if (copy) {
			this.flags |= COPY;
		} else {
			this.flags &= ~COPY;
		}
	}

	public boolean isModify() {
		return (this.flags & MODIFY) != 0;
	}

	public void setModify(final boolean modify) {
		if (modify) {
			this.flags |= MODIFY;
		} else {
			this.flags &= ~MODIFY;
		}
	}

	public boolean isPrint() {
		return (this.flags & PRINT) != 0;
	}

	public void setPrint(final boolean print) {
		if (print) {
			this.flags |= PRINT;
		} else {
			this.flags &= ~PRINT;
		}
	}
}
