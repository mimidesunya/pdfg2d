package net.zamasoft.pdfg2d.pdf;

/**
 * オブジェクトリファレンスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ObjectRef {
	public final int objectNumber, generationNumber;

	/**
	 * 
	 * @param objectNumber     参照のためのオブジェクト番号です。
	 * @param generationNumber 修正時に使われるジェネレーション番号です。
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
