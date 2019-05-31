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

/**
 * An abstract class of constructed NFA.
 */
public abstract class AbstractBuiltNFA implements CharacterNFA<Object> {

	/**
	 * An NFA which matches empty language.
	 */
	public static final AbstractBuiltNFA NULL_NFA = new AbstractBuiltNFA() {

		private final Object theState = new Object();

		@Override
		public Object getStartState() {
			return theState;
		}

		@Override
		public Set<Object> getAcceptStates() {
			return Collections.singleton(theState);
		}

		@Override
		public Set<Object> transit(Object state, Character alphabet) {
			return Collections.emptySet();
		}

		@Override
		public Set<Object> transitEpsilon(Object state) {
			return Collections.emptySet();
		}

		@Override
		public boolean isStateOf(Object state) {
			return state == theState;
		}

	};

	/**
	 * returns true if the given state is in the NFA.
	 *
	 * @param state states to test
	 * @return true if the given state is in the NFA.
	 */
	public abstract boolean isStateOf(Object state);

}
