package net.zamasoft.pdfg2d.pdf.action;

import java.io.IOException;

import net.zamasoft.pdfg2d.pdf.PdfOutput;
import net.zamasoft.pdfg2d.pdf.params.PdfParams;

/**
 * JavaScriptを実行するアクションです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: JavaScriptAction.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class JavaScriptAction extends Action {
	protected final String script;

	public JavaScriptAction(String script) {
		this.script = script;
	}

	public String getScript() {
		return this.script;
	}

	public void writeTo(PdfOutput out) throws IOException {
		super.writeTo(out);
		if (this.params.getVersion() < PdfParams.VERSION_1_3) {
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
