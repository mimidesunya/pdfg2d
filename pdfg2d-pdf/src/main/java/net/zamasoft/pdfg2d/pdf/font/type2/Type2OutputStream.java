package net.zamasoft.pdfg2d.pdf.font.type2;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Outputs Type 2 font data.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class Type2OutputStream extends FilterOutputStream {
	public static final byte[] HSTEM = { 1 };

	public static final byte[] VSTEM = { 3 };

	public static final byte[] VMOVETO = { 4 };

	public static final byte[] RLINETO = { 5 };

	public static final byte[] HLINETO = { 6 };

	public static final byte[] VLINETO = { 7 };

	public static final byte[] RRCURVETO = { 8 };

	public static final byte[] CALLSUBR = { 10 };

	public static final byte[] RETURN = { 11 };

	public static final byte[] ENDCHAR = { 14 };

	public static final byte[] HSTEMHM = { 18 };

	public static final byte[] HINTMASK = { 19 };

	public static final byte[] CNTRMASK = { 20 };

	public static final byte[] RMOVETO = { 21 };

	public static final byte[] HMOVETO = { 22 };

	public static final byte[] VSTEMHM = { 23 };

	public static final byte[] RCURVELINE = { 24 };

	public static final byte[] RLINECURVE = { 25 };

	public static final byte[] VVCURVETO = { 26 };

	public static final byte[] HCURVETO = { 27 };

	public static final byte[] HHCURVETO = { 28 };

	public static final byte[] CALLGSUBR = { 29 };

	public static final byte[] VHCURVETO = { 30 };

	public static final byte[] HVCURVETO = { 31 };

	public static final byte[] AND = { 12, 3 };

	public static final byte[] OR = { 12, 4 };

	public static final byte[] NOT = { 12, 5 };

	public static final byte[] ABS = { 12, 9 };

	public static final byte[] ADD = { 12, 10 };

	public static final byte[] SUB = { 12, 11 };

	public static final byte[] DIV = { 12, 12 };

	public static final byte[] NEG = { 12, 14 };

	public static final byte[] EQ = { 12, 15 };

	public static final byte[] DROP = { 12, 18 };

	public static final byte[] PUT = { 12, 20 };

	public static final byte[] GET = { 12, 21 };

	public static final byte[] IFELSE = { 12, 22 };

	public static final byte[] RANDOM = { 12, 23 };

	public static final byte[] MUL = { 12, 24 };

	public static final byte[] SQRT = { 12, 26 };

	public static final byte[] DUP = { 12, 27 };

	public static final byte[] EXCH = { 12, 28 };

	public static final byte[] INDEX = { 12, 29 };

	public static final byte[] ROLL = { 12, 30 };

	public static final byte[] HFLEX = { 12, 34 };

	public static final byte[] FLEX = { 12, 35 };

	public static final byte[] HFLEX1 = { 12, 36 };

	public static final byte[] FLEX1 = { 12, 37 };

	public Type2OutputStream(OutputStream out) {
		super(out);
	}

	public int writeOperator(byte[] o) throws IOException {
		this.write(o);
		return o.length;
	}

	public int writeShort(short a) throws IOException {
		if (a >= -107 && a <= 107) {
			this.write(a + 139);
			return 1;
		} else if (a >= 108 && a <= 1131) {
			a -= 108;
			this.write((a >> 8) + 247);
			this.write(a);
			return 2;
		} else if (a >= -1131 && a <= -108) {
			a += 108;
			this.write((-a >> 8) + 251);
			this.write(-a);
			return 2;
		} else {
			this.write(28);
			this.write(a >> 8);
			this.write(a);
			return 3;
		}
	}

	public void writeFixed(double f) throws IOException {
		if (f > Short.MAX_VALUE || f < Short.MIN_VALUE) {
			throw new IllegalArgumentException();
		}
		this.write(255);

		short a = (short) f;
		this.write(a >> 8);
		this.write(a);

		f -= (double) a;
		int b = (int) (f * 0xFFFFf);
		this.write(b >> 8);
		this.write(b);
	}
}
