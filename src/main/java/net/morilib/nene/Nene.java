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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.morilib.automaton2.AbstractBuiltNFA;
import net.morilib.automaton2.RegexParseException;
import net.morilib.automaton2.RegexParser;

/**
 * Body of parsing library.
 *
 * @param <A> type of attribute
 */
public class Nene<A> {

	/**
	 * Result index and attribute.
	 *
	 * @param <A> type of attribute
	 */
	public static class Result<A> {

		/**
		 * Last index of matching.
		 */
		public final int index;

		/**
		 * Result attribute.
		 */
		public final A attr;

		private Result(int index, A attr) {
			this.index = index;
			this.attr = attr;
		}

	}

	/**
	 * Parsing executer.
	 *
	 * @param <A> type of attribute
	 */
	@FunctionalInterface
	public static interface Executer<A> {

		/**
		 * matches the given input.
		 *
		 * @param match state of input
		 * @param index first index of input
		 * @param attr inherited attribute
		 * @return result of matching or null if pattern is not matched
		 * @throws IOException I/O exception
		 */
		public Result<A> match(MatchInfo<A> match, int index, A attr) throws IOException;

	}

	/**
	 * Builder of parsing executer.
	 *
	 * @param <A> type of attribute
	 */
	public static abstract class Builder<A> {

		/*package*/ abstract Executer<A> build();

		/**
		 * starts matching with the given reader and initial attribute.
		 *
		 * @param reader reader to match
		 * @param attr initial attribute
		 * @return result of matching or null if pattern is not matched
		 * @throws IOException I/O exception
		 */
		public abstract Result<A> match(Reader reader, A attr) throws IOException;

