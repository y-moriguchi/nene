/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.nene;

import java.io.IOException;

/**
 * A sequence to match.
 */
public interface Sequence {

	/**
	 * read a character from this sequence.
	 *
	 * @return a character
	 * @throws IOException I/O exception
	 */
	public int read() throws IOException;

	/**
	 * unread a character to this sequence.
	 *
	 * @param ch a character to unread
	 */
	public void unread(int ch);

}
