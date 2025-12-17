package net.zamasoft.pdfg2d.font;

import java.io.Serializable;

/**
 * Represents a bounding box defined by two points: lower-left and upper-right.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public record BBox(short llx, short lly, short urx, short ury) implements Serializable {

	@Override
	public String toString() {
		return "[llx=" + this.llx + ",lly=" + this.lly + ",urx=" + this.urx + ",ury=" + this.ury + "]";
	}
}