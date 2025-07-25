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
public abstract class KernSubtable {

	/** Creates new KernSubtable */
	protected KernSubtable() {
	}

	public abstract int getKerningPairCount();

	public abstract KerningPair getKerningPair(int i);

	public static KernSubtable read(RandomAccessFile raf) throws IOException {
		KernSubtable table = null;
		/* int version = */raf.readUnsignedShort();
		/* int length = */raf.readUnsignedShort();
		int coverage = raf.readUnsignedShort();
		int format = coverage >> 8;

		switch (format) {
		case 0:
			table = new KernSubtableFormat0(raf);
			break;
		case 2:
			table = new KernSubtableFormat2(raf);
			break;
		default:
			break;
		}
		return table;
	}

}
