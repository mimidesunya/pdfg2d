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
 * @version $Id: LangSys.java 1034 2013-10-23 05:51:57Z miyabe $
 */
public class LangSys {
	private final int featureCount;

	private final int[] featureIndex;

	/** Creates new LangSys */
	protected LangSys(RandomAccessFile raf) throws IOException {
		raf.readUnsignedShort(); // lookupOrder
		raf.readUnsignedShort(); // reqFeatureIndex
		this.featureCount = raf.readUnsignedShort();
		this.featureIndex = new int[this.featureCount];
		for (int i = 0; i < this.featureCount; i++) {
			this.featureIndex[i] = raf.readUnsignedShort();
		}
	}

	protected boolean isFeatureIndexed(int n) {
		for (int i = 0; i < this.featureCount; i++) {
			if (this.featureIndex[i] == n) {
				return true;
			}
		}
		return false;
	}

}