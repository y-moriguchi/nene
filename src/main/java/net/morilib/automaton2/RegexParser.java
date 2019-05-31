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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser of regular expression.
 */
public final class RegexParser {

	private static class Result {

		private AbstractBuiltNFA nfa;
		private int lastIndex;

		private Result(int lastIndex, AbstractBuiltNFA nfa) {
			this.lastIndex = lastIndex;
			this.nfa = nfa;
		}

	}

	private static final String METACHARACTERS = "|()";
	private static final Pattern CHARSET = Pattern.compile("\\[(?:\\\\.|[^\\[\\]])+\\]");
	private static final Pattern DOT = Pattern.compile(".");

	private static Result parseAlternation(String match, int index) throws RegexParseException {
		Result result = parseSequence(match, index);

		if(result.lastIndex < match.length() && match.charAt(result.lastIndex) == '|') {
			List<AbstractBuiltNFA> nfas = new ArrayList<AbstractBuiltNFA>();
			int indexNew = result.lastIndex;

			nfas.add(result.nfa);
			while(result.lastIndex < match.length() && match.charAt(indexNew) == '|') {
				result = parseSequence(match, indexNew + 1);
				nfas.add(result.nfa);
				indexNew = result.lastIndex;
			}
			return new Result(result.lastIndex, new AlternationNFA(nfas));
		} else {
			return result;
		}
	}

	private static Result parseSequence(String match, int index) throws RegexParseException {
		List<AbstractBuiltNFA> nfas = new ArrayList<AbstractBuiltNFA>();
		int lastIndex = index;

		do {
			Result result = parseRepetition(match, lastIndex);

			nfas.add(result.nfa);
			lastIndex = result.lastIndex;
		} while(lastIndex < match.length() && METACHARACTERS.indexOf(match.charAt(lastIndex)) < 0);

		if(nfas.size() == 0) {
			return new Result(lastIndex, AbstractBuiltNFA.NULL_NFA);
		} else if(nfas.size() == 1) {
			return new Result(lastIndex, nfas.get(0));
		} else {
			return new Result(lastIndex, new ConcatenationNFA(nfas));
		}
	}

	private static Result parseRepetition(String match, int index) throws RegexParseException {
		Result result = parseCharacter(match, index);

		if(result.lastIndex >= match.length()) {
			return result;
		} else {
			switch(match.charAt(result.lastIndex)) {
			case '*':
				return new Result(result.lastIndex + 1, new RepetitionNFA(result.nfa, true));
			case '+':
				return new Result(result.lastIndex + 1, new RepetitionNFA(result.nfa, false));
			default:
				return result;
			}
		}
	}

	private static Result parseCharacter(String match, int index) throws RegexParseException {
		if(index >= match.length()) {
			throw new RegexParseException();
		}

		char aChar = match.charAt(index);
		if(aChar == '(') {
			Result result = parseAlternation(match, index + 1);

			if(result.lastIndex >= match.length() || match.charAt(result.lastIndex) != ')') {
				throw new RegexParseException();
			}
			return new Result(result.lastIndex + 1, result.nfa);
		} else if(aChar == '.') {
			return new Result(index + 1, new SingletonNFA(
					ch -> DOT.matcher(String.valueOf(ch)).matches()));
		}

		String aString = match.substring(index);
		Matcher matcher;
		if((matcher = CHARSET.matcher(aString)).lookingAt()) {
			Pattern aPattern = Pattern.compile(matcher.group());

			return new Result(index + matcher.end(), new SingletonNFA(
					ch -> aPattern.matcher(String.valueOf(ch)).matches()));
		} else {
			return new Result(index + 1, new SingletonNFA(ch -> ch == match.charAt(index)));
		}
	}

	/**
	 * parses the given regular expression.
	 *
	 * @param regex regular expression to parse
	 * @return constructed NFA
	 * @throws RegexParseException syntax error
	 */
	public static AbstractBuiltNFA parse(String regex) throws RegexParseException {
		Result result = parseAlternation(regex, 0);

		return result.nfa;
	}

}
