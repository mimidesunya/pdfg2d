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
public abstract class LigatureSubst extends LookupSubtable {

	public static LigatureSubst read(RandomAccessFile raf, int offset) throws IOException {
		LigatureSubst ls = null;
		synchronized (raf) {
			raf.seek(offset);
			int format = raf.readUnsignedShort();
			if (format == 1) {
				ls = new LigatureSubstFormat1(raf, offset);
			}
		}
		return ls;
	}

}
