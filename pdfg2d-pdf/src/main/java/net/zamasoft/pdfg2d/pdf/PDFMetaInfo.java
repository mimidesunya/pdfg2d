package net.zamasoft.pdfg2d.pdf;

/**
 * Metadata information for a PDF document.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class PDFMetaInfo {
	private String author;
	private String producer;
	private String creator;
	private String title;
	private String subject;
	private String keywords;
	private long creationDate = -1L;
	private long modDate = -1L;

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * @param author The author to set.
	 */
	public void setAuthor(final String author) {
		this.author = author;
	}

	/**
	 * @return Returns the creator.
	 */
	public String getCreator() {
		return this.creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(final String creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the keywords.
	 */
	public String getKeywords() {
		return this.keywords;
	}

	/**
	 * @param keywords The keywords to set.
	 */
	public void setKeywords(final String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return Returns the producer.
	 */
	public String getProducer() {
		return this.producer;
	}

	/**
	 * @param producer The producer to set.
	 */
	public void setProducer(final String producer) {
		this.producer = producer;
	}

	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return this.subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return The creation date as a timestamp, or -1 if not set.
	 */
	public long getCreationDate() {
		return this.creationDate;
	}

	/**
	 * @param creationDate The creation date timestamp to set.
	 */
	public void setCreationDate(final long creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return The modification date as a timestamp, or -1 if not set.
	 */
	public long getModDate() {
		return this.modDate;
	}

	/**
	 * @param modDate The modification date timestamp to set.
	 */
	public void setModDate(final long modDate) {
		this.modDate = modDate;
	}
}
