package net.zamasoft.pdfg2d.pdf;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class PDFMetaInfo {
	private String author = null, producer = null, creator = null, title = null, subject = null, keywords = null;

	private long creationDate = -1L, modDate = -1L;

	/**
	 * @return Returns the author.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author The author to set.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return Returns the creator.
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the keywords.
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords The keywords to set.
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return Returns the producer.
	 */
	public String getProducer() {
		return producer;
	}

	/**
	 * @param producer The producer to set.
	 */
	public void setProducer(String producer) {
		this.producer = producer;
	}

	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public long getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public long getModDate() {
		return this.modDate;
	}

	public void setModDate(long modDate) {
		this.modDate = modDate;
	}
}
