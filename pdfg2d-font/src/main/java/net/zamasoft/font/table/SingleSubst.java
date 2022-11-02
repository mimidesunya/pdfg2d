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
import java.io.Serializable;

/**
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public abstract class SingleSubst extends LookupSubtable implements Serializable {
	private static final long serialVersionUID = 0L;

	public abstract int getFormat();

	public abstract int substitute(int glyphId);

	public static SingleSubst read(RandomAccessFile raf, int offset) throws IOException {
		SingleSubst s = null;
		synchronized (raf) {
			raf.seek(offset);
			int format = raf.readUnsignedShort();
			if (format == 1) {
				s = new SingleSubstFormat1(raf, offset);
			} else if (format == 2) {
				s = new SingleSubstFormat2(raf, offset);
			}
		}
		return s;
	}

}
