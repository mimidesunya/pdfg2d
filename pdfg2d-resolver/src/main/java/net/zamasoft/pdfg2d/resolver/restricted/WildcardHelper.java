/*
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 * include  the following  acknowledgment:  "This product includes  software
 * developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 * Alternately, this  acknowledgment may  appear in the software itself,  if
 * and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
 * used to  endorse or promote  products derived from  this software without
 * prior written permission. For written permission, please contact
 * apache@apache.org.
 *
 * 5. Products  derived from this software may not  be called "Apache", nor may
 * "Apache" appear  in their name,  without prior written permission  of the
 * Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package net.zamasoft.pdfg2d.resolver.restricted;

import java.util.Objects;

/**
 * Utility class for wildcard pattern matching.
 */
class WildcardHelper {

	static final int MATCH_FILE = -1;
	static final int MATCH_PATH = -2;
	static final int MATCH_BEGIN = -4;
	static final int MATCH_THEEND = -5;
	static final int MATCH_END = -3;

	static int[] compilePattern(String data) {
		Objects.requireNonNull(data);

		int[] expr = new int[data.length() + 2];
		char[] buff = data.toCharArray();

		int y = 0;
		boolean slash = false;

		expr[y++] = MATCH_BEGIN;

		if (buff.length > 0) {
			if (buff[0] == '\\') {
				slash = true;
			} else if (buff[0] == '*') {
				expr[y++] = MATCH_FILE;
			} else {
				expr[y++] = buff[0];
			}

			for (int x = 1; x < buff.length; x++) {
				if (slash) {
					expr[y++] = buff[x];
					slash = false;
				} else {
					if (buff[x] == '\\') {
						slash = true;
					} else if (buff[x] == '*') {
						if (expr[y - 1] <= MATCH_FILE) {
							expr[y - 1] = MATCH_PATH;
						} else {
							expr[y++] = MATCH_FILE;
						}
					} else {
						expr[y++] = buff[x];
					}
				}
			}
		}

		expr[y] = MATCH_THEEND;
		return expr;
	}

	static boolean match(String data, int[] expr) {
		Objects.requireNonNull(data);
		Objects.requireNonNull(expr);

		char[] buff = data.toCharArray();
		char[] rslt = new char[expr.length + buff.length];

		int charpos = 0;
		int exprpos = 0;
		int buffpos = 0;
		int rsltpos = 0;
		int offset = -1;

		boolean matchBegin = false;
		if (expr[charpos] == MATCH_BEGIN) {
			matchBegin = true;
			exprpos = ++charpos;
		}

		while (expr[charpos] >= 0)
			charpos++;

		int exprchr = expr[charpos];

		while (true) {
			if (matchBegin) {
				if (!matchArray(expr, exprpos, charpos, buff, buffpos))
					return false;
				matchBegin = false;
			} else {
				offset = indexOfArray(expr, exprpos, charpos, buff, buffpos);
				if (offset < 0)
					return false;
			}

			if (matchBegin) {
				if (offset != 0)
					return false;
				matchBegin = false;
			}

			buffpos += (charpos - exprpos);

			if (exprchr == MATCH_END) {
				return true;
			} else if (exprchr == MATCH_THEEND) {
				return (buffpos == buff.length);
			}

			exprpos = ++charpos;
			while (expr[charpos] >= 0)
				charpos++;
			int prevchr = exprchr;
			exprchr = expr[charpos];

			offset = (prevchr == MATCH_FILE) ? indexOfArray(expr, exprpos, charpos, buff, buffpos)
					: lastIndexOfArray(expr, exprpos, charpos, buff, buffpos);

			if (offset < 0)
				return false;

			if (prevchr == MATCH_PATH) {
				while (buffpos < offset)
					rslt[rsltpos++] = buff[buffpos++];
			} else {
				while (buffpos < offset) {
					if (buff[buffpos] == '/')
						return false;
					rslt[rsltpos++] = buff[buffpos++];
				}
			}

			rsltpos = 0;
		}
	}

	protected static int indexOfArray(int[] r, int rpos, int rend, char[] d, int dpos) {
		if (rend < rpos)
			throw new IllegalArgumentException("rend < rpos");
		if (rend == rpos)
			return d.length; // Was d.length in original.
		if ((rend - rpos) == 1) {
			for (int x = dpos; x < d.length; x++)
				if (r[rpos] == d[x])
					return x;
		}
		while ((dpos + rend - rpos) <= d.length) {
			int y = dpos;
			for (int x = rpos; x <= rend; x++) {
				if (x == rend)
					return dpos;
				if (r[x] != d[y++])
					break;
			}
			dpos++;
		}
		return -1;
	}

	protected static int lastIndexOfArray(int[] r, int rpos, int rend, char[] d, int dpos) {
		if (rend < rpos)
			throw new IllegalArgumentException("rend < rpos");
		if (rend == rpos)
			return d.length;

		if ((rend - rpos) == 1) {
			for (int x = d.length - 1; x > dpos; x--)
				if (r[rpos] == d[x])
					return x;
		}

		int l = d.length - (rend - rpos);
		while (l >= dpos) {
			int y = l;
			for (int x = rpos; x <= rend; x++) {
				if (x == rend)
					return l;
				if (r[x] != d[y++])
					break;
			}
			l--;
		}
		return -1;
	}

	protected static boolean matchArray(int[] r, int rpos, int rend, char[] d, int dpos) {
		if (d.length - dpos < rend - rpos)
			return false;
		for (int i = rpos; i < rend; i++)
			if (r[i] != d[dpos++])
				return false;
		return true;
	}
}
