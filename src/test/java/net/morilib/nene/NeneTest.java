/*
 * Nene
 *
 * Copyright (c) 2019 Yuichiro MORIGUCHI
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package net.morilib.nene;

import junit.framework.TestCase;

public class NeneTest extends TestCase {

	public void testSimple001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("765").match("765", 0.0).index, 3);
		assertNull(n.then("765").match("961", 0.0));
		assertNull(n.then("765").match("", 0.0));
	}

	public void testSimple002() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("a*").match("ab", 0.0).index, 1);
		assertEquals(n.then("a*").match("aaab", 0.0).index, 3);
		assertEquals(n.then("a*").match("b", 0.0).index, 0);
		assertEquals(n.then("a+").match("ab", 0.0).index, 1);
		assertEquals(n.then("a+").match("aaab", 0.0).index, 3);
		assertNull(n.then("a+").match("b", 0.0));
		assertEquals(n.then("ab+").match("abb", 0.0).index, 3);
		assertEquals(n.then("ab+").match("abab", 0.0).index, 2);
	}

	public void testSimple003() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("765|346").match("765", 0.0).index, 3);
		assertEquals(n.then("765|346").match("346", 0.0).index, 3);
		assertNull(n.then("765|346").match("961", 0.0));
		assertNull(n.then("765|346").match("", 0.0));
	}

	public void testSimple004() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then(".").match("a", 0.0).index, 1);
		assertNull(n.then(".").match("\n", 0.0));
		assertNull(n.then(".").match("", 0.0));
	}

	public void testSimple005() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("[a-zA-Z\\[\\]]").match("i", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("I", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("a", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("A", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("z", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("Z", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("[", 0.0).index, 1);
		assertEquals(n.then("[a-zA-Z\\[\\]]").match("]", 0.0).index, 1);
		assertNull(n.then("[a-zA-Z\\[\\]]").match("1", 0.0));
		assertNull(n.then("[a-zA-Z\\[\\]]").match("", 0.0));
	}

	public void testSimple006() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("(7|3)o").match("7o", 0.0).index, 2);
		assertEquals(n.then("(7|3)o").match("3o", 0.0).index, 2);
		assertNull(n.then("(7|3)pro").match("9o", 0.0));
		assertNull(n.then("(7|3)pro").match("o", 0.0));
		assertNull(n.then("(7|3)pro").match("", 0.0));
	}

	public void testSimple007() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("(ab)*").match("ab", 0.0).index, 2);
		assertEquals(n.then("(ab)*").match("ababab", 0.0).index, 6);
		assertEquals(n.then("(ab)*").match("b", 0.0).index, 0);
		assertEquals(n.then("(ab)+").match("ab", 0.0).index, 2);
		assertEquals(n.then("(ab)+").match("ababab", 0.0).index, 6);
		assertNull(n.then("(ab)+").match("b", 0.0));
		assertEquals(n.then("(ab)+").match("abbb", 0.0).index, 2);
	}

	public void testThen001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.then("765").then("pro").match("765pro", 0.0).index, 6);
		assertNull(n.then("765").then("pro").match("961pro", 0.0));
		assertNull(n.then("765").then("pro").match("765aaa", 0.0));
		assertNull(n.then("765").then("pro").match("", 0.0));
	}

	public void testOrElse001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("765pro", 0.0).index, 6);
		assertEquals(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("27", 0.0).index, 2);
		assertNull(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("765?", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("961pro", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("961", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse(n.then("27")).match("", 0.0));
	}

	public void testOrElse002() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.cond("765", n.then("765pro")).orElse("27", n.then("27chan")).match("765pro", 0.0).index, 6);
		assertEquals(n.cond("765", n.then("765pro")).orElse("27", "27chan").match("27chan", 0.0).index, 6);
		assertNull(n.cond("765", n.then("765pro")).orElse("27", n.then("27chan")).match("765?", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse("27", n.then("27chan")).match("961pro", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse("27", n.then("27chan")).match("27?", 0.0));
		assertNull(n.cond("765", n.then("765pro")).orElse("27", n.then("27chan")).match("", 0.0));
	}

	public void testOrElse003() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.cond("765", "765pro").orElse("27", "27chan").orElse("764", "7643").match("765pro", 0.0).index, 6);
		assertEquals(n.cond("765", "765pro").orElse("27", "27chan").orElse("764", "7643").match("27chan", 0.0).index, 6);
		assertEquals(n.cond("765", "765pro").orElse("27", "27chan").orElse("764", "7643").match("7643", 0.0).index, 4);
		assertNull(n.cond("765", "765pro").orElse("27", "27chan").orElse("764", "7643").match("764?", 0.0));
	}

	public void testTimes001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.times(1, 3, n.then("27")).match("27", 0.0).index, 2);
		assertEquals(n.times(1, 3, "27").match("272727", 0.0).index, 6);
		assertEquals(n.times(1, 3, "27").match("27272727", 0.0).index, 6);
		assertNull(n.times(1, 3, n.then("27")).match("", 0.0));
	}

	public void testZeroOrMore001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.zeroOrMore(n.then("27")).match("", 0.0).index, 0);
		assertEquals(n.zeroOrMore("27").match("272727", 0.0).index, 6);
	}

	public void testLetrec001() {
		Nene<Double> n = new Nene<Double>();

		assertEquals(n.letrec(a -> n.cond("<", n.then("<").then(a).then(">")).orElse(n.success)).match("<<<>>>", 0.0).index, 6);
		assertEquals(n.letrec(a -> n.cond("<", n.then("<").then(a).then(">")).orElse(n.success)).match("<<>>>", 0.0).index, 4);
		assertNull(n.letrec(a -> n.cond("<", n.then("<").then(a).then(">")).orElse(n.success)).match("<<<>>", 0.0));
	}

}
