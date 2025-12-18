package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record CmapIndexEntry(int platformId, int encodingId, int offset) {

	public static CmapIndexEntry read(RandomAccessFile raf) throws IOException {
		int platformId = raf.readUnsignedShort();
		int encodingId = raf.readUnsignedShort();
		int offset = raf.readInt();
		return new CmapIndexEntry(platformId, encodingId, offset);
	}

	@Override
	public String toString() {
		String platform = switch (platformId) {
			case 1 -> " (Macintosh)";
			case 3 -> " (Windows)";
			default -> "";
		};

		String encoding = "";
		if (platformId == 3) {
			// Windows specific encodings
			encoding = switch (encodingId) {
				case 0 -> " (Symbol)";
				case 1 -> " (Unicode)";
				case 2 -> " (ShiftJIS)";
				case 3 -> " (Big5)";
				case 4 -> " (PRC)";
				case 5 -> " (Wansung)";
				case 6 -> " (Johab)";
				default -> "";
			};
		}

		return new StringBuilder()
				.append("platform id: ").append(platformId).append(platform)
				.append(", encoding id: ").append(encodingId).append(encoding)
				.append(", offset: ").append(offset)
				.toString();
	}
}
