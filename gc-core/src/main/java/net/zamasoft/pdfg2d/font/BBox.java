package net.zamasoft.pdfg2d.font;

import java.io.Serializable;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class BBox implements Serializable {
	private static final long serialVersionUID = 0;

	public final short llx, lly, urx, ury;

	/**
	 * AFM形式のBBoxからBBoxを構築します。
	 * 
	 * @param llx
	 * @param lly
	 * @param urx
	 * @param ury
	 */
	public BBox(short llx, short lly, short urx, short ury) {
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
	}

	public String toString() {
		return "[llx=" + this.llx + ",lly=" + this.lly + ",urx=" + this.urx + ",ury=" + this.ury + "]";
	}
}