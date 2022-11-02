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

/**
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public class KernTable implements Table {
	private int nTables;

	private KernSubtable[] tables;

	/** Creates new KernTable */
	protected KernTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());
			raf.readUnsignedShort();// version
			this.nTables = raf.readUnsignedShort();
			this.tables = new KernSubtable[this.nTables];
			for (int i = 0; i < this.nTables; i++) {
				this.tables[i] = KernSubtable.read(raf);
			}
		}
	}

	public int getSubtableCount() {
		return this.nTables;
	}

	public KernSubtable getSubtable(int i) {
		return this.tables[i];
	}

	/**
	 * Get the table type, as a table directory value.
	 * 
	 * @return The table type
	 */
	public int getType() {
		return kern;
	}

}
