/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.automaton2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A concatenated NFA.
 */
public class ConcatenationNFA extends AbstractBuiltNFA {

	private List<AbstractBuiltNFA> sequence;

	/**
	 * creates conatenated NFA
	 *
	 * @param sequence NFAs to concatenate
	 */
	public ConcatenationNFA(List<AbstractBuiltNFA> sequence) {
		if(sequence.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.sequence = new ArrayList<AbstractBuiltNFA>(sequence);
	}

	@Override
	public Object getStartState() {
		return sequence.get(0).getStartState();
	}

	@Override
	public Set<Object> getAcceptStates() {
		return sequence.get(sequence.size() - 1).getAcceptStates();
	}

	@Override
	public Set<Object> transit(Object state, Character alphabet) {
		for(AbstractBuiltNFA nfa : sequence) {
			if(nfa.isStateOf(state)) {
				return nfa.transit(state, alphabet);
			}
		}
		return Collections.emptySet();
	}

	@Override
	public Set<Object> transitEpsilon(Object state) {
		for(int i = 0; i < sequence.size(); i++) {
			AbstractBuiltNFA nfa = sequence.get(i);
			Set<Object> result = new HashSet<Object>();

			if(nfa.isStateOf(state)) {
				result.addAll(nfa.transitEpsilon(state));
				if(nfa.getAcceptStates().contains(state) && i < sequence.size() - 1) {
					for(int j = i + 1; j < sequence.size(); j++) {
						result.addAll(sequence.get(j).transitEpsilon(sequence.get(j).getStartState()));
						if(!sequence.get(j).isAccept(result)) {
							break;
						}
					}
				}
				return result;
			}
		}
		return Collections.emptySet();
	}

	@Override
	public boolean isStateOf(Object state) {
		for(AbstractBuiltNFA nfa : sequence) {
			if(nfa.isStateOf(state)) {
				return true;
			}
		}
		return false;
	}

}
