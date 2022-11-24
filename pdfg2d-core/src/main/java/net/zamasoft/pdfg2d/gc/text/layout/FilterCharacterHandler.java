package net.zamasoft.pdfg2d.gc.text.layout;

import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.CharacterHandler;
import net.zamasoft.pdfg2d.gc.text.Quad;

public class FilterCharacterHandler implements CharacterHandler {
	protected CharacterHandler characterHandler;

	public FilterCharacterHandler(CharacterHandler characterHandler) {
		this.setCharacterHandler(characterHandler);
	}

	public FilterCharacterHandler() {
		// default
	}

	public CharacterHandler getCharacterHandler() {
		return this.characterHandler;
	}

	public void setCharacterHandler(CharacterHandler characterHandler) {
		this.characterHandler = characterHandler;
	}

	public void characters(int charOffset, char[] ch, int off, int len) {
		this.characterHandler.characters(charOffset, ch, off, len);
	}

	public void quad(Quad quad) {
		this.characterHandler.quad(quad);
	}

	public void fontStyle(FontStyle fontStyle) {
		this.characterHandler.fontStyle(fontStyle);
	}

	public void flush() {
		this.characterHandler.flush();
	}

	public void close() {
		this.characterHandler.flush();
	}
}
