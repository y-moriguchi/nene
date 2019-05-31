/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.nene;

/**
 * Result of matched pattern.
 */
public class ConditionResult {

	/**
	 * scanned string.
	 */
	public final String scanned;

	/**
	 * true if a pattern is matched.
	 */
	public final boolean matched;

	/**
	 * creates result of matched pattern.
	 *
	 * @param scanned scanned string
	 * @param matched matched
	 */
	public ConditionResult(String scanned, boolean matched) {
		this.scanned = scanned;
		this.matched = matched;
	}

}
