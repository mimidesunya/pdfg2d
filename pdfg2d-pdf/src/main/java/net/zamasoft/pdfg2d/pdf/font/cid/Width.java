package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.Serializable;

public class Width implements Serializable {
	private static final long serialVersionUID = 0;

	/** 最初のコードと最後のコードです。 */
	int firstCode, lastCode;

	/** 幅のリストです。 */
	short[] widths;

	/**
	 * ある範囲の文字に対するエントリを構築します。
	 * 
	 * @param firstCode 最初の文字のコード。
	 * @param lastCode  最後の文字のコード。
	 * @param widths    文字の幅のリスト。
	 */
	public Width(int firstCode, int lastCode, short[] widths) {
		this.firstCode = firstCode;
		this.lastCode = lastCode;
		this.widths = widths;
	}

	public Width(int code, short[] widths) {
		this(code, code, widths);
	}

	public Width(short[] widths) {
		this(0, 0, widths);
	}

	public int getFirstCode() {
		return this.firstCode;
	}

	public int getLastCode() {
		return this.lastCode;
	}

	public short[] getWidths() {
		return this.widths;
	}

	public short getWidth(int code) {
		assert (code >= this.firstCode && code <= this.lastCode);
		int index = code - this.firstCode;
		if (index >= this.widths.length) {
			return this.widths[this.widths.length - 1];
		}
		return this.widths[index];
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.firstCode).append(' ');
		buff.append(this.lastCode);
		for (int i = 0; i < this.widths.length; ++i) {
			buff.append(' ');
			buff.append(this.widths[i]);
		}
		return buff.toString();
	}
}
