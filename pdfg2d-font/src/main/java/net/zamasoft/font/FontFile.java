package net.zamasoft.font;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.InflaterInputStream;

import net.zamasoft.font.util.BufferedRandomAccessFile;

/**
 * TTCまたはTTF, WOFFファイルへアクセスするためのクラスです。 このクラスはTTC/TTF/WOFFファイルの判定を行います。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FontFile {
	public final long timestamp;

	private final OpenTypeFont[] fonts;

	private final long[] offsets;

	private final File file;

	private final boolean woff;

	/**
	 * 
	 * @param file
	 *            TTCまたはTTF, WOFFファイル。
	 * @throws IOException
	 */
	public FontFile(File file) throws IOException {
		this.timestamp = file.lastModified();
		RandomAccessFile raf = new BufferedRandomAccessFile(file, "r");
		try {
			byte[] tagBytes = new byte[4];
			raf.readFully(tagBytes);
			String tag = new String(tagBytes, "ISO-8859-1");
			if ("wOFF".equals(tag)) {
				// WOFF
				this.file = File.createTempFile("copper", "dat");
				try (DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(this.file)))) {
					int version = raf.readInt();
					raf.skipBytes(4);
					int numTables = raf.readShort();
					raf.skipBytes(30);

					out.writeInt(version);
					out.writeShort(numTables);
					out.writeShort(0);
					out.writeShort(0);
					out.writeShort(0);

					int outOffset = out.size() + numTables * 16;
					for (int i = 0; i < numTables; i++) {
						raf.seek(44 + i * 20);
						raf.readFully(tagBytes);
						tag = new String(tagBytes, "ISO-8859-1");
						raf.skipBytes(8);
						int origLen = raf.readInt();

						out.writeBytes(tag);
						out.writeInt(0);
						out.writeInt(outOffset);
						out.writeInt(origLen);
						outOffset += origLen;
					}
					byte[] buff = new byte[1024];
					for (int i = 0; i < numTables; i++) {
						raf.seek(44 + i * 20 + 4);
						int inOffset = raf.readInt();
						int compLen = raf.readInt();
						int origLen = raf.readInt();

						int remainder = origLen;
						try (InputStream in = new FileInputStream(file)) {
							in.skip(inOffset);
							if (compLen == origLen) {
								// no compression
								for (int len = in.read(buff, 0, Math.min(remainder, buff.length)); remainder > 0
										&& len != -1; len = in.read(buff, 0, Math.min(remainder, buff.length))) {
									out.write(buff, 0, len);
									remainder -= len;
								}
							} else {
								// gzipped
								try (InputStream zin = new InflaterInputStream(in)) {
									for (int len = zin.read(buff, 0, Math.min(remainder, buff.length)); remainder > 0
											&& len != -1; len = zin.read(buff, 0, Math.min(remainder, buff.length))) {
										out.write(buff, 0, len);
										remainder -= len;
									}
								}
							}
						}
						for (; remainder > 0; --remainder) {
							out.writeByte(0);
						}
					}
				}
				this.file.deleteOnExit();
				raf.close();
				raf = new BufferedRandomAccessFile(this.file, "r");
				this.woff = true;
			} else {
				this.file = file;
				this.woff = false;
			}
			if ("ttcf".equals(tag)) {
				// TTC
				raf.skipBytes(4);
				int numFonts = raf.readInt();
				this.offsets = new long[numFonts];
				this.fonts = new OpenTypeFont[numFonts];
				for (int i = 0; i < numFonts; i++) {
					this.offsets[i] = raf.readInt();
				}
			} else {
				// other
				this.offsets = new long[] { 0 };
				this.fonts = new OpenTypeFont[1];
			}
		} finally {
			raf.close();
		}
	}

	/**
	 * TTCファイルであればtrueを返します。
	 * 
	 * @return TTCファイルであればtrueです。
	 */
	public boolean isFontCollection() {
		return this.fonts != null;
	}

	/**
	 * TTCに含まれるフォントの数を返します。 TTFの場合は常に1を返します。
	 * 
	 * @return フォントの数です。
	 */
	public int getNumFonts() {
		return this.fonts.length;
	}

	/**
	 * TTCに含まれるフォントを返します。 TTFの場合、iは常に0でなければなりません。
	 * 
	 * @param i
	 * @return １つのフォントです。
	 */
	public OpenTypeFont getFont(int i) throws IOException {
		if (this.fonts[i] == null) {
			RandomAccessFile raf = new BufferedRandomAccessFile(this.file, "r");
			raf.seek(this.offsets[i]);
			if (this.woff) {
				this.fonts[i] = new OpenTypeFont(raf) {
					protected void finalize() throws Throwable {
						super.finalize();
						file.delete();
					}
				};
			} else {
				this.fonts[i] = new OpenTypeFont(raf);
			}
		}
		return this.fonts[i];
	}

	/**
	 * 最初のフォントを返します。 TTFの場合は、唯一のフォントを返します。
	 * 
	 * @return １つのフォントです。
	 */
	public OpenTypeFont getFont() throws IOException {
		return this.getFont(0);
	}
}
