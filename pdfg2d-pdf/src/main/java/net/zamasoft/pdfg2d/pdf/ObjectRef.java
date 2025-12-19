package net.zamasoft.pdfg2d.pdf;

/**
 * Represents a PDF object reference.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class ObjectRef {
	private final int objectNumber;
	private final int generationNumber;

	/**
	 * Creates a new PDF object reference.
	 * 
	 * @param objectNumber     the object number for referencing
	 * @param generationNumber the generation number used during modifications
	 */
	protected ObjectRef(final int objectNumber, final int generationNumber) {
		this.objectNumber = objectNumber;
		this.generationNumber = generationNumber;
	}

	/**
	 * Returns the object number.
	 * 
	 * @return the object number
	 */
	public final int objectNumber() {
		return this.objectNumber;
	}

	/**
	 * Returns the generation number.
	 * 
	 * @return the generation number
	 */
	public final int generationNumber() {
		return this.generationNumber;
	}

	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ObjectRef ref)) {
			return false;
		}
		return this.objectNumber == ref.objectNumber && this.generationNumber == ref.generationNumber;
	}

	@Override
	public final int hashCode() {
		return 31 * this.objectNumber + this.generationNumber;
	}
}
