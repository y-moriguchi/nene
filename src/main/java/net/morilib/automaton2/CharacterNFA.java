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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An NFA whose class of alpabets is Character.
 *
 * @param <S> type of state
 */
public interface CharacterNFA<S> extends NFA<S, Character> {

	/**
	 * runs the NFA with the input.
	 *
	 * @param input input to match
	 * @return matched string or null if the NFA is not matched
	 * @throws IOException I/O exception
	 */
	public default String run(FAInput input) throws IOException {
		StringBuilder builder = new StringBuilder();
		Set<S> states = transitEpsilon(Collections.singleton(getStartState()));

		while(states.size() > 0) {
			char ch = (char)input.read();
			Set<S> stateNew = new HashSet<S>();

			if(ch < 0) {
				break;
			} else {
				for(S state : states) {
					stateNew.addAll(transit(state, ch));
				}
				if(stateNew.size() > 0) {
					builder.append(ch);
				} else {
					input.unread(ch);
				}
				states = transitEpsilon(stateNew);
			}
		}
		return isAccept(states) ? builder.toString() : null;
	}

}
