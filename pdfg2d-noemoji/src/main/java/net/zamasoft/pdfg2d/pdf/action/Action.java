package net.zamasoft.pdfg2d.pdf.action;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PdfOutput;
import net.zamasoft.pdfg2d.pdf.params.PdfParams;

public class Action {
	protected PdfParams params;
	private Action[] next = null;

	public void setParams(PdfParams params) {
		this.params = params;
	}

	public Action[] getNext() {
		return this.next;
	}

	public void setNext(Action[] next) {
		this.next = next;
	}

	public void writeTo(PdfOutput out) throws IOException {
		out.writeName("Type");
		out.writeName("Action");
		out.lineBreak();

		if (this.next != null && this.next.length > 0) {
			out.writeName("Next");
			if (this.next.length == 1) {
				out.startHash();
				this.next[0].setParams(this.params);
				this.next[0].writeTo(out);
				out.endHash();
			} else {
				out.startArray();
				for (int i = 0; i < this.next.length; ++i) {
					out.startHash();
					this.next[i].setParams(this.params);
					this.next[i].writeTo(out);
					out.endHash();
				}
				out.endArray();
			}
			out.lineBreak();
		}
	}
}
