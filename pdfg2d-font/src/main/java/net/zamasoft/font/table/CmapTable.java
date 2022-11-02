/*

 Copyright 2001  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.zamasoft.font.OpenTypeFont;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class CmapTable implements Table {

	private final int numTables;

	private final CmapIndexEntry[] entries;

	private final CmapFormat[] formats;

	private final OpenTypeFont otf;

	private final RandomAccessFile raf;

	private final long fp;

	protected CmapTable(OpenTypeFont otf, DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());
			this.fp = raf.getFilePointer();
			raf.readUnsignedShort(); // version
			this.numTables = raf.readUnsignedShort();
			this.entries = new CmapIndexEntry[this.numTables];
			this.formats = new CmapFormat[this.numTables];

			// Get each of the index entries
			for (int i = 0; i < this.numTables; i++) {
				this.entries[i] = new CmapIndexEntry(raf);
			}

			this.raf = raf;
			this.otf = otf;
		}
	}

	public CmapFormat getCmapFormat(short platformId, short encodingId) {
		// Find the requested format
		for (int i = 0; i < this.numTables; i++) {
			if (this.entries[i].getPlatformId() == platformId
					&& (encodingId == -1 || this.entries[i].getEncodingId() == encodingId)) {
				return this.getCmapFormat(i);
			}
		}
		return null;
	}

	public synchronized CmapFormat getCmapFormat(int ix) {
		if (this.formats[ix] == null) {
			synchronized (raf) {
				try {
					this.raf.seek(this.fp + this.entries[ix].getOffset());
					int format = raf.readUnsignedShort();
					this.formats[ix] = CmapFormat.createCmapFormat(format, this.otf.getNumGlyphs(), this.raf);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return this.formats[ix];
	}

	public int getTableCount() {
		return this.numTables;
	}

	public int getType() {
		return cmap;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer().append("cmap\n");

		// Get each of the index entries
		for (int i = 0; i < this.numTables; i++) {
			sb.append("\t").append(this.entries[i]).append("\n");
		}

		// Get each of the tables
		for (int i = 0; i < this.numTables; i++) {
			sb.append("\t").append(this.formats[i]).append("\n");
		}
		return sb.toString();
	}
}
