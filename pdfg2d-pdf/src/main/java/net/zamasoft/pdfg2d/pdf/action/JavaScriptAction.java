package net.zamasoft.pdfg2d.pdf.action;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * JavaScriptを実行するアクションです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class JavaScriptAction extends Action {
	protected final String script;

	public JavaScriptAction(String script) {
		this.script = script;
	}

	public String getScript() {
		return this.script;
	}

	public void writeTo(PDFOutput out) throws IOException {
		super.writeTo(out);
		if (this.params.getVersion().v < PDFParams.Version.V_1_3.v) {
			throw new UnsupportedOperationException("JavaScript Actionは PDF 1.3 以降で使用できます。");
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
