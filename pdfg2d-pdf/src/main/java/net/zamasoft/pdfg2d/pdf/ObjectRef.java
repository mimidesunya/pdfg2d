package net.zamasoft.pdfg2d.pdf;

/**
 * Represents a PDF object reference.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ObjectRef {
	public final int objectNumber, generationNumber;

	/**
	 * Creates a new PDF object reference.
	 * 
	 * @param objectNumber     the object number for referencing
	 * @param generationNumber the generation number used during modifications
	 */
	protected ObjectRef(int objectNumber, int generationNumber) {
		this.objectNumber = objectNumber;
		this.generationNumber = generationNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ObjectRef ref))
			return false;
		return this.objectNumber == ref.objectNumber && this.generationNumber == ref.generationNumber;
	}

	@Override
	public int hashCode() {
		return 31 * objectNumber + generationNumber;
	}
}
