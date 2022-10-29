package net.zamasoft.pdfg2d.pdf.impl;

import jp.cssj.rsr.RandomBuilder.PositionInfo;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

class ObjectRefImpl extends ObjectRef {
	private int position = -1, id;

	// private StringWriter t = new StringWriter();

	public ObjectRefImpl(int objectNum) {
		super(objectNum, 0);
		// new Exception().printStackTrace(new PrintWriter(t));
	}

	public void setPosition(int id, int position) {
		if (this.position != -1) {
			throw new IllegalStateException("同じリファレンスで2度オブジェクトを作成しようとしました。");
		}
		this.id = id;
		this.position = position;
	}

	public long getPosition(PositionInfo info) {
		return info.getPosition(this.id) + this.position;
	}

	public String toString() {
		return "R " + this.objectNumber + " " + this.generationNumber;// +"/"+this
		// .t;
	}
}