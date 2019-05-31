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
 * An alternation NFA.
 */
public class AlternationNFA extends AbstractBuiltNFA {

	private final Object start = new Object();
	private final Object end = new Object();
	private List<AbstractBuiltNFA> alternates = new ArrayList<AbstractBuiltNFA>();

	/**
	 * creates an alternation NFA.
	 *
	 * @param alternates alternates
	 */
	public AlternationNFA(List<AbstractBuiltNFA> alternates) {
		this.alternates = new ArrayList<AbstractBuiltNFA>(alternates);
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
		for(AbstractBuiltNFA alternate : alternates) {
			Set<Object> result = alternate.transit(state, alphabet);

			if(!result.isEmpty()) {
				return result;
			}
		}
		return Collections.emptySet();
	}

	@Override
	public Set<Object> transitEpsilon(Object state) {
		if(state == start) {
			Set<Object> result = new HashSet<Object>();

			result.add(start);
			for(AbstractBuiltNFA alternate : alternates) {
				result.addAll(alternate.transitEpsilon(alternate.getStartState()));
			}
			return result;
		} else if(state == end) {
			return Collections.singleton(end);
		} else {
			Set<Object> result = new HashSet<Object>();

			for(AbstractBuiltNFA alternate : alternates) {
				if(alternate.isStateOf(state)) {
					result = new HashSet<Object>(alternate.transitEpsilon(state));
					for(Object acceptState : alternate.getAcceptStates()) {
						if(result.contains(acceptState)) {
							result.add(end);
							break;
						}
					}
					return result;
				}
			}
			return Collections.emptySet();
		}
	}

	@Override
	public boolean isStateOf(Object state) {
		if(state == start || state == end) {
			return true;
		} else {
			for(AbstractBuiltNFA alternate : alternates) {
				if(alternate.isStateOf(state)) {
					return true;
				}
			}
			return false;
		}
	}

}
