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
public class LigatureSet {

	private int ligatureCount;

	private int[] ligatureOffsets;

	private Ligature[] ligatures;

	/** Creates new LigatureSet */
	public LigatureSet(RandomAccessFile raf, int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			ligatureCount = raf.readUnsignedShort();
			ligatureOffsets = new int[ligatureCount];
			ligatures = new Ligature[ligatureCount];
			for (int i = 0; i < ligatureCount; i++) {
				ligatureOffsets[i] = raf.readUnsignedShort();
			}
			for (int i = 0; i < ligatureCount; i++) {
				raf.seek(offset + ligatureOffsets[i]);
				ligatures[i] = new Ligature(raf);
			}
		}
	}

}
