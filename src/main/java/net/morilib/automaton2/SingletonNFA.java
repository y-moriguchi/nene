/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.automaton2;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An NFA which matches one character.
 */
public class SingletonNFA extends AbstractBuiltNFA {

	private final Object start = new Object();
	private final Object end = new Object();
	private Predicate<Character> charset;

	/**
	 * creates a singleton NFA.
	 *
	 * @param charset predicate of character
	 */
	public SingletonNFA(Predicate<Character> charset) {
		this.charset = charset;
	}

	/**
	 * creates a singleton NFA.
	 *
	 * @param ch a character to match
	 */
	public SingletonNFA(char ch) {
		this.charset = destChar -> ch == destChar;
	}

	/**
	 * creates a singleton NFA.
	 *
	 * @param setOfChar a set of characters to match
	 */
	public SingletonNFA(Set<Character> setOfChar) {
		this.charset = destChar -> setOfChar.contains(destChar);
	}

	@Override
	public Object getStartState() {
		return start;
	}

	@Override
	public Set<Object> getAcceptStates() {
		return Collections.singleton(end);
	}

	@Override
	public Set<Object> transit(Object state, Character alphabet) {
		if(state == start && charset.test(alphabet)) {
			return Collections.singleton(end);
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Set<Object> transitEpsilon(Object state) {
		return Collections.singleton(state);
	}

	@Override
	public boolean isStateOf(Object state) {
		return state == start || state == end;
	}

}
