package net.zamasoft.pdfg2d.pdf.params;

/**
 * Permissions for Revision 3 (PDF 1.4).
 */
public class R3Permissions extends R2Permissions {
	public static final int FILL = 1 << 8;

	public static final int EXTRACT = 1 << 9;

	public static final int ASSEMBLE = 1 << 10;

	public static final int PRINT_HIGH = 1 << 11;

	@Override
	public Type getType() {
		return Type.R3;
	}

	@Override
	public int getFlags() {
		return this.flags;
	}

	public boolean isAssemble() {
		return (this.flags & ASSEMBLE) != 0;
	}

	public void setAssemble(final boolean assemble) {
		if (assemble) {
			this.flags |= ASSEMBLE;
		} else {
			this.flags &= ~ASSEMBLE;
		}
	}

	public boolean isExtract() {
		return (this.flags & EXTRACT) != 0;
	}

	public void setExtract(final boolean extract) {
		if (extract) {
			this.flags |= EXTRACT;
		} else {
			this.flags &= ~EXTRACT;
		}
	}

	public boolean isFill() {
		return (this.flags & FILL) != 0;
	}

	public void setFill(final boolean fill) {
		if (fill) {
			this.flags |= FILL;
		} else {
			this.flags &= ~FILL;
		}
	}

	public boolean isPrintHigh() {
		return (this.flags & PRINT_HIGH) != 0;
	}

	public void setPrintHigh(final boolean printHigh) {
		if (printHigh) {
			this.flags |= PRINT_HIGH;
		} else {
			this.flags &= ~PRINT_HIGH;
		}
	}
}
