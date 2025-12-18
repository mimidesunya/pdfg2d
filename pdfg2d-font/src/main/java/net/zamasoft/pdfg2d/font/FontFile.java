package net.zamasoft.pdfg2d.font;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.zip.InflaterInputStream;

import net.zamasoft.pdfg2d.font.util.BufferedRandomAccessFile;

/**
 * Class for accessing TTC, TTF, or WOFF files.
 * Provides file format handling and extraction.
 */
public class FontFile {
	public final long timestamp;

	private final OpenTypeFont[] fonts;
	private final long[] offsets;
	private final File file;
	private final boolean woff;

	/**
	 * @param file The TTC, TTF, or WOFF file.
	 * @throws IOException If an I/O error occurs.
	 */
	public FontFile(final File file) throws IOException {
		this.timestamp = file.lastModified();
		try (final RandomAccessFile raf = new BufferedRandomAccessFile(file, "r")) {
			final byte[] tagBytes = new byte[4];
			raf.readFully(tagBytes);
			String tag = new String(tagBytes, StandardCharsets.ISO_8859_1);

			if ("wOFF".equals(tag)) {
				// WOFF
				this.file = extractWoff(raf, file);
				this.woff = true;

				// Re-read header from extracted file to check if it's TTC
				try (final RandomAccessFile tempRaf = new BufferedRandomAccessFile(this.file, "r")) {
					tempRaf.readFully(tagBytes);
					tag = new String(tagBytes, StandardCharsets.ISO_8859_1);
					if ("ttcf".equals(tag)) {
						tempRaf.skipBytes(4);
						final int numFonts = tempRaf.readInt();
						this.offsets = new long[numFonts];
						this.fonts = new OpenTypeFont[numFonts];
						for (int i = 0; i < numFonts; i++) {
							this.offsets[i] = tempRaf.readInt();
						}
					} else {
						// Single TTF
						this.offsets = new long[] { 0 };
						this.fonts = new OpenTypeFont[1];
					}
				}
			} else {
				this.file = file;
				this.woff = false;
				if ("ttcf".equals(tag)) {
					// TTC
					raf.skipBytes(4);
					final int numFonts = raf.readInt();
					this.offsets = new long[numFonts];
					this.fonts = new OpenTypeFont[numFonts];
					for (int i = 0; i < numFonts; i++) {
						this.offsets[i] = raf.readInt();
					}
				} else {
					// Single TTF
					this.offsets = new long[] { 0 };
					this.fonts = new OpenTypeFont[1];
				}
			}
		}
	}

	private File extractWoff(final RandomAccessFile raf, final File originalFile) throws IOException {
		final File tempFile = File.createTempFile("pdfg2d-font-", ".dat");
		try (final DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(tempFile)))) {
			raf.seek(4); // Skip signature
			final int version = raf.readInt();
			raf.skipBytes(4); // length
			final short numTables = raf.readShort();
			raf.skipBytes(2); // reserved
			raf.skipBytes(4); // totalSfntSize
			raf.skipBytes(22); // majorVersion to metaOrigLength

			// Write SFNT header
			out.writeInt(version);
			out.writeShort(numTables);
			out.writeShort(0); // searchRange
			out.writeShort(0); // entrySelector
			out.writeShort(0); // rangeShift

			// Write table directory
			int outOffset = out.size() + numTables * 16;
			final byte[] tagBuff = new byte[4];

			for (int i = 0; i < numTables; i++) {
				raf.seek(44 + i * 20); // TableDirectory entries start at 44
				raf.readFully(tagBuff);
				raf.skipBytes(4); // offset
				raf.skipBytes(4); // compLen
				final int origLen = raf.readInt();
				final int origChecksum = raf.readInt();

				// Write Directory Entry in SFNT format
				out.write(tagBuff);
				out.writeInt(origChecksum);
				out.writeInt(outOffset);
				out.writeInt(origLen);
				outOffset += origLen;
			}

			// Write table data
			final byte[] buff = new byte[1024];
			for (int i = 0; i < numTables; i++) {
				raf.seek(44 + i * 20 + 4);
				final int inOffset = raf.readInt();
				final int compLen = raf.readInt();
				final int origLen = raf.readInt();

				int remainder = origLen;
				try (final InputStream in = new FileInputStream(originalFile)) {
					in.skip(inOffset);
					if (compLen == origLen) {
						// no compression
						int len;
						while (remainder > 0 && (len = in.read(buff, 0, Math.min(remainder, buff.length))) != -1) {
							out.write(buff, 0, len);
							remainder -= len;
						}
					} else {
						// gzipped
						try (final InputStream zin = new InflaterInputStream(in)) {
							int len;
							while (remainder > 0 && (len = zin.read(buff, 0, Math.min(remainder, buff.length))) != -1) {
								out.write(buff, 0, len);
								remainder -= len;
							}
						}
					}
				}
				while (remainder > 0) {
					out.writeByte(0);
					remainder--;
				}
			}
		}
		tempFile.deleteOnExit();
		return tempFile;
	}

	/**
	 * Returns true if file is a Font Collection (TTC).
	 *
	 * @return true if TTC.
	 */
	public boolean isFontCollection() {
		return this.fonts != null && this.fonts.length > 1;
	}

	/**
	 * Returns the number of fonts in the file.
	 * Always 1 for TTF/OTF.
	 *
	 * @return Number of fonts.
	 */
	public int getNumFonts() {
		return this.fonts.length;
	}

	/**
	 * Returns the font at the specified index.
	 * For TTF, index must be 0.
	 *
	 * @param i Index of the font.
	 * @return The font.
	 * @throws IOException If I/O error occurs.
	 */
	public OpenTypeFont getFont(final int i) throws IOException {
		if (this.fonts[i] == null) {
			final RandomAccessFile fontRaf = new BufferedRandomAccessFile(this.file, "r");
			fontRaf.seek(this.offsets[i]);
			if (this.woff) {
				this.fonts[i] = new OpenTypeFont(fontRaf, () -> {
					if (!this.file.delete()) {
						// ignore
					}
				});
			} else {
				this.fonts[i] = new OpenTypeFont(fontRaf);
			}
		}
		return this.fonts[i];
	}

	/**
	 * Returns the first font.
	 *
	 * @return The first font.
	 * @throws IOException If I/O error occurs.
	 */
	public OpenTypeFont getFont() throws IOException {
		return this.getFont(0);
	}
}
