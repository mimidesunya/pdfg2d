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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @version $Id: Program.java 1034 2013-10-23 05:51:57Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class Program {

	private short[] instructions;

	public short[] getInstructions() {
		return instructions;
	}

	protected void readInstructions(RandomAccessFile raf, int count) throws IOException {
		if (count < 0) {
			return;
		}
		instructions = new short[count];
		for (int i = 0; i < count; i++) {
			instructions[i] = (short) raf.readUnsignedByte();
		}
	}

	protected void readInstructions(ByteArrayInputStream bais, int count) {
		if (count < 0) {
			return;
		}
		instructions = new short[count];
		for (int i = 0; i < count; i++) {
			instructions[i] = (short) bais.read();
		}
	}
}