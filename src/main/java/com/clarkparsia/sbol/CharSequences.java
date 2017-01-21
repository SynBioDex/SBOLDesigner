/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sbol;

/**
 * Char sequence utility functions.
 * 
 * @author Evren Sirin
 */
public class CharSequences {
	private CharSequences() {}
	
	public static CharSequence shorten(final CharSequence seq, final int maxLength) {
		int length = seq.length();
		if (length < maxLength) {
			return seq;
		}
		else {
			int endLength = maxLength / 3;
			int startLength = maxLength - endLength - 1;
			return seq.subSequence(0, startLength).toString() + '\u2026' + seq.subSequence(length - endLength, length);
		}
	}
	
	public static CharSequence toUpperCase(final CharSequence seq) {
		return new TransformingCharSequence(seq, UPPER_CASE);
	}
	
	public static CharSequence toLowerCase(final CharSequence seq) {
		return new TransformingCharSequence(seq, LOWER_CASE);
	}
	
	public static CharSequence complement(final CharSequence seq) {
		return new TransformingCharSequence(seq, COMPLEMENT);
	}
	
	public static CharSequence reverseComplement(final CharSequence seq) {
		final int maxIndex = seq.length() - 1;
		CharSequenceTransformer transformer = new ComplementTransformer() {
			@Override
			public int transformIndex(int index) {
				return maxIndex - index;
			}
		};
		return new TransformingCharSequence(seq, transformer);
	}

	private static final CharSequenceTransformer UPPER_CASE = new CharSequenceTransformer() {
		public char transformChar(char c) {
			return Character.toUpperCase(c);
		}
	};

	private static final CharSequenceTransformer LOWER_CASE = new CharSequenceTransformer() {
		public char transformChar(char c) {
			return Character.toLowerCase(c);
		}
	};

	private static final CharSequenceTransformer COMPLEMENT = new ComplementTransformer();
	
	private static class CharSequenceTransformer {
		public char transformChar(char c) {
			return c;
		}
		
		public int transformIndex(int index) {
			return index;
		}
	}
	
	private static class ComplementTransformer extends CharSequenceTransformer {
		public char transformChar(char c) {
			char upper = Character.toUpperCase(c);
			char complement = transformUppercase(upper);
			return c == upper ? complement : Character.toLowerCase(complement); 
		}
		
		private char transformUppercase(char c) {	
			switch (c) {
				case 'A': return 'T';
				case 'T': return 'A';
				case 'U': return 'A';
				case 'G': return 'C';
				case 'C': return 'G';
				case 'Y': return 'R';
				case 'R': return 'Y';
				case 'S': return 'S';
				case 'W': return 'W';
				case 'K': return 'M';
				case 'M': return 'K';
				case 'B': return 'V';
				case 'D': return 'H';
				case 'H': return 'D';
				case 'V': return 'B';
				case 'N': return 'N';	
				default: return c;
			}
		}
	};
	
	private static class TransformingCharSequence implements CharSequence {
		private final CharSequence baseSeq;
		private final CharSequenceTransformer transformer;
		
		public TransformingCharSequence(CharSequence baseSeq, CharSequenceTransformer transformer) {
	        this.baseSeq = baseSeq;
	        this.transformer = transformer;
        }

		@Override
        public char charAt(int index) {
			int newIndex = transformer.transformIndex(index);
	        char ch = baseSeq.charAt(newIndex);
			return transformer.transformChar(ch);
        }

		@Override
        public int length() {
	        return baseSeq.length();
        }

		@Override
        public CharSequence subSequence(int start, int end) {
			int newStart = transformer.transformIndex(start);
			int newEnd = transformer.transformIndex(end);
			CharSequence newBaseSeq = baseSeq.subSequence(newStart, newEnd);
	        return new TransformingCharSequence(newBaseSeq, transformer);
        }

		@Override
        public String toString() {
			int length = length();
	        StringBuilder sb = new StringBuilder(length);
	        for (int i = 0; i < length; i++) {
	        	sb.append(charAt(i));
	        }
	        return sb.toString();
        }
		
	}

	public static String toTitleCase(String input) {
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;
	
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			}
			else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}
			else {
				c = Character.toLowerCase(c);
			}
	
			titleCase.append(c);
		}
	
		return titleCase.toString();
	}
}
