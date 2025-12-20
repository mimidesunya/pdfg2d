package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFOutput.Destination;

/**
 * Manages the PDF document outline (bookmarks) hierarchy.
 * This class handles the creation of bookmark entries and serializes them
 * into the PDF structure during finalization.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class OutlineFlow {
	private final XRefImpl xref;

	private final PDFFragmentOutputImpl out, catalogFlow;

	private OutlineEntry firstOutline = null, lastOutline = null, currentOutline = null;

	private int rootOutlineCount = 0;

	public OutlineFlow(final PDFWriterImpl pdfWriter) throws IOException {
		this.xref = pdfWriter.xref;
		this.out = pdfWriter.mainFlow.forkFragment();
		this.catalogFlow = pdfWriter.catalogFlow;
	}

	/**
	 * Starts a new bookmark entry.
	 * 
	 * @param pageRef Reference to the target page object.
	 * @param title   The label for the bookmark.
	 * @param t       Page height to calculate relative top-origin.
	 * @param x       X coordinate.
	 * @param y       Y coordinate (top-origin).
	 */
	public void startBookmark(final ObjectRef pageRef, final String title, final double t, final double x,
			final double y) {
		final var dest = new Destination(pageRef, x, t - y, 0);
		final var outline = new OutlineEntry(title, dest);

		if (this.currentOutline == null) {
			if (this.firstOutline == null) {
				this.firstOutline = outline;
			} else {
				this.lastOutline.next = outline;
				outline.prev = this.lastOutline;
			}
			this.lastOutline = outline;
			this.rootOutlineCount++;
		} else {
			if (this.currentOutline.first == null) {
				this.currentOutline.first = outline;
			} else {
				this.currentOutline.last.next = outline;
				outline.prev = this.currentOutline.last;
			}
			this.currentOutline.last = outline;
			this.currentOutline.count++;
		}

		outline.parent = this.currentOutline;
		this.currentOutline = outline;
	}

	public void endBookmark() {
		this.currentOutline = this.currentOutline.parent;
	}

	/**
	 * Finalizes the outline hierarchy and writes it to the PDF output.
	 * 
	 * @throws IOException If an I/O error occurs.
	 */
	public void close() throws IOException {
		if (this.firstOutline == null) {
			this.out.close();
			return;
		}

		// First pass: Filter out empty entries and assign object references.
		{
			var outline = this.firstOutline;
			FOR: for (;;) {
				if (outline.isEmpty()) {
					if (outline.prev != null) {
						outline.prev.next = outline.next;
					}
					if (outline.next != null) {
						outline.next.prev = outline.prev;
					}
					final var parent = outline.parent;
					if (parent != null) {
						if (parent.first == outline) {
							parent.first = outline.next;
						}
						--parent.count;
					} else {
						if (this.firstOutline == outline) {
							this.firstOutline = outline.next;
						}
						--this.rootOutlineCount;
					}
				} else {
					outline.ref = this.xref.nextObjectRef();
					final var parent = outline.parent;
					if (parent != null) {
						parent.last = outline;
					} else {
						this.lastOutline = outline;
					}
				}

				if (outline.first != null) {
					outline = outline.first;
				} else {
					while (outline.next == null) {
						outline = outline.parent;
						if (outline == null) {
							break FOR;
						}
					}
					outline = outline.next;
				}
			}
		}

		if (this.firstOutline == null) {
			this.out.close();
			return;
		}

		final var outFlow = this.out;
		final var catalog = this.catalogFlow;

		// Update PDF Catalog with the Outline root
		catalog.writeName("Outlines");
		final var outlineRootRef = this.xref.nextObjectRef();
		catalog.writeObjectRef(outlineRootRef);
		catalog.lineBreak();

		catalog.writeName("PageMode");
		catalog.writeName("UseOutlines");
		catalog.lineBreak();

		// Write Outline Dictionary (Root)
		outFlow.startObject(outlineRootRef);
		outFlow.startHash();
		outFlow.writeName("Count");
		outFlow.writeInt(this.rootOutlineCount);
		outFlow.lineBreak();

		outFlow.writeName("First");
		outFlow.writeObjectRef(this.firstOutline.ref);
		outFlow.lineBreak();

		outFlow.writeName("Last");
		outFlow.writeObjectRef(this.lastOutline.ref);
		outFlow.lineBreak();

		outFlow.endHash();
		outFlow.endObject();

		// Second pass: Serialize entries to PDF objects
		{
			var outline = this.firstOutline;
			FOR: for (;;) {
				outFlow.startObject(outline.ref);
				outFlow.startHash();

				outFlow.writeName("Parent");
				outFlow.writeObjectRef(outline.parent == null ? outlineRootRef : outline.parent.ref);
				outFlow.lineBreak();

				outFlow.writeName("Dest");
				outFlow.writeDestination(outline.dest);
				outFlow.lineBreak();

				outFlow.writeName("Title");
				outFlow.writeText(outline.title == null ? "" : outline.title);
				outFlow.lineBreak();

				if (outline.prev != null) {
					outFlow.writeName("Prev");
					outFlow.writeObjectRef(outline.prev.ref);
					outFlow.lineBreak();
				}

				if (outline.next != null) {
					outFlow.writeName("Next");
					outFlow.writeObjectRef(outline.next.ref);
					outFlow.lineBreak();
				}

				if (outline.count > 0) {
					outFlow.writeName("First");
					outFlow.writeObjectRef(outline.first.ref);
					outFlow.lineBreak();

					outFlow.writeName("Last");
					outFlow.writeObjectRef(outline.last.ref);
					outFlow.lineBreak();

					outFlow.writeName("Count");
					// Negative count indicates bookmarks are closed by default
					outFlow.writeInt(-outline.count);
					outFlow.lineBreak();
				}

				outFlow.endHash();
				outFlow.endObject();

				if (outline.first != null) {
					outline = outline.first;
				} else {
					while (outline.next == null) {
						outline = outline.parent;
						if (outline == null) {
							break FOR;
						}
					}
					outline = outline.next;
				}
			}
		}
		outFlow.close();
	}

	/**
	 * Represents an internal bookmark entry data structure.
	 */
	private static class OutlineEntry {
		public final String title;
		public final Destination dest;
		public ObjectRef ref;
		public OutlineEntry parent, prev, next, first, last;
		public int count = 0;

		public OutlineEntry(final String title, final Destination dest) {
			this.title = title;
			this.dest = dest;
		}

		public boolean isEmpty() {
			return this.title == null && this.count <= 0;
		}
	}
}