		/**
		 * starts matching with the given string and inital attribute
		 *
		 * @param string string to match
		 * @param attr initial attribute
		 * @return result of matching or null if pattern is not matched
		 */
		public Result<A> match(String string, A attr) {
			StringReader reader = new StringReader(string);

			try {
				return match(reader, attr);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Builder of clause then.
	 *
	 * @param <A> type of attribute
	 */
	public static abstract class ThenBuilder<A> extends Builder<A> {

		/**
		 * builds matcher which succeeds the given regular expression.
		 *
		 * @param regex regular expression to match
		 * @return this instance
		 */
		public abstract ThenBuilder<A> then(String regex);

		/**
		 * builds matcher which succeeds the given executer.
		 *
		 * @param executer executer to match
		 * @return this instance
		 */
		public abstract ThenBuilder<A> then(Executer<A> executer);

		/**
		 * builds matcher which succeeds the given builder.
		 *
		 * @param builder builder to match
		 * @return this instance
		 */
		public abstract ThenBuilder<A> then(Builder<A> builder);

	}

	/**
	 * Builder of clause orElse.
	 *
	 * @param <A> type of attribute
	 */
	public static abstract class ConditionBuilder<A> extends Builder<A> {

		/*package*/ abstract ConditionBuilder<A> orElse(ConditionMatcher fa, Builder<A> ifTrue);

		/**
		 * builds matcher which matches the given builder if the matcher is not matched.
		 *
		 * @param builder builder to match
		 * @return this instance
		 */
		public abstract ConditionBuilder<A> orElse(Builder<A> builder);

		/**
		 * builds matcher which matches the given regular expression if the matcher is not matched.
		 *
		 * @param regex regular expression to match
		 * @return this instance
		 */
		public abstract ConditionBuilder<A> orElse(String regex);

		/**
		 * builds matcher which matches the regular expression given by second argument
		 * if the regular expression given by first argument is matched.
		 *
		 * @param regex testing regular expression
		 * @param ifTrue regular expression to match
		 * @return this instance
		 */
		public abstract ConditionBuilder<A> orElse(String regex, String ifTrue);

		/**
		 * builds matcher which matches the builder given by second argument
		 * if the regular expression given by first argument is matched.
		 *
		 * @param regex testing regular expression
		 * @param ifTrue builder to match
		 * @return this instance
		 */
		public ConditionBuilder<A> orElse(String regex, Builder<A> ifTrue) {
			return orElse(compileRegex(regex), ifTrue);
		}

	}

	private final class InnerThenBuilder extends ThenBuilder<A> {

		private List<Executer<A>> executers = new ArrayList<Executer<A>>();

		private InnerThenBuilder() {}

		public ThenBuilder<A> then(String regex) {
			final ConditionMatcher fa = compileRegex(regex);

			executers.add((match, index, attr) -> {
				ConditionResult matched = fa.run(match.match);
				if(matched.matched) {
					return new Result<A>(index + matched.scanned.length(), attr);
				} else {
					match.match.backtrack(matched.scanned);
					return null;
				}
			});
			return this;
		}

		@Override
		public ThenBuilder<A> then(Builder<A> builder) {
			executers.add(builder.build());
			return this;
		}

		@Override
		public ThenBuilder<A> then(Executer<A> executer) {
			executers.add(executer);
			return this;
		}

		@Override
		/*package*/ Executer<A> build() {
			final List<Executer<A>> executers = new ArrayList<Executer<A>>(this.executers);
			return (match, index, attr) -> {
				Result<A> result = new Result<A>(index, attr);

				for(Executer<A> executer : executers) {
					if((result = executer.match(match, result.index, result.attr)) == null) {
						return null;
					}
				}
				return result;
			};
		}

		@Override
		public Result<A> match(Reader reader, A attr) throws IOException {
			return build().match(new MatchInfo<A>(new Input<A>(Nene.this, reader)), 0, attr);
		}

	}

	private final class InnerConditionBuilder extends ConditionBuilder<A> {

		private DelayExecuter now = null;
		private DelayExecuter last = null;

		private class DelayExecuter implements Executer<A> {

			private final ConditionMatcher fa;
			private final Executer<A> ifTrue;
			private Executer<A> orElse;

			private DelayExecuter(ConditionMatcher fa, Executer<A> ifTrue, Executer<A> orElse) {
				this.fa = fa;
				this.ifTrue = ifTrue;
				this.orElse = orElse;
			}

			private DelayExecuter(ConditionMatcher fa, Executer<A> ifTrue) {
				this(fa, ifTrue, fail.build());
			}

			@Override
			public Result<A> match(MatchInfo<A> match, int index, A attr) throws IOException {
				ConditionResult matched = fa.run(match.match);

				match.match.backtrack(matched.scanned);
				if(matched.matched) {
					return ifTrue.match(match, index, attr);
				} else {
					return orElse.match(match, index, attr);
				}
			}

		}

		private InnerConditionBuilder(ConditionMatcher fa, Executer<A> ifTrue) {
			now = last = new DelayExecuter(fa, ifTrue);
		}

		@Override
		public ConditionBuilder<A> orElse(Builder<A> orElse) {
			last.orElse = orElse.build();
			return this;
		}

		@Override
		public ConditionBuilder<A> orElse(String ifTrue) {
			return orElse(Nene.this.then(ifTrue));
		}

		@Override
		public ConditionBuilder<A> orElse(String regex, String ifTrue) {
			return orElse(regex, Nene.this.then(ifTrue));
		}

		@Override
		/*package*/ ConditionBuilder<A> orElse(ConditionMatcher fa, Builder<A> ifTrue) {
			DelayExecuter before = last;

			last = new DelayExecuter(fa, ifTrue.build());
			if(before != null) {
				before.orElse = last;
			}
			return this;
		}

		@Override
		/*package*/ Executer<A> build() {
			return now;
		}

		@Override
		public Result<A> match(Reader reader, A attr) throws IOException {
			return build().match(new MatchInfo<A>(new Input<A>(Nene.this, reader)), 0, attr);
		}

	}

	private class InnerBuilder extends Builder<A> {

		private Executer<A> executer;

		private InnerBuilder(Executer<A> executer) {
			this.executer = executer;
		}

		@Override
		/*package*/ Executer<A> build() {
			return executer;
		}

		@Override
		public Result<A> match(Reader reader, A attr) throws IOException {
			return executer.match(new MatchInfo<A>(new Input<A>(Nene.this, reader)), 0, attr);
		}

	};

	private static class Input<A> implements Sequence {

		private static final int INIT_BUFSIZE = 64;

		private Nene<A> nene;
		private Reader reader;
		private char[] buffer = new char[INIT_BUFSIZE];
		private int bufferPtr = -1;
		private int bufferMax = -1;

		private Input(Nene<A> nene, Reader reader) {
			this.nene = nene;
			this.reader = reader;
		}

		@Override
		public int read() throws IOException {
			int ch;

			if(bufferPtr >= 0) {
				ch = buffer[bufferPtr++];
				if(bufferPtr >= bufferMax) {
					bufferPtr = bufferMax = -1;
				}
				return ch;
			} else {
				return reader.read();
			}
		}

		@Override
		public void unread(int ch) {
			char[] array = new char[1];

			array[0] = (char)ch;
			backtrack(new String(array));
		}

		private void backtrack(CharSequence sequence) {
			if(bufferMax < 0) {
				bufferMax = bufferPtr = 0;
			} else {
				System.arraycopy(buffer, bufferPtr, buffer, 0, bufferMax - bufferPtr);
				bufferMax -= bufferPtr;
				bufferPtr = 0;
			}

			if(sequence.length() - bufferMax < buffer.length) {
				char[] seqchars;

				seqchars = sequence.toString().toCharArray();
				System.arraycopy(buffer, 0, buffer, seqchars.length, bufferMax);
				System.arraycopy(seqchars, 0, buffer, 0, seqchars.length);
				bufferMax += seqchars.length;
			} else {
				char[] oldBuffer;

				oldBuffer = buffer;
				if(buffer.length * 2 <= nene.maxBufferSize) {
					buffer = new char[buffer.length * 2];
				} else if(buffer.length < nene.maxBufferSize) {
					buffer = new char[nene.maxBufferSize];
				} else {
					throw new NeneException();
				}
				System.arraycopy(oldBuffer, 0, buffer, 0, bufferMax);
				backtrack(sequence);
			}
		}

	}

	/**
	 * The state of input to match.
	 *
	 * @param <A> type of attribute
	 */
	public static final class MatchInfo<A> {

		private Input<A> match;

		private MatchInfo(Input<A> match) {
			this.match = match;
		}

	}

	@FunctionalInterface
	private static interface ILetrec<T> {

		public T apply(ILetrec<T> f);

	}

	/**
	 * Builder of fail pattern.
	 */
	public final Builder<A> fail = new InnerBuilder((match, index, attr) -> null);

	/**
	 * Builder of success pattern.
	 */
	public final Builder<A> success =
			new InnerBuilder((match, index, attr) -> new Result<A>(index, attr));

	private final int maxBufferSize = 1024;

	/**
	 * creates this instance.
	 */
	public Nene() {}

	private static ConditionMatcher compileRegex(String regex) {
		AbstractBuiltNFA nfa;

		try {
			nfa = RegexParser.parse(regex);
		} catch (RegexParseException e) {
			throw new NeneException();
		}

		return sequence -> {
			StringBuilder builder = new StringBuilder();
			Set<Object> states = nfa.transitEpsilon(Collections.singleton(nfa.getStartState()));

			while(states.size() > 0) {
				int ch = sequence.read();
				Set<Object> stateNew = new HashSet<Object>();

				if(ch < 0) {
					break;
				} else {
					for(Object state : states) {
						stateNew.addAll(nfa.transit(state, (char)ch));
					}
					if(stateNew.size() > 0) {
						Set<Object> stateAdd = null;

						builder.append((char)ch);
						states = nfa.transitEpsilon(stateNew);
						while(stateAdd == null || !stateAdd.equals(states)) {
							stateAdd = states;
							states = nfa.transitEpsilon(states);
						}
					} else {
						sequence.unread((char)ch);
						break;
					}
				}
			}
			return new ConditionResult(builder.toString(), nfa.isAccept(states));
		};
	}

	/**
	 * builds matcher which matches the given regular expression.
	 *
	 * @param regex regular expression to match
	 * @return then clause bulider
	 */
	public ThenBuilder<A> then(String regex) {
		return new InnerThenBuilder().then(regex);
	}

	/**
	 * builds matcher which matches the given executer.
	 *
	 * @param executer executer to match
	 * @return then clause bulider
	 */
	public ThenBuilder<A> then(Executer<A> executer) {
		return new InnerThenBuilder().then(executer);
	}

	/**
	 * builds matcher which matches the builder given by second argument
	 * if the regular expression given by first argument is matched.
	 *
	 * @param regex testing regular expression
	 * @param builder builder to match
	 * @return orElse clause builder
	 */
	public ConditionBuilder<A> cond(String regex, Builder<A> builder) {
		ConditionMatcher fa = compileRegex(regex);

		return new InnerConditionBuilder(fa, builder.build());
	}

	/**
	 * builds matcher which matches the regular expression given by second argument
	 * if the regular expression given by first argument is matched.
	 *
	 * @param regex testing regular expression
	 * @param match regular expression to match
	 * @return orElse clause builder
	 */
	public ConditionBuilder<A> cond(String regex, String match) {
		ConditionMatcher fa = compileRegex(regex);

		return new InnerConditionBuilder(fa, then(match).build());
	}

	/**
	 * repeats the given builder.
	 *
	 * @param minCount minimum count of repetition
	 * @param maxCount maximum count of repetition and forever if the argument is negative
	 * @param builder builder to repeat
	 * @return builder of result
	 */
	public Builder<A> times(int minCount, int maxCount, Builder<A> builder) {
		final Executer<A> executer = builder.build();
		final Executer<A> result = (match, index, attr) -> {
			Result<A> before = new Result<A>(index, attr);
			Result<A> after = new Result<A>(index, attr);

			for(int i = 0; maxCount < 0 || i < maxCount; i++, before = after) {
				if((after = executer.match(match, before.index, before.attr)) == null) {
					return i >= minCount ? before : null;
				}
			}
			return after;
		};

		return new InnerBuilder(result);
	}

	/**
	 * repeats the given regular expression.
	 *
	 * @param minCount minimum count of repetition
	 * @param maxCount maximum count of repetition and forever if the argument is negative
	 * @param match regular expression to repeat
	 * @return builder of result
	 */
	public Builder<A> times(int minCount, int maxCount, String match) {
		return times(minCount, maxCount, then(match));
	}

	/**
	 * repeats the given builder at least minCount times.
	 *
	 * @param minCount minimum count of repetition
	 * @param builder builder to repeat
	 * @return builder of result
	 */
	public Builder<A> atLeast(int minCount, Builder<A> builder) {
		return times(minCount, -1, builder);
	}

	/**
	 * repeats the given regular expression at least minCount times.
	 *
	 * @param minCount minimum count of repetition
	 * @param match regular expression to repeat
	 * @return builder of result
	 */
	public Builder<A> atLeast(int minCount, String match) {
		return times(minCount, -1, match);
	}

	/**
	 * repeats the given builder at most maxCount times.
	 *
	 * @param maxCount maximum count of repetition and forever if the argument is negative
	 * @param builder builder to repeat
	 * @return builder of result
	 */
	public Builder<A> atMost(int maxCount, Builder<A> builder) {
		return times(0, maxCount, builder);
	}

	/**
	 * repeats the given regular expression at most maxCount times.
	 *
	 * @param maxCount maximum count of repetition and forever if the argument is negative
	 * @param match regular expression to repeat
	 * @return builder of result
	 */
	public Builder<A> atMost(int maxCount, String match) {
		return times(0, maxCount, match);
	}

	/**
	 * repeats the given builder zero or more times.
	 *
	 * @param builder builder to repeat
	 * @return builder of result
	 */
	public Builder<A> zeroOrMore(Builder<A> builder) {
		return times(0, -1, builder);
	}

	/**
	 * repeats the given regular expression zero or more times.
	 *
	 * @param match regular expression to repeat
	 * @return builder of result
	 */
	public Builder<A> zeroOrMore(String match) {
		return times(0, -1, match);
	}

	/**
	 * repeats the given builder one or more times.
	 *
	 * @param builder builder to repeat
	 * @return builder of result
	 */
	public Builder<A> oneOrMore(Builder<A> builder) {
		return times(1, -1, builder);
	}

	/**
	 * repeats the given regular expression one or more times.
	 *
	 * @param match regular expression to repeat
	 * @return builder of result
	 */
	public Builder<A> oneOrMore(String match) {
		return times(1, -1, match);
	}

	/**
	 * matches the given builder 0 or 1 times.
	 *
	 * @param builder builder to repeat
	 * @return
	 */
	public Builder<A> maybe(Builder<A> builder) {
		return times(0, 1, builder);
	}

	/**
	 * matches the given regular expression 0 or 1 times.
	 *
	 * @param match regular expression to repeat
	 * @return
	 */
	public Builder<A> maybe(String match) {
		return times(0, 1, match);
	}

	/**
	 * executes the given action if the given regular expression is matched.
	 *
	 * @param regex regular expression to match
	 * @param action the action
	 * @return this builder
	 */
	public Builder<A> action(String regex, BiFunction<String, A, A> action) {
		final ConditionMatcher fa = compileRegex(regex);

		return new InnerBuilder((match, index, attr) -> {
			ConditionResult matched = fa.run(match.match);
			if(matched.matched) {
				return new Result<A>(index + matched.scanned.length(), action.apply(matched.scanned, attr));
			} else {
				match.match.backtrack(matched.scanned);
				return null;
			}
		});
	}

	/**
	 * executes the given action if the given builder is matched.
	 *
	 * @param builder builder to match
	 * @param action the action
	 * @return this builder
	 */
	public Builder<A> action(Builder<A> builder, BiFunction<A, A, A> action) {
		final Executer<A> executer = builder.build();

		return new InnerBuilder((match, index, attr) -> {
			Result<A> result = executer.match(match, index, attr);

			if(result == null) {
				return null;
			} else {
				return new Result<A>(index, action.apply(result.attr, attr));
			}
		});
	}

	/**
	 * A method which can refer a return value of the function itself.<br>
	 * This method will be used for defining a pattern with recursion.
	 *
	 * @param func a function whose argument is a return value itself.
	 * @return Builder interface
	 */
	public Builder<A> letrec(final Function<Executer<A>, Builder<A>> func) {
		final ILetrec<Executer<A>> f = g -> g.apply(g);
		final ILetrec<Executer<A>> h =
				g -> func.apply((match, index, attr) -> g.apply(g).match(match, index, attr)).build();
		final Executer<A> executer = f.apply(h);

		return new InnerBuilder(executer);
	}

}
