/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
 include  the following  acknowledgment:  "This product includes  software
 developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 Alternately, this  acknowledgment may  appear in the software itself,  if
 and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
 used to  endorse or promote  products derived from  this software without
 prior written permission. For written permission, please contact
 apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
 "Apache" appear  in their name,  without prior written permission  of the
 Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
//package org.apache.cocoon.matching.helpers;
package jp.cssj.resolver.restricted;

/**
 * This class is an utility class that perform wilcard-patterns matching and
 * isolation.
 * 
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli </a> (Apache
 *         Software Foundation)
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati </a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi </a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch </a>
 * @since 1.0
 */
class WildcardHelper {

	/** The int representing '*' in the pattern <code>int []</code>. */
	protected static final int MATCH_FILE = -1;

	/** The int representing '**' in the pattern <code>int []</code>. */
	protected static final int MATCH_PATH = -2;

	/** The int representing begin in the pattern <code>int []</code>. */
	protected static final int MATCH_BEGIN = -4;

	/** The int representing end in pattern <code>int []</code>. */
	protected static final int MATCH_THEEND = -5;

	/** The int value that terminates the pattern <code>int []</code>. */
	protected static final int MATCH_END = -3;

	/**
	 * Translate the given <code>String</code> into a <code>int []</code>
	 * representing the pattern matchable by this class. <br>
	 * This function translates a <code>String</code> into an int array
	 * converting the special '*' and '\' characters. <br>
	 * Here is how the conversion algorithm works:
	 * <ul>
	 * <li>The '*' character is converted to MATCH_FILE, meaning that zero or
	 * more characters (excluding the path separator '/') are to be matched.</li>
	 * <li>The '**' sequence is converted to MATCH_PATH, meaning that zero or
	 * more characters (including the path separator '/') are to be matched.</li>
	 * <li>The '\' character is used as an escape sequence ('\*' is translated
	 * in '*', not in MATCH_FILE). If an exact '\' character is to be matched
	 * the source string must contain a '\\'. sequence.</li>
	 * </ul>
	 * When more than two '*' characters, not separated by another character,
	 * are found their value is considered as '**' (MATCH_PATH). <br>
	 * The array is always terminated by a special value (MATCH_END). <br>
	 * All MATCH* values are less than zero, while normal characters are equal
	 * or greater.
	 * 
	 * @param data
	 *            The string to translate.
	 * @return The encoded string as an int array, terminated by the MATCH_END
	 *         value (don't consider the array length).
	 * @exception NullPointerException
	 *                If data is null.
	 */
	public static int[] compilePattern(String data) throws NullPointerException {

		// Prepare the arrays
		int expr[] = new int[data.length() + 2];
		char buff[] = data.toCharArray();

		// Prepare variables for the translation loop
		int y = 0;
		boolean slash = false;

		// Must start from beginning
		expr[y++] = MATCH_BEGIN;

		if (buff.length > 0) {
			if (buff[0] == '\\') {
				slash = true;
			} else if (buff[0] == '*') {
				expr[y++] = MATCH_FILE;
			} else {
				expr[y++] = buff[0];
			}

			// Main translation loop
			for (int x = 1; x < buff.length; x++) {
				// If the previous char was '\' simply copy this char.
				if (slash) {
					expr[y++] = buff[x];
					slash = false;
					// If the previous char was not '\' we have to do a bunch of
					// checks
				} else {
					// If this char is '\' declare that and continue
					if (buff[x] == '\\') {
						slash = true;
						// If this char is '*' check the previous one
					} else if (buff[x] == '*') {
						// If the previous character als was '*' match a path
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

		// Must match end at the end
		expr[y] = MATCH_THEEND;
		return expr;
	}

	/**
	 * match a pattern agains a string and isolates wildcard replacement into a
	 * <code>Stack</code>.
	 */
	public static boolean match(String data, int[] expr)
			throws NullPointerException {
		if (data == null)
			throw new NullPointerException("No data provided");
		if (expr == null)
			throw new NullPointerException("No pattern expression provided");

		char buff[] = data.toCharArray();
		// Allocate the result buffer
		char rslt[] = new char[expr.length + buff.length];

		// The previous and current position of the expression character
		// (MATCH_*)
		int charpos = 0;

		// The position in the expression, input, translation and result arrays
		int exprpos = 0;
		int buffpos = 0;
		int rsltpos = 0;
		int offset = -1;

		// First check for MATCH_BEGIN
		boolean matchBegin = false;
		if (expr[charpos] == MATCH_BEGIN) {
			matchBegin = true;
			exprpos = ++charpos;
		}

		// Search the fist expression character (except MATCH_BEGIN - already
		// skipped)
		while (expr[charpos] >= 0)
			charpos++;

		// The expression charater (MATCH_*)
		int exprchr = expr[charpos];

		while (true) {
			// Check if the data in the expression array before the current
			// expression character matches the data in the input buffer
			if (matchBegin) {
				if (!matchArray(expr, exprpos, charpos, buff, buffpos))
					return (false);
				matchBegin = false;
			} else {
				offset = indexOfArray(expr, exprpos, charpos, buff, buffpos);
				if (offset < 0)
					return (false);
			}

			// Check for MATCH_BEGIN
			if (matchBegin) {
				if (offset != 0)
					return (false);
				matchBegin = false;
			}

			// Advance buffpos
			buffpos += (charpos - exprpos);

			// Check for END's
			if (exprchr == MATCH_END) {
				// Don't care about rest of input buffer
				return (true);
			} else if (exprchr == MATCH_THEEND) {
				// Check that we reach buffer's end
				return (buffpos == buff.length);
			}

			// Search the next expression character
			exprpos = ++charpos;
			while (expr[charpos] >= 0)
				charpos++;
			int prevchr = exprchr;
			exprchr = expr[charpos];

			// We have here prevchr == * or **.
			offset = (prevchr == MATCH_FILE) ? indexOfArray(expr, exprpos,
					charpos, buff, buffpos) : lastIndexOfArray(expr, exprpos,
					charpos, buff, buffpos);

			if (offset < 0)
				return (false);

			// Copy the data from the source buffer into the result buffer
			// to substitute the expression character
			if (prevchr == MATCH_PATH) {
				while (buffpos < offset)
					rslt[rsltpos++] = buff[buffpos++];
			} else {
				// Matching file, don't copy '/'
				while (buffpos < offset) {
					if (buff[buffpos] == '/')
						return (false);
					rslt[rsltpos++] = buff[buffpos++];
				}
			}

			rsltpos = 0;
		}
	}

	/**
	 * Get the offset of a part of an int array within a char array. <br>
	 * This method return the index in d of the first occurrence after dpos of
	 * that part of array specified by r, starting at rpos and terminating at
	 * rend.
	 * 
	 * @param r
	 *            The array containing the data that need to be matched in d.
	 * @param rpos
	 *            The index of the first character in r to look for.
	 * @param rend
	 *            The index of the last character in r to look for plus 1.
	 * @param d
	 *            The array of char that should contain a part of r.
	 * @param dpos
	 *            The starting offset in d for the matching.
	 * @return The offset in d of the part of r matched in d or -1 if that was
	 *         not found.
	 */
	protected static int indexOfArray(int r[], int rpos, int rend, char d[],
			int dpos) {
		// Check if pos and len are legal
		if (rend < rpos)
			throw new IllegalArgumentException("rend < rpos");
		// If we need to match a zero length string return current dpos
		if (rend == rpos)
			return (d.length); // ?? dpos?
		// If we need to match a 1 char length string do it simply
		if ((rend - rpos) == 1) {
			// Search for the specified character
			for (int x = dpos; x < d.length; x++)
				if (r[rpos] == d[x])
					return (x);
		}
		// Main string matching loop. It gets executed if the characters to
		// match are less then the characters left in the d buffer
		while ((dpos + rend - rpos) <= d.length) {
			// Set current startpoint in d
			int y = dpos;
			// Check every character in d for equity. If the string is matched
			// return dpos
			for (int x = rpos; x <= rend; x++) {
				if (x == rend)
					return (dpos);
				if (r[x] != d[y++])
					break;
			}
			// Increase dpos to search for the same string at next offset
			dpos++;
		}
		// The remaining chars in d buffer were not enough or the string
		// wasn't matched
		return (-1);
	}

	/**
	 * Get the offset of a last occurance of an int array within a char array. <br>
	 * This method return the index in d of the last occurrence after dpos of
	 * that part of array specified by r, starting at rpos and terminating at
	 * rend.
	 * 
	 * @param r
	 *            The array containing the data that need to be matched in d.
	 * @param rpos
	 *            The index of the first character in r to look for.
	 * @param rend
	 *            The index of the last character in r to look for plus 1.
	 * @param d
	 *            The array of char that should contain a part of r.
	 * @param dpos
	 *            The starting offset in d for the matching.
	 * @return The offset in d of the last part of r matched in d or -1 if that
	 *         was not found.
	 */
	protected static int lastIndexOfArray(int r[], int rpos, int rend,
			char d[], int dpos) {
		// Check if pos and len are legal
		if (rend < rpos)
			throw new IllegalArgumentException("rend < rpos");
		// If we need to match a zero length string return current dpos
		if (rend == rpos)
			return (d.length); // ?? dpos?

		// If we need to match a 1 char length string do it simply
		if ((rend - rpos) == 1) {
			// Search for the specified character
			for (int x = d.length - 1; x > dpos; x--)
				if (r[rpos] == d[x])
					return (x);
		}

		// Main string matching loop. It gets executed if the characters to
		// match are less then the characters left in the d buffer
		int l = d.length - (rend - rpos);
		while (l >= dpos) {
			// Set current startpoint in d
			int y = l;
			// Check every character in d for equity. If the string is matched
			// return dpos
			for (int x = rpos; x <= rend; x++) {
				if (x == rend)
					return (l);
				if (r[x] != d[y++])
					break;
			}
			// Decrease l to search for the same string at next offset
			l--;
		}
		// The remaining chars in d buffer were not enough or the string
		// wasn't matched
		return (-1);
	}

	/**
	 * Matches elements of array r from rpos to rend with array d, starting from
	 * dpos. <br>
	 * This method return true if elements of array r from rpos to rend equals
	 * elements of array d starting from dpos to dpos+(rend-rpos).
	 * 
	 * @param r
	 *            The array containing the data that need to be matched in d.
	 * @param rpos
	 *            The index of the first character in r to look for.
	 * @param d
	 *            The array of char that should start from a part of r.
	 * @param dpos
	 *            The starting offset in d for the matching.
	 * @return true if array d starts from portion of array r.
	 */
	protected static boolean matchArray(int r[], int rpos, int rend, char d[],
			int dpos) {
		if (d.length - dpos < rend - rpos)
			return (false);
		for (int i = rpos; i < rend; i++)
			if (r[i] != d[dpos++])
				return (false);
		return (true);
	}
}