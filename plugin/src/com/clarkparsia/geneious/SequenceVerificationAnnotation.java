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

package com.clarkparsia.geneious;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Representation of sequence verification annotation. 
 * 
 * @author Evren Sirin
 */
public class SequenceVerificationAnnotation {
	private static Logger LOGGER = LoggerFactory.getLogger(SequenceVerificationAnnotation.class.getName());
	
	private final SublimeAlignmentDocument alignmentDoc;
	private final SequenceAnnotationInterval interval;

	private SequenceVariantType variantType;
	private char[] substitution;
	private URI soType;
	private String identifier;
	private boolean isAmbiguous;

	public SequenceVerificationAnnotation(SequenceAnnotationInterval interval, SublimeAlignmentDocument alignmentDoc) {
		this.interval = interval;
		
		this.alignmentDoc = alignmentDoc;
    	
		variantType = guessSequenceVariant();

		identifier = generateIdentifier();

		isAmbiguous = guessAmbiguous();
	}

    public SequenceVariantType getVariantType() {
    	return variantType;
    }

	public void setVariantType(SequenceVariantType variantType) {
    	this.variantType = variantType;
    	this.identifier = generateIdentifier();
    }

	public boolean isAmbiguous() {
    	return isAmbiguous;
    }

	public void setAmbiguous(boolean isAmbiguous) {
    	this.isAmbiguous = isAmbiguous;
    }

	public SequenceAnnotationInterval getInterval() {
    	return interval;
    }

	public SublimeAlignmentDocument getAligmentDoc() {
    	return alignmentDoc;
    }

	public String getIdentifier() {
    	return identifier;
    }

	private SequenceVariantType guessSequenceVariant() {
		int start = interval.getMinimumIndex() - 1;
		int end = interval.getMaximumIndex();
		
		SequenceCharSequence expectedSequence = alignmentDoc.getDesign().getCharSequence();
		int gapCount = expectedSequence.countGaps(start, end); 
		if (gapCount == interval.getLength()) {
			// TODO check if this is duplication
			return SequenceVariantType.INSERTION;
		}

		int possibleDeletion = 0;
		for (SequenceDocument actualSequenceDoc : alignmentDoc.getSequencingData()) {
			SequenceCharSequence actualSequence = actualSequenceDoc.getCharSequence();
			gapCount = actualSequence.countGaps(start, end); 
			if (gapCount == interval.getLength()) {
				possibleDeletion++;
			}
		}

		return possibleDeletion > alignmentDoc.getSequencingData().size() / 2 ? SequenceVariantType.DELETION : SequenceVariantType.SUBSTITUTION;
	}

	private boolean guessAmbiguous() {
		int from = interval.getMinimumIndex() - 1;
		int to = interval.getTo();

		SequenceCharSequence sequence = null;

		for (SequenceDocument actualSequenceDoc : alignmentDoc.getSequencingData()) {
			SequenceCharSequence anotherSequence = actualSequenceDoc.getCharSequence().subSequence(from, to);
			if (sequence == null) {
				sequence = anotherSequence;
			}
			else if (!sequence.equalsIgnoreCase(anotherSequence)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Generate an identifier based on http://www.hgvs.org/mutnomen/recs.html
	 */
	private String generateIdentifier() {
		guessSOType();
		
		StringBuilder sb = new StringBuilder();
		sb.append("c.");

		boolean isSingleSubstitution = (substitution != null);
		if (isSingleSubstitution) {
			sb.append(alignmentDoc.originalPosition(interval.getFrom()));
			sb.append(substitution[0]);
			sb.append('>');
			sb.append(substitution[1]);
		}
		else {
			sb.append(alignmentDoc.originalPosition(interval.getFrom()));
			if (interval.getLength() > 1) {
				sb.append('_');
				sb.append(alignmentDoc.originalPosition(interval.getTo()));
			}
			sb.append(variantType.symbol());
		}
	
		return sb.toString();

	}
	
	private void guessSOType() {
		SequenceCharSequence expectedSequence = alignmentDoc.getCharSequence();

		substitution = null;
		soType = null;
		
		if (interval.getLength() == 1 && variantType == SequenceVariantType.SUBSTITUTION) {
			int position = interval.getMinimumIndex() - 1;
			SequenceCharSequence e = expectedSequence.subSequence(position - 10, position + 10);
			char expected = expectedSequence.charAt(position);
			LOGGER.debug("          **********V*********");
			LOGGER.debug("Expected: " + e + " " + expected);
			char mostLikelySubstitution = '-';
			int maxOccurence = 0;
			Multiset<Character> possibleSubstitutions = HashMultiset.create();
			for (SequenceDocument actualSequenceDoc : alignmentDoc.getSequencingData()) {
				SequenceCharSequence actualSequence = actualSequenceDoc.getCharSequence();
				char substitution = actualSequence.charAt(position);
				LOGGER.debug("Actual  : " + actualSequence.subSequence(position - 10, position + 10) + " " + substitution);
				if (substitution != '-' && substitution != expected) {
					possibleSubstitutions.add(substitution);
					int occurences = possibleSubstitutions.count(substitution);
					if (occurences > maxOccurence || (occurences == maxOccurence && isValidChar(substitution) && !isValidChar(mostLikelySubstitution))) {
						mostLikelySubstitution = substitution;
						maxOccurence = occurences;
					}
				}
			}
			LOGGER.debug("          **********^*********");
			
			if (isValidChar(mostLikelySubstitution)) {
				substitution = new char[] { expected, mostLikelySubstitution };
				soType = SequenceOntologyUtil.getSOSubstitutionType(expected, mostLikelySubstitution);
			}
		}
		
		if (substitution == null) {
			soType = SequenceOntologyUtil.getSOType(variantType);
		}
	}
	
	private static boolean isValidChar(char c) {
		return c == 'A' || c == 'C' || c == 'G' || c == 'T';
	}
	
	public SequenceAnnotation generateAnnotation() {
		SequenceAnnotation result = new SequenceAnnotation(identifier, variantType.toString(), interval);
		
		if (soType != null) {
			String soName = SequenceOntologyUtil.getSOLabel(soType);
			if (soName != null) {
				result.addQualifier(GeneiousQualifiers.SO_TYPE_NAME, soName);
			}
			result.addQualifier(GeneiousQualifiers.SO_TYPE, soType.toString());
		}

		result.addQualifier(GeneiousQualifiers.AMBIGUOUS_VERIFICATION, isAmbiguous ? "yes" : "no");

		SequenceAnnotation affectedComponent = getAffectedComponent();
		if (affectedComponent != null) {
			result.addQualifier(GeneiousQualifiers.AFFECTED_COMPONENT, affectedComponent.getName());
		}

		return result;
	}
	
	public SequenceAnnotation getAffectedComponent() {
		return GeneiousUtils.findParentAnnotation(interval, alignmentDoc.getDesign().getSequenceAnnotations());
	}

	@Override
    public String toString() {
	    return "SequenceVerificationResult [interval=" + interval
	                    + ", variantType=" + variantType + ", identifier=" + identifier + ", isAmbiguous="
	                    + isAmbiguous + "]";
    }
}
