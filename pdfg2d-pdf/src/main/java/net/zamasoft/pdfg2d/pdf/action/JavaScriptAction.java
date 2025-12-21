package net.zamasoft.pdfg2d.pdf.action;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * Action that executes JavaScript.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class JavaScriptAction extends Action {
	protected final String script;

	public JavaScriptAction(final String script) {
		this.script = script;
	}

	public String getScript() {
		return this.script;
	}

	public void writeTo(final PDFOutput out, final PDFParams params) throws IOException {
		super.writeTo(out, params);
		if (params.version().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("JavaScript Action requires PDF 1.3 or later.");
		}
		out.writeName("S");
		out.writeName("JavaScript");
		out.lineBreak();

		out.writeName("JS");
		out.writeText(this.script);
		out.lineBreak();
	}

	public String toString() {
		return "JavaScript: " + this.script;
	}
}
