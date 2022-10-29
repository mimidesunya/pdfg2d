package net.zamasoft.pdfg2d.pdf.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfOutput.Destination;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: OutlineFlow.java 1565 2018-07-04 11:51:25Z miyabe $
 */
class OutlineFlow {
	private final XRefImpl xref;

	private final PdfFragmentOutputImpl out, catalogFlow;

	private OutlineEntry firstOutline = null, lastOutline = null, currentOutline = null;

	private int rootOutlineCount = 0;

	public OutlineFlow(PdfWriterImpl pdfWriter) throws IOException {
		this.xref = pdfWriter.xref;
		this.out = pdfWriter.mainFlow.forkFragment();
		this.catalogFlow = pdfWriter.catalogFlow;
	}

	public void startBookmark(ObjectRef pageRef, String title, double t, double x, double y) {
		Destination dest = new Destination(pageRef, x, t - y, 0);
		OutlineEntry outline = new OutlineEntry(title, dest);

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

	public void close() throws IOException {
		if (this.firstOutline == null) {
			this.out.close();
			return;
		}

		// titleが空で、子ノードがないエントリは出力しない
		// オブジェクトリファレンスを生成
		{
			OutlineEntry outline = this.firstOutline;
			FOR: for (;;) {
				if (outline.isEmpty()) {
					if (outline.prev != null) {
						outline.prev.next = outline.next;
					}
					if (outline.next != null) {
						outline.next.prev = outline.prev;
					}
					if (outline.parent != null) {
						if (outline.parent.first == outline) {
							outline.parent.first = outline.next;
						}
						--outline.parent.count;
					} else {
						if (this.firstOutline == outline) {
							this.firstOutline = outline.next;
						}
						--this.rootOutlineCount;
					}
				} else {
					outline.ref = this.xref.nextObjectRef();
					if (outline.parent != null) {
						outline.parent.last = outline;
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

		// カタログを更新
		this.catalogFlow.writeName("Outlines");
		ObjectRef outlineRef = this.xref.nextObjectRef();
		this.catalogFlow.writeObjectRef(outlineRef);
		this.catalogFlow.lineBreak();

		this.catalogFlow.writeName("PageMode");
		this.catalogFlow.writeName("UseOutlines");
		this.catalogFlow.lineBreak();

		// ルート情報を出力
		this.out.startObject(outlineRef);
		this.out.startHash();
		this.out.writeName("Count");
		this.out.writeInt(this.rootOutlineCount);
		this.out.lineBreak();

		this.out.writeName("First");
		this.out.writeObjectRef(this.firstOutline.ref);
		this.out.lineBreak();

		this.out.writeName("Last");
		this.out.writeObjectRef(this.lastOutline.ref);
		this.out.lineBreak();

		this.out.endHash();
		this.out.endObject();

		// エントリを出力
		{
			OutlineEntry outline = this.firstOutline;
			FOR: for (;;) {
				this.out.startObject(outline.ref);
				this.out.startHash();

				this.out.writeName("Parent");
				if (outline.parent == null) {
					this.out.writeObjectRef(outlineRef);
				} else {
					this.out.writeObjectRef(outline.parent.ref);
				}
				this.out.lineBreak();

				this.out.writeName("Dest");
				this.out.writeDestination(outline.dest);
				this.out.lineBreak();

				this.out.writeName("Title");
				if (outline.title == null) {
					this.out.writeText("");
				} else {
					this.out.writeText(outline.title);
				}
				this.out.lineBreak();

				if (outline.prev != null) {
					this.out.writeName("Prev");
					this.out.writeObjectRef(outline.prev.ref);
					this.out.lineBreak();
				}

				if (outline.next != null) {
					this.out.writeName("Next");
					this.out.writeObjectRef(outline.next.ref);
					this.out.lineBreak();
				}

				if (outline.count > 0) {
					this.out.writeName("First");
					this.out.writeObjectRef(outline.first.ref);
					this.out.lineBreak();

					this.out.writeName("Last");
					this.out.writeObjectRef(outline.last.ref);
					this.out.lineBreak();

					this.out.writeName("Count");
					this.out.writeInt(-outline.count);
					this.out.lineBreak();
				}

				this.out.endHash();
				this.out.endObject();

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
		this.out.close();
	}
}

class OutlineEntry {
	public final String title;

	public final Destination dest;

	public ObjectRef ref;

	public OutlineEntry parent, prev, next, first, last;

	public int count = 0;

	public OutlineEntry(String title, Destination dest) {
		this.title = title;
		this.dest = dest;
	}

	public boolean isEmpty() {
		return this.title == null && this.count <= 0;
	}
}
