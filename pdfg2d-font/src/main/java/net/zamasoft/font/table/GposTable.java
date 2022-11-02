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

/**
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public class GposTable implements Table, LookupSubtableFactory {

	private ScriptList scriptList;

	private FeatureList featureList;

	private LookupList lookupList;

	protected GposTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());

			// GPOS Header
			/* int version = */raf.readInt();
			int scriptListOffset = raf.readUnsignedShort();
			int featureListOffset = raf.readUnsignedShort();
			int lookupListOffset = raf.readUnsignedShort();

			// Script List
			this.scriptList = new ScriptList(raf, de.getOffset() + scriptListOffset);

			// Feature List
			this.featureList = new FeatureList(raf, de.getOffset() + featureListOffset);

			// Lookup List
			this.lookupList = new LookupList(raf, de.getOffset() + lookupListOffset, this);
		}
	}

	public LookupSubtable read(int type, RandomAccessFile raf, int offset) throws IOException {
		LookupSubtable s = null;
		switch (type) {
		case 1:
			// s = SingleAdj.read(raf, offset);
			break;
		case 2:
			// s = PairAdj.read(raf, offset);
			break;
		}
		return s;
	}

	/**
	 * Get the table type, as a table directory value.
	 * 
	 * @return The table type
	 */
	public int getType() {
		return GPOS;
	}

	public ScriptList getScriptList() {
		return this.scriptList;
	}

	public FeatureList getFeatureList() {
		return this.featureList;
	}

	public LookupList getLookupList() {
		return this.lookupList;
	}

	public String toString() {
		return "GPOS";
	}

}
