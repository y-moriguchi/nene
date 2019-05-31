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
 * An interface which represents NFA.
 *
 * @param <S> type of states
 * @param <T> type of alphabets
 */
public interface NFA<S, T> {

	/**
	 * gets the initial state.
	 *
	 * @return the initial state
	 */
	public S getStartState();

	/**
	 * gets the accept states.
	 *
	 * @return the accept state.
	 */
	public Set<S> getAcceptStates();

	/**
	 * transit by the given alphabet.
	 *
	 * @param state the state
	 * @param alphabet the alphabet
	 * @return states
	 */
	public Set<S> transit(S state, T alphabet);

	/**
	 * epsition transition.
	 *
	 * @param state the state
	 * @return states
	 */
	public Set<S> transitEpsilon(S state);

	/**
	 * returns true if one of the given states is an accepted state.
	 *
	 * @param states states to test
	 * @return true if one of the given states is an accepted state
	 */
	public default boolean isAccept(Set<S> states) {
		for(S state : states) {
			if(getAcceptStates().contains(state)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * epsition transition.
	 *
	 * @param states the state
	 * @return states
	 */
	public default Set<S> transitEpsilon(Set<S> states) {
		Set<S> result = new HashSet<S>();

		for(S state : states) {
			result.addAll(transitEpsilon(state));
		}
		return result;
	}

}
