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
public class GsubTable implements Table, LookupSubtableFactory {

	private ScriptList scriptList;

	private FeatureList featureList;

	private LookupList lookupList;

	protected GsubTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());

			// GSUB Header
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

	/**
	 * 1 - Single - Replace one glyph with one glyph 2 - Multiple - Replace one
	 * glyph with more than one glyph 3 - Alternate - Replace one glyph with one of
	 * many glyphs 4 - Ligature - Replace multiple glyphs with one glyph 5 - Context
	 * - Replace one or more glyphs in context 6 - Chaining - Context Replace one or
	 * more glyphs in chained context
	 */
	public LookupSubtable read(int type, RandomAccessFile raf, int offset) throws IOException {
		LookupSubtable s = null;
		switch (type) {
		case 1:
			s = SingleSubst.read(raf, offset);
			break;
		case 2:
			// s = MultipleSubst.read(raf, offset);
			break;
		case 3:
			// s = AlternateSubst.read(raf, offset);
			break;
		case 4:
			s = LigatureSubst.read(raf, offset);
			break;
		case 5:
			// s = ContextSubst.read(raf, offset);
			break;
		case 6:
			// s = ChainingSubst.read(raf, offset);
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
		return GSUB;
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
		return "GSUB";
	}

}
