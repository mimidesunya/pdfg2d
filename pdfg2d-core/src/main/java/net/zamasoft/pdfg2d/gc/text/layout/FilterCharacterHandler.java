package net.zamasoft.pdfg2d.gc.text.layout;

import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.CharacterHandler;
import net.zamasoft.pdfg2d.gc.text.Quad;

public class FilterCharacterHandler implements CharacterHandler {
	protected CharacterHandler characterHandler;

	public FilterCharacterHandler(final CharacterHandler characterHandler) {
		this.setCharacterHandler(characterHandler);
	}

	public FilterCharacterHandler() {
		// default
	}

	public CharacterHandler getCharacterHandler() {
		return this.characterHandler;
	}

	public void setCharacterHandler(final CharacterHandler characterHandler) {
		this.characterHandler = characterHandler;
	}

	@Override
	public void characters(final int charOffset, final char[] ch, final int off, final int len) {
		this.characterHandler.characters(charOffset, ch, off, len);
	}

	@Override
	public void quad(final Quad quad) {
		this.characterHandler.quad(quad);
	}

	@Override
	public void fontStyle(final FontStyle fontStyle) {
		this.characterHandler.fontStyle(fontStyle);
	}

	@Override
	public void flush() {
		this.characterHandler.flush();
	}

	@Override
	public void close() {
		this.characterHandler.flush();
	}
}
