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
 * @version $Id: LookupList.java 1034 2013-10-23 05:51:57Z miyabe $
 */
public class LookupList {
	private final int lookupCount;

	private final int[] lookupOffsets;

	private final Lookup[] lookups;

	/** Creates new LookupList */
	public LookupList(RandomAccessFile raf, int offset, LookupSubtableFactory factory) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			this.lookupCount = raf.readUnsignedShort();
			this.lookupOffsets = new int[this.lookupCount];
			this.lookups = new Lookup[this.lookupCount];
			for (int i = 0; i < this.lookupCount; i++) {
				this.lookupOffsets[i] = raf.readUnsignedShort();
			}
			for (int i = 0; i < this.lookupCount; i++) {
				this.lookups[i] = new Lookup(factory, raf, offset + this.lookupOffsets[i]);
			}
		}
	}

	public Lookup getLookup(Feature feature, int index) {
		if (feature.getLookupCount() > index) {
			int i = feature.getLookupListIndex(index);
			return this.lookups[i];
		}
		return null;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer(this.lookupCount + ":");
		for (int i = 0; i < this.lookups.length; i++) {
			int tag = this.lookups[i].getType();
			buff.append((char) ((tag >> 24) & 0xff)).append((char) ((tag >> 16) & 0xff))
					.append((char) ((tag >> 8) & 0xff)).append((char) ((tag) & 0xff)).append('/');
		}
		return buff.toString();
	}
}
