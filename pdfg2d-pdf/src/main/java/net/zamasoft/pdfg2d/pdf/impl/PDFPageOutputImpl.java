package net.zamasoft.pdfg2d.pdf.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;

import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.annot.Annot;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Implementation of a PDF page output. This class manages the page content
 * stream,
 * annotations, and page-level metadata like boxes (MediaBox, CropBox, etc.).
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class PDFPageOutputImpl extends PDFPageOutput {
	private final PDFFragmentOutputImpl pageFlow;

	/** Current page object. */
	private final ObjectRef pageRef;

	/** Parameters flow for current page. */
	private final PDFFragmentOutputImpl paramsFlow;

	/** Annotations flow for current page. */
	private final PDFFragmentOutputImpl annotsFlow;

	/** Does current page have annotations? */
	private boolean hasAnnots = false;

	private Rectangle2D mediaBox, cropBox, bleedBox, trimBox, artBox;

	public PDFPageOutputImpl(final PDFWriterImpl pdfWriter, final ObjectRef rootPageRef,
			final PDFFragmentOutputImpl pagesKidsFlow, final double width, final double height) throws IOException {
		super(pdfWriter, null, width, height);
		if (width < PDFWriter.MIN_PAGE_WIDTH || height < PDFWriter.MIN_PAGE_HEIGHT) {
			throw new IllegalArgumentException("Page size is too small: " + width + "x" + height);
		}
		if (width > PDFWriter.MAX_PAGE_WIDTH || height > PDFWriter.MAX_PAGE_HEIGHT) {
			throw new IllegalArgumentException("Page size exceeds limits: " + width + "x" + height);
		}

		final var params = pdfWriter.getParams();
		if (params.version() == PDFParams.Version.V_PDFX1A) {
			this.artBox = new Rectangle2D.Double(0, 0, width, height);
		}

		final var mainFlow = pdfWriter.mainFlow;
		final var xref = pdfWriter.xref;

		this.pageRef = xref.nextObjectRef();
		mainFlow.startObject(this.pageRef);
		pagesKidsFlow.writeObjectRef(this.pageRef);
		mainFlow.startHash();

		mainFlow.writeName("Type");
		mainFlow.writeName("Page");
		mainFlow.lineBreak();

		this.paramsFlow = mainFlow.forkFragment();
		this.mediaBox = new Rectangle2D.Double(0, 0, width, height);

		mainFlow.writeName("Parent");
		mainFlow.writeObjectRef(rootPageRef);
		mainFlow.lineBreak();

		mainFlow.writeName("Resources");
		mainFlow.writeObjectRef(pdfWriter.pageResourceRef);
		mainFlow.lineBreak();

		mainFlow.writeName("Contents");
		final var contentsRef = xref.nextObjectRef();
		mainFlow.writeObjectRef(contentsRef);
		mainFlow.lineBreak();

		this.annotsFlow = mainFlow.forkFragment();
		mainFlow.lineBreak();

		mainFlow.endHash();
		mainFlow.endObject();

		this.pageFlow = mainFlow.forkFragment();
		this.pageFlow.startObject(contentsRef);

		// Always use ASCII/Flate compression for page contents
		this.out = this.pageFlow.startStream(PDFFragmentOutput.Mode.ASCII);
	}

	private PDFWriterImpl getPDFWriterImpl() {
		return (PDFWriterImpl) this.pdfWriter;
	}

	public PDFPageOutput getPDFPageOutput() {
		return this;
	}

	public void useResource(final String type, final String name) throws IOException {
		final var pdfWriter = this.getPDFWriterImpl();
		final var resourceFlow = pdfWriter.pageResourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		final var nameToResourceRef = pdfWriter.nameToResourceRef;
		final var objectRef = nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	/**
	 * Adds an annotation to this page.
	 * 
	 * @param annot The annotation to add
	 * @throws IOException If an I/O error occurs
	 */
	public void addAnnotation(final Annot annot) throws IOException {
		final var pdfWriterImpl = this.getPDFWriterImpl();
		final var params = pdfWriterImpl.getParams();
		if (params.version() == PDFParams.Version.V_PDFX1A) {
			throw new UnsupportedOperationException("Annotations are not allowed in PDF/X standards.");
		}

		if (!this.hasAnnots) {
			this.annotsFlow.writeName("Annots");
			this.annotsFlow.startArray();
			this.hasAnnots = true;
		}

		final var annotRef = pdfWriterImpl.xref.nextObjectRef();
		this.annotsFlow.writeObjectRef(annotRef);

		// Write annotation object to a separate fragment
		try (final var objectsFlow = pdfWriterImpl.objectsFlow.forkFragment()) {
			objectsFlow.startObject(annotRef);
			objectsFlow.startHash();
			annot.writeTo(objectsFlow, this);

			// Required flags for PDF/A or PDF/X
			if (params.version() == PDFParams.Version.V_PDFA1B
					|| params.version() == PDFParams.Version.V_PDFX1A) {
				objectsFlow.writeName("F");
				objectsFlow.writeInt(0x04); // Print flag
				objectsFlow.lineBreak();
			}

			objectsFlow.endHash();
			objectsFlow.endObject();
		}
	}

	/**
	 * Adds a fragment.
	 */
	@SuppressWarnings("resource")
	public void addFragment(final String id, final Point2D location) throws IOException {
		final Destination dest = new Destination(this.pageRef, location.getX(), this.height - location.getY(), 0);
		this.getPDFWriterImpl().fragments.addEntry(id, dest);
	}

	/**
	 * Starts bookmark hierarchy.
	 * <p>
	 * The number of endBookmark calls does not need to match startBookmark.
	 * Unclosed hierarchies are automatically closed when document construction is
	 * complete.
	 * </p>
	 * 
	 * @param title    the title
	 * @param location the location
	 * @throws IOException in case of I/O error
	 */
	@SuppressWarnings("resource")
	public void startBookmark(final String title, final Point2D location) throws IOException {
		if (this.getPDFWriterImpl().outline != null) {
			this.getPDFWriterImpl().outline.startBookmark(this.pageRef, title, this.height, location.getX(),
					location.getY());
		}
	}

	/**
	 * Ends the current bookmark hierarchy level.
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	public void endBookmark() throws IOException {
		final var outline = this.getPDFWriterImpl().outline;
		if (outline != null) {
			outline.endBookmark();
		}
	}

	private void paramRect(final Rectangle2D r) throws IOException {
		this.paramsFlow.startArray();
		this.paramsFlow.writeReal(r.getMinX());
		this.paramsFlow.writeReal(this.height - r.getMaxY());
		this.paramsFlow.writeReal(r.getMaxX());
		this.paramsFlow.writeReal(this.height - r.getMinY());
		this.paramsFlow.endArray();
		this.paramsFlow.lineBreak();
	}

	public void setMediaBox(final Rectangle2D mediaBox) {
		if (mediaBox == null) {
			throw new NullPointerException("MediaBox cannot be null");
		}
		this.mediaBox = mediaBox;
	}

	public void setCropBox(final Rectangle2D cropBox) {
		this.cropBox = cropBox;
	}

	public void setBleedBox(final Rectangle2D bleedBox) {
		if (bleedBox != null && this.pdfWriter.getParams().version().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("BleedBox requires PDF 1.4+.");
		}
		this.bleedBox = bleedBox;
	}

	public void setTrimBox(final Rectangle2D trimBox) {
		if (trimBox != null && this.pdfWriter.getParams().version().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("TrimBox requires PDF 1.4+.");
		}
		this.trimBox = trimBox;
	}

	public void setArtBox(final Rectangle2D artBox) {
		if (artBox != null && this.pdfWriter.getParams().version().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("ArtBox requires PDF 1.4+.");
		}
		this.artBox = artBox;
	}

	public void close() throws IOException {
		super.close();

		if (this.mediaBox == null) {
			throw new IllegalStateException();
		}
		this.paramsFlow.writeName("MediaBox");
		this.paramRect(this.mediaBox);

		if (this.cropBox != null) {
			this.paramsFlow.writeName("CropBox");
			this.paramRect(this.cropBox);
		}

		if (this.bleedBox != null) {
			this.paramsFlow.writeName("BleedBox");
			this.paramRect(this.bleedBox);
		}

		if (this.trimBox != null) {
			this.paramsFlow.writeName("TrimBox");
			this.paramRect(this.trimBox);
		}

		if (this.artBox != null) {
			this.paramsFlow.writeName("ArtBox");
			this.paramRect(this.artBox);
		}

		this.paramsFlow.close();

		if (this.hasAnnots) {
			this.annotsFlow.endArray();
			this.hasAnnots = false;
		}
		this.annotsFlow.close();
		this.pageFlow.endObject();
		this.pageFlow.close();
	}
}
