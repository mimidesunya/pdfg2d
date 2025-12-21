package net.zamasoft.pdfg2d.pdf.action;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

public class Action {

	private Action[] next = null;

	public Action[] getNext() {
		return this.next;
	}

	public void setNext(Action[] next) {
		this.next = next;
	}

	public void writeTo(PDFOutput out, PDFParams params) throws IOException {
		out.writeName("Type");
		out.writeName("Action");
		out.lineBreak();

		if (this.next != null && this.next.length > 0) {
			out.writeName("Next");
			if (this.next.length == 1) {
				out.startHash();
				this.next[0].writeTo(out, params);
				out.endHash();
			} else {
				out.startArray();
				for (int i = 0; i < this.next.length; ++i) {
					out.startHash();
					this.next[i].writeTo(out, params);
					out.endHash();
				}
				out.endArray();
			}
			out.lineBreak();
		}
	}
}
