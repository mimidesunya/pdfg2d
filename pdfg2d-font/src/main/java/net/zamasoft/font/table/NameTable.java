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
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class NameTable implements Table {

	private short numberOfNameRecords;

	private short stringStorageOffset;

	private NameRecord[] records;

	protected NameTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());
			raf.readShort(); // formatSelector
			numberOfNameRecords = raf.readShort();
			stringStorageOffset = raf.readShort();
			records = new NameRecord[numberOfNameRecords];

			// Load the records, which contain the encoding information and
			// string
			// offsets
			for (int i = 0; i < numberOfNameRecords; i++) {
				records[i] = new NameRecord(raf);
			}

			// Now load the strings
			for (int i = 0; i < numberOfNameRecords; i++) {
				records[i].loadString(raf, de.getOffset() + stringStorageOffset);
			}
		}
	}

	public String getRecord(short nameId) {

		// Search for the first instance of this name ID
		for (int i = 0; i < numberOfNameRecords; i++) {
			if (records[i].getNameId() == nameId) {
				return records[i].getRecordString();
			}
		}
		return "";
	}

	public int getType() {
		return name;
	}

	public NameRecord get(int i) {
		return this.records[i];
	}

	public int size() {
		return this.numberOfNameRecords;
	}
}
