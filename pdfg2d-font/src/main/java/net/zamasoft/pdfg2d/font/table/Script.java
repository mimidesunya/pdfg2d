package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a script table in an OpenType font.
 * 
 * @param defaultLangSys the default language system table
 * @param langSysRecords array of language system records
 * @param langSys        array of language system tables
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record Script(
		LangSys defaultLangSys,
		LangSysRecord[] langSysRecords,
		LangSys[] langSys) {

	/**
	 * Reads a Script by reading from the given file.
	 * 
	 * @param raf    the file to read from
	 * @param offset the offset to seek to
	 * @return a new Script instance
	 * @throws IOException if an I/O error occurs
	 */
	protected static Script read(final RandomAccessFile raf, final int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			final int defaultLangSysOffset = raf.readUnsignedShort();
			final int langSysCount = raf.readUnsignedShort();

			LangSysRecord[] langSysRecords = null;
			if (langSysCount > 0) {
				langSysRecords = new LangSysRecord[langSysCount];
				for (int i = 0; i < langSysCount; i++) {
					langSysRecords[i] = LangSysRecord.read(raf);
				}
			}

			// Read the LangSys tables
			LangSys[] langSys = null;
			if (langSysCount > 0) {
				langSys = new LangSys[langSysCount];
				for (int i = 0; i < langSysCount; i++) {
					raf.seek(offset + langSysRecords[i].offset());
					langSys[i] = LangSys.read(raf);
				}
			}

			LangSys defaultLangSys = null;
			if (defaultLangSysOffset > 0) {
				raf.seek(offset + defaultLangSysOffset);
				defaultLangSys = LangSys.read(raf);
			}
			return new Script(defaultLangSys, langSysRecords, langSys);
		}
	}

	public LangSys getDefaultLangSys() {
		return this.defaultLangSys;
	}

	@Override
	public String toString() {
		if (this.langSysRecords == null) {
			return "";
		}
		final var sb = new StringBuilder();
		for (final LangSysRecord record : this.langSysRecords) {
			final int tag = record.tag();
			sb.append((char) ((tag >> 24) & 0xff))
					.append((char) ((tag >> 16) & 0xff))
					.append((char) ((tag >> 8) & 0xff))
					.append((char) ((tag) & 0xff))
					.append('/');
		}
		return sb.toString();
	}
}
