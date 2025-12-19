package net.zamasoft.pdfg2d.pdf.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;

import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.annot.Annot;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
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
			throw new IllegalArgumentException("Page size is less than 3pt: width=" + width + ",height=" + height);
		}
		if (width > PDFWriter.MAX_PAGE_WIDTH || height > PDFWriter.MAX_PAGE_HEIGHT) {
			throw new IllegalArgumentException("Page size exceeds 14400pt: width=" + width + ",height=" + height);
		}
		if (pdfWriter.getParams().getVersion() == PDFParams.Version.V_PDFX1A) {
			this.artBox = new Rectangle2D.Double(0, 0, width, height);
		}

		final PDFFragmentOutputImpl mainFlow = pdfWriter.mainFlow;

		this.pageRef = pdfWriter.xref.nextObjectRef();
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
		final ObjectRef contentsRef = pdfWriter.xref.nextObjectRef();
		mainFlow.writeObjectRef(contentsRef);
		mainFlow.lineBreak();

		this.annotsFlow = mainFlow.forkFragment();
		mainFlow.lineBreak();

		mainFlow.endHash();
		mainFlow.endObject();

		this.pageFlow = mainFlow.forkFragment();
		this.pageFlow.startObject(contentsRef);

		this.out = this.pageFlow.startStream(PDFFragmentOutput.Mode.ASCII);
	}

	private PDFWriterImpl getPDFWriterImpl() {
		return (PDFWriterImpl) this.pdfWriter;
	}

	public PDFPageOutput getPDFPageOutput() {
		return this;
	}

	public void useResource(final String type, final String name) throws IOException {
		final PDFWriterImpl pdfWriter = this.getPDFWriterImpl();
		final ResourceFlow resourceFlow = pdfWriter.pageResourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		final Map<String, ObjectRef> nameToResourceRef = pdfWriter.nameToResourceRef;

		final ObjectRef objectRef = nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	/**
	 * Adds an annotation.
	 */
	public void addAnnotation(final Annot annot) throws IOException {
		if (this.pdfWriter.getParams().getVersion() == PDFParams.Version.V_PDFX1A) {
			throw new UnsupportedOperationException("Annotations are not available in PDF/X.");
		}

		if (!this.hasAnnots) {
			this.annotsFlow.writeName("Annots");
			this.annotsFlow.startArray();
			this.hasAnnots = true;
		}
		@SuppressWarnings("resource")
		final ObjectRef annotRef = this.getPDFWriterImpl().xref.nextObjectRef();
		this.annotsFlow.writeObjectRef(annotRef);

		try (@SuppressWarnings("resource")
		PDFFragmentOutput objectsFlow = this.getPDFWriterImpl().objectsFlow.forkFragment()) {
			objectsFlow.startObject(annotRef);
			objectsFlow.startHash();

			// Output details
			annot.writeTo(objectsFlow, this);

			if (this.pdfWriter.getParams().getVersion() == PDFParams.Version.V_PDFA1B
					|| this.pdfWriter.getParams().getVersion() == PDFParams.Version.V_PDFX1A) {
				// Flags
				objectsFlow.writeName("F");
				objectsFlow.writeInt(0x04);
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
	 * Ends bookmark hierarchy.
	 * 
	 * @throws IOException in case of I/O error
	 */
	@SuppressWarnings("resource")
	public void endBookmark() throws IOException {
		if (this.getPDFWriterImpl().outline != null) {
			this.getPDFWriterImpl().outline.endBookmark();
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
			throw new NullPointerException();
		}
		this.mediaBox = mediaBox;
	}

	public void setCropBox(final Rectangle2D cropBox) {
		this.cropBox = cropBox;
	}

	public void setBleedBox(final Rectangle2D bleedBox) {
		if (bleedBox != null && this.pdfWriter.getParams().getVersion().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("BleedBox is available in PDF 1.4 or later.");
		}
		this.bleedBox = bleedBox;
	}

	public void setTrimBox(final Rectangle2D trimBox) {
		if (trimBox != null && this.pdfWriter.getParams().getVersion().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("TrimBox is available in PDF 1.4 or later.");
		}
		this.trimBox = trimBox;
	}

	public void setArtBox(final Rectangle2D artBox) {
		if (artBox != null && this.pdfWriter.getParams().getVersion().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("ArtBox is available in PDF 1.4 or later.");
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
