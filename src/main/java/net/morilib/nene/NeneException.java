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
 * Exception of Nene
 */
public class NeneException extends RuntimeException {

	/**
	 * creates exception.
	 */
	public NeneException() {
		super();
	}

	/**
	 * creates exception.
	 *
	 * @param message a message
	 */
	public NeneException(String message) {
		super(message);
	}

}
