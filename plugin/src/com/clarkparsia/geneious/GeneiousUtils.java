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
import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core.StrandType;

import com.biomatters.geneious.publicapi.documents.AbstractPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationQualifier;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.clarkparsia.versioning.Branch;
import com.google.common.collect.Iterables;

/**
 * Generic utility functions.
 * 
 * @author Evren Sirin
 */
public class GeneiousUtils {
	public static boolean isVariant(SequenceAnnotation ann) {
		return SequenceVariantType.find(ann.getType()) != null;
    }

	public static List<SequenceAnnotation> getVariantAnnotations(List<SequenceAnnotation> annotations) {
		return filterVerificationAnnotations(annotations, true);
	}

	public static List<SequenceAnnotation> getNonVariantAnnotations(List<SequenceAnnotation> annotations) {
		return filterVerificationAnnotations(annotations, false);
	}
	
	private static List<SequenceAnnotation> filterVerificationAnnotations(List<SequenceAnnotation> annotations, boolean isVariant) {
		List<SequenceAnnotation> result = new ArrayList<SequenceAnnotation>();
		for (SequenceAnnotation annotation : annotations) {
			if (isVariant(annotation) == isVariant) {
				result.add(annotation);
			}
		}
		return result;
	}

	public static List<SequenceAnnotation> findOverlappingAnnotations(SequenceAnnotationInterval interval,
	                Iterable<SequenceAnnotation> annotations) {
		List<SequenceAnnotation> result = new ArrayList<SequenceAnnotation>();
		for (SequenceAnnotation annotation : annotations) {
			for (SequenceAnnotationInterval annotationInterval : annotation.getIntervals()) {
				if (annotationInterval.contains(interval) && annotationInterval.getLength() > interval.getLength()) {
					result.add(annotation);
					break;
				}
			}
		}
		return result;
	}

	public static SequenceAnnotation findShortestAnnotation(Iterable<SequenceAnnotation> annotations) {
		int minLength = Integer.MAX_VALUE;
		SequenceAnnotation result = null;
		for (SequenceAnnotation annotation : annotations) {
			int length = getAnnotationLength(annotation);
			if (length < minLength) {
				minLength = length;
				result = annotation;
			}
		}
		return result;
	}

	public static SequenceAnnotation findLongestAnnotation(Iterable<SequenceAnnotation> annotations) {
		int maxLength = Integer.MIN_VALUE;
		SequenceAnnotation result = null;
		for (SequenceAnnotation annotation : annotations) {
			int length = getAnnotationLength(annotation);
			if (length > maxLength) {
				maxLength = length;
				result = annotation;
			}
		}
		return result;
	}

	public static SequenceAnnotation findParentAnnotation(SequenceAnnotation sequence, Iterable<SequenceAnnotation> annotations) {
		SequenceAnnotationInterval interval = Iterables.getFirst(sequence.getIntervals(), null);
		return interval == null ? null : findParentAnnotation(interval, annotations);
	}

	public static SequenceAnnotation findParentAnnotation(SequenceAnnotationInterval interval, Iterable<SequenceAnnotation> annotations) {
		List<SequenceAnnotation> overlaps = findOverlappingAnnotations(interval, annotations);
		
		return GeneiousUtils.findShortestAnnotation(overlaps);
	}

	public static SequenceAnnotation findAnnotationWithQualifier(Iterable<SequenceAnnotation> annotations, String qualifierName, String qualifierValue) {
		for (SequenceAnnotation annotation : annotations) {
			for (SequenceAnnotationQualifier qualifier : annotation.getQualifiers()) {
	            if (qualifier.getName().equals(qualifierName) && qualifier.getValue().equals(qualifierValue)) {
	            	return annotation;
	            }
            }
		}
		return null;
	}

	public static int getAnnotationLength(SequenceAnnotation annotation) {
		int length = 0;
		for (SequenceAnnotationInterval annotationInterval : annotation.getIntervals()) {
			length += annotationInterval.getLength();
		}
		return length;
	}

	public static Direction mapDirection(StrandType strand) {
		if (strand == null) {
			return Direction.none;
		}
		
    	switch (strand) {
    		case POSITIVE:
    			return Direction.leftToRight;
    
    		case NEGATIVE:
    			return Direction.rightToLeft;
    
    		default:
    			return Direction.none;
    	}
    }

	public static StrandType mapStrandType(Direction direction) {
    	switch (direction) {
    		case leftToRight:
    			return StrandType.POSITIVE;
    
    		case rightToLeft:
    			return StrandType.NEGATIVE;
    
    		default:
    			return null;
    	}
    }

	public static StrandType flipStrandType(StrandType strandType) {
		if (strandType == null) {
			return null;
		}
		
    	switch (strandType) {
    		case NEGATIVE:
    			return StrandType.POSITIVE;
    
    		case POSITIVE:
    			return StrandType.NEGATIVE;
    
    		default:
    			return null;
    	}
    }

	public static Boolean mapAmbiguousVerification(String verification) {
		if (verification == null) {
			return null;
		}
		else if (verification.equalsIgnoreCase("yes")) {
			return Boolean.TRUE;
		}
		else if (verification.equalsIgnoreCase("no")) {
			return Boolean.FALSE;
		}
		else {
			return null;
    	}
    }

	public static URI getComponentURI(SequenceDocument doc) {
		return getURI(doc, GeneiousQualifiers.COMPONENT_URI);
    }

	public static URI getSequenceURI(SequenceDocument doc) {
    	return getURI(doc, GeneiousQualifiers.URI);
    }

	public static URI getURI(SequenceDocument doc, String fieldName) {
		Object uri = doc.getFieldValue(fieldName);
    	return SBOLUtils.createURI((uri != null && uri instanceof String) ? uri.toString() : null);
    }

	public static void setComponentURI(AbstractPluginDocument doc, URI uri) {
		doc.setFieldValue(GeneiousQualifiers.COMPONENT_URI, uri.toString());
    }

	public static void setSequenceURI(AbstractPluginDocument doc, URI uri) {
		doc.setFieldValue(GeneiousQualifiers.URI, uri.toString());
    }

	public static RVTDocumentIO getVersioningInfo(SequenceDocument doc) {
		Object endpointURL = doc.getFieldValue(GeneiousQualifiers.VERSION_ENDPOINT);
		Object designName = doc.getFieldValue(GeneiousQualifiers.VERSION_NAME);
		if (endpointURL != null && designName != null) {
			return RVTDocumentIO.createForBranch(new StardogEndpoint(endpointURL.toString()), designName.toString(), Branch.MASTER);
		}
		return null;
    }

	public static void setVersioningInfo(AbstractPluginDocument doc, RVTDocumentIO io) {
		doc.setFieldValue(GeneiousQualifiers.VERSION_ENDPOINT, io.getBranch().getEndpoint().getURL());
		doc.setFieldValue(GeneiousQualifiers.VERSION_NAME, io.getBranch().getRepository().getName());
    }

	public static SequenceAnnotation findRootAnnotation(SequenceDocument sequenceDoc) {
    	int seqLength = sequenceDoc.getCharSequence().getInternalSequenceLength();		
    	if (seqLength > 0) {
    		final SequenceAnnotation longest = findLongestAnnotation(sequenceDoc.getSequenceAnnotations());
    		if (longest != null && getAnnotationLength(longest) == seqLength) {
    			return longest;
    		}
    	}
    	return null;
    }
}
