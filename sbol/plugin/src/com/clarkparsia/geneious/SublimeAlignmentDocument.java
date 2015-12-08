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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraphSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.clarkparsia.sbol.SBOLUtils;
import com.google.common.collect.Lists;

/**
 * A convenience class that wraps a Geneious alignment document and makes it easier to process its contents.
 * 
 * @author Evren Sirin
 */
public class SublimeAlignmentDocument {
	private static Logger LOGGER = LoggerFactory.getLogger(SublimeAlignmentDocument.class.getName());
	
	private final SequenceAlignmentDocument originalDoc;
	
	private final URI alignmentURI;
	
	/**
	 * The design document used in the alignment. 
	 */
	private SequenceDocument designDoc;
	/**
	 * The sequencing data used in the alignment.
	 */
	private List<SequenceDocument> sequencingDataDocs;
	/**
	 * The sequence of the design document in the alignment. This sequence will include 
	 */
	private SequenceCharSequence sequence;
	/**
	 * The annotations of the (consensus sequence of the) alignment document.
	 */
	private List<SequenceAnnotation> annotations;

	public SublimeAlignmentDocument(SequenceAlignmentDocument alignmentDoc) throws IllegalArgumentException {
		originalDoc = alignmentDoc;
		
		alignmentURI = SBOLUtils.createURI();
		
		LOGGER.debug("Sequences: {}",  alignmentDoc.getSequences());
		
		sequencingDataDocs = Lists.newArrayList(alignmentDoc.getSequences());
		
		LOGGER.debug("Sequencing data: {}",  sequencingDataDocs);
    	
    	int designDocumentIndex = findDesignDocumentIndex(sequencingDataDocs); 
    	
    	LOGGER.debug("Design doc index: {}",  designDocumentIndex);

    	designDoc = sequencingDataDocs.remove(designDocumentIndex);
    	
    	LOGGER.debug("Design doc: {}",  designDoc);
    	
    	sequence = designDoc.getCharSequence();    	
    	
    	annotations = alignmentDoc.getAnnotationsOnConsensus();
	}
	
	public URI getURI() {
		return alignmentURI;
	}
	
	public Date getCreationDate() {
		return originalDoc.getCreationDate();
	}

	public SequenceDocument getDesign() {
    	return designDoc;
    }

	public List<SequenceDocument> getSequencingData() {
    	return sequencingDataDocs;
    }

	public List<SequenceAnnotation> getAnnotations() {
    	return annotations;
    }

	public SequenceCharSequence getCharSequence() {
	    return sequence;
    }

	public SequenceAnnotationInterval originalInterval(SequenceAnnotationInterval interval) {
		int start = originalPosition(interval.getFrom());
		int end = originalPosition(interval.getTo());
		return new SequenceAnnotationInterval(start, end);
	}

	public int originalPosition(int position) {
		int startPosition = sequence.getLeadingGapsLength();
		int gapCount = sequence.countGaps(startPosition, position);
		return position - startPosition - gapCount;
	}

	private static int findDesignDocumentIndex(List<SequenceDocument> sequences) {
		int index = -1;
		for (int i = 0; i < sequences.size(); i++) {
			SequenceDocument sequenceDocument = sequences.get(i);
			if (!(sequenceDocument instanceof NucleotideGraphSequenceDocument)) {
				if (index != -1) {
					throw new IllegalArgumentException("Multiple design sequences found: [" + sequences.get(index).getName() + ", " + sequenceDocument.getName() + "]");
				}
				index = i;
			}
		}
		
		if (index == -1) {
			throw new IllegalArgumentException("No design sequence document found");
		}
		
		return index;
	}
}

