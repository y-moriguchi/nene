/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.automaton2;

import java.util.HashSet;
import java.util.Set;

/**
 * A repetition NFA.
 */
public class RepetitionNFA extends AbstractBuiltNFA {

	private AbstractBuiltNFA nfa;
	private boolean nullable;

	/**
	 * creates a repetition NFA.
	 *
	 * @param nfa NFA to wrap
	 * @param nullable true if nullable
	 */
	public RepetitionNFA(AbstractBuiltNFA nfa, boolean nullable) {
		this.nfa = nfa;
		this.nullable = nullable;
	}

	@Override
	public Object getStartState() {
		return nfa.getStartState();
	}

	@Override
	public Set<Object> getAcceptStates() {
		return nfa.getAcceptStates();
	}

	@Override
	public Set<Object> transit(Object state, Character alphabet) {
		return nfa.transit(state, alphabet);
	}

	@Override
	public Set<Object> transitEpsilon(Object state) {
		Set<Object> result = new HashSet<Object>(nfa.transitEpsilon(state));

		if(state == nfa.getStartState() && nullable) {
			result.addAll(nfa.transitEpsilon(nfa.getAcceptStates()));
		} else if(nfa.getAcceptStates().contains(state)) {
			result.addAll(nfa.transitEpsilon(nfa.getStartState()));
		}
		return result;
	}

	@Override
	public boolean isStateOf(Object state) {
		return nfa.isStateOf(state);
	}

}
