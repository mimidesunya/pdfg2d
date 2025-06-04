/*

 Copyright 2001,2003  The Apache Software Foundation 

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
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class VorgTable implements Table {

	private DirectoryEntry de;

	private RandomAccessFile raf;

	private short defaultVertOriginY;

	private int numVertOriginYMetrics;

	private Map<Integer, Short> indexToVertY = null;

	protected VorgTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		this.de = de;
		this.raf = raf;
	}

	protected void load() {
		if (this.raf == null) {
			return;
		}
		synchronized (this.raf) {
			try {
				this.raf.seek(this.de.getOffset());
				this.raf.readUnsignedShort();
				this.raf.readUnsignedShort();
				this.defaultVertOriginY = this.raf.readShort();
				this.numVertOriginYMetrics = this.raf.readUnsignedShort();

				this.indexToVertY = new HashMap<Integer, Short>();
				for (int i = 0; i < this.numVertOriginYMetrics; ++i) {
					int glyphIndex = this.raf.readUnsignedShort();
					short vertOriginY = this.raf.readShort();
					this.indexToVertY.put(Integer.valueOf(glyphIndex), Short.valueOf(vertOriginY));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			this.raf = null;
		}
	}

	public short getDefaultVertOriginY() {
		this.load();
		return this.defaultVertOriginY;
	}

	public short getVertOrigunY(int ix) {
		this.load();
		Short y = (Short) this.indexToVertY.get(Integer.valueOf(ix));
		if (y == null) {
			return this.defaultVertOriginY;
		}
		return y.shortValue();
	}

	public int getType() {
		return VORG;
	}
}
