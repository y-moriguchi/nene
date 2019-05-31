/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.automaton2;

import java.io.IOException;

/**
 * An input of finite automata.
 */
public interface FAInput {

	/**
	 * read a character.
	 *
	 * @return a character
	 * @throws IOException I/O exception
	 */
	public int read() throws IOException;

	/**
	 * unread a character.
	 *
	 * @param ch a character to unread
	 */
	public void unread(int ch);

}
