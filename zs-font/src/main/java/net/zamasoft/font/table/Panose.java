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

import java.io.Serializable;

/**
 * @version $Id: Panose.java 1034 2013-10-23 05:51:57Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class Panose implements Serializable {
	private static final long serialVersionUID = 0L;

	public final byte[] code;

	/** Creates new Panose */
	public Panose(byte[] panose) {
		assert panose.length == 10;
		this.code = panose;
	}

	public byte getFamilyType() {
		return this.code[0];
	}

	public byte getSerifStyle() {
		return this.code[1];
	}

	public byte getWeight() {
		return this.code[2];
	}

	public byte getProportion() {
		return this.code[3];
	}

	public byte getContrast() {
		return this.code[4];
	}

	public byte getStrokeVariation() {
		return this.code[5];
	}

	public byte getArmStyle() {
		return this.code[6];
	}

	public byte getLetterForm() {
		return this.code[7];
	}

	public byte getMidline() {
		return this.code[8];
	}

	public byte getXHeight() {
		return this.code[9];
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(this.code[0]));
		for (int i = 1; i < this.code.length; ++i) {
			sb.append(' ').append(String.valueOf(this.code[i]));
		}
		return sb.toString();
	}
}
