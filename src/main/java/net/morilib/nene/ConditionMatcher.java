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
 * Executer of condition.
 */
@FunctionalInterface
public interface ConditionMatcher {

	/**
	 * executes condition with the given input.
	 *
	 * @param input input to match
	 * @return result of exection
	 * @throws IOException I/O exception
	 */
	public ConditionResult run(Sequence input) throws IOException;

}
