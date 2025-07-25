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
public class Lookup {

	// LookupFlag bit enumeration
	public static final int IGNORE_BASE_GLYPHS = 0x0002;

	public static final int IGNORE_BASE_LIGATURES = 0x0004;

	public static final int IGNORE_BASE_MARKS = 0x0008;

	public static final int MARK_ATTACHMENT_TYPE = 0xFF00;

	private int type;

	private int subTableCount;

	private int[] subTableOffsets;

	private LookupSubtable[] subTables;

	/** Creates new Lookup */
	public Lookup(LookupSubtableFactory factory, RandomAccessFile raf, int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			type = raf.readUnsignedShort();
			raf.readUnsignedShort(); // flag
			subTableCount = raf.readUnsignedShort();
			subTableOffsets = new int[subTableCount];
			subTables = new LookupSubtable[subTableCount];
			for (int i = 0; i < subTableCount; i++) {
				subTableOffsets[i] = raf.readUnsignedShort();
			}
			for (int i = 0; i < subTableCount; i++) {
				subTables[i] = factory.read(type, raf, offset + subTableOffsets[i]);
			}
		}
	}

	public int getType() {
		return type;
	}

	public int getSubtableCount() {
		return subTableCount;
	}

	public LookupSubtable getSubtable(int i) {
		return subTables[i];
	}

}
