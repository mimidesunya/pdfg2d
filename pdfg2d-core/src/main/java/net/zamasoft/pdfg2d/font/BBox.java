package net.zamasoft.pdfg2d.font;

import java.io.Serializable;

/**
 * Represents a bounding box defined by two points: lower-left and upper-right.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class BBox implements Serializable {
	private static final long serialVersionUID = 0;

	/**
	 * The x coordinate of the lower-left corner.
	 */
	public final short llx;

	/**
	 * The y coordinate of the lower-left corner.
	 */
	public final short lly;

	/**
	 * The x coordinate of the upper-right corner.
	 */
	public final short urx;

	/**
	 * The y coordinate of the upper-right corner.
	 */
	public final short ury;

	/**
	 * Constructs a BBox from AFM format BBox values.
	 * 
	 * @param llx the x coordinate of the lower-left corner
	 * @param lly the y coordinate of the lower-left corner
	 * @param urx the x coordinate of the upper-right corner
	 * @param ury the y coordinate of the upper-right corner
	 */
	public BBox(final short llx, final short lly, final short urx, final short ury) {
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
	}

	@Override
	public String toString() {
		return "[llx=" + this.llx + ",lly=" + this.lly + ",urx=" + this.urx + ",ury=" + this.ury + "]";
	}
}