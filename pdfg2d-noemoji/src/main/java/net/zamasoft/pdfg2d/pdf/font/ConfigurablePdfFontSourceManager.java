package net.zamasoft.pdfg2d.pdf.font;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceValidity;
import jp.cssj.resolver.url.URLSource;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.FontSourceManager;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.pdf.font.util.MultimapUtils;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PDFFontSourceManagerImpl.java,v 1.1 2007-05-06 15:37:19
 *          miyabeExp $
 */
public class ConfigurablePdfFontSourceManager extends PdfFontSourceManager {
	private static final Logger LOG = Logger.getLogger(FontSourceManager.class.getName());

	private final Source config;

	private URI configURI = null;

	private SourceValidity configValidity = null;

	private static FontSourceManager fsm = null;

	public static final synchronized FontSourceManager getDefaultFontSourceManager() {
		if (fsm == null) {
			URL url = ConfigurablePdfFontSourceManager.class.getResource("builtin/fonts.xml");
			try {
				Source source = new URLSource(url);
				fsm = new ConfigurablePdfFontSourceManager(source, null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return fsm;
	}

	public ConfigurablePdfFontSourceManager(Source config) {
		this(config, null);
	}

	public ConfigurablePdfFontSourceManager(Source config, File dbFile) {
		this.config = config;
		this.configURI = this.config.getURI();
		this.poll();
	}

	protected synchronized void poll() {
		try {
			if (!this.config.exists()) {
				Exception e = new FileNotFoundException(this.config.getURI().toString());
				LOG.log(Level.SEVERE, this.config + "がありません", e);
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (this.configValidity != null && this.configValidity.getValid() == SourceValidity.VALID
				&& this.configURI.equals(this.config.getURI())) {
			return;
		}

		LOG.fine(this.config.getURI() + "からフォントをDBを構築しています...");
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		XMLReader parser;
		try {
			parser = new ParserAdapter(parserFactory.newSAXParser().getParser());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			PdfFontSourceManagerConfigurationHandler handler = new PdfFontSourceManagerConfigurationHandler(
					this.config.getURI());
			try (InputStream in = new BufferedInputStream(this.config.getInputStream())) {
				parser.setContentHandler(handler);
				parser.parse(new InputSource(in));
			}

			this.configURI = this.config.getURI();
			this.configValidity = this.config.getValidity();
			
			
			try {
				Class<?> clazz = Class.forName("jp.cssj.sakae.font.emoji.EmojiFontSource");
				clazz.getField("INSTANCES_LTR").get(null);
				FontLoader.add((FontSource)clazz.getField("INSTANCES_LTR").get(null), handler.nameToFonts);
				FontLoader.add((FontSource)clazz.getField("INSTANCES_TB").get(null), handler.nameToFonts);
			} catch (Exception e) {
				// ignore
			}
			
			this.nameToFonts = MultimapUtils.unmodifiableMap(handler.nameToFonts);
			this.genericToFamily = Collections.unmodifiableMap(handler.genericToFamily);
			this.allFonts = handler.allFonts;

			this.fontListCache = null;

			LOG.fine("フォントをDBを構築しました");
		} catch (Exception e) {
			LOG.log(Level.SEVERE, this.config.getURI() + "を読み込めませんでした", e);
			throw new RuntimeException(e);
		}
	}

	public synchronized FontSource[] lookup(FontStyle fontStyle) {
		this.poll();
		return super.lookup(fontStyle);
	}
}