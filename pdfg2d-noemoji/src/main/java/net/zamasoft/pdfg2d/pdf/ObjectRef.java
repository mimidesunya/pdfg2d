package net.zamasoft.pdfg2d.pdf;

/**
 * オブジェクトリファレンスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ObjectRef.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class ObjectRef {
	public final int objectNumber, generationNumber;

	/**
	 * 
	 * @param objectNumber
	 *            参照のためのオブジェクト番号です。
	 * @param generationNumber
	 *            修正時に使われるジェネレーション番号です。
	 */
	protected ObjectRef(int objectNumber, int generationNumber) {
		this.objectNumber = objectNumber;
		this.generationNumber = generationNumber;
	}

	public boolean equals(Object o) {
		ObjectRef ref = (ObjectRef) o;
		return this.objectNumber == ref.objectNumber && this.generationNumber == ref.generationNumber;
	}
}
