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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jebl.util.ProgressListener;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationQualifier;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceListDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentFileExporter;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.CharSequenceUtilities;
import com.clarkparsia.sbol.SBOLObjectFinder;
import com.clarkparsia.sbol.SBOLPredicates;
import com.clarkparsia.sbol.SBOLTextWriter;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.SublimeSBOLFactory;
import com.clarkparsia.sbol.SublimeSequenceAnalysis;
import com.clarkparsia.sbol.SublimeSequenceVariant;
import com.clarkparsia.sbol.SublimeSequencingData;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLExporter extends DocumentFileExporter {
	private static Logger LOGGER = LoggerFactory.getLogger(SBOLExporter.class.getName());
	
	private static final Set<String> IGNORED_ANNOTION_TYPES = ImmutableSet.of(
	                SequenceAnnotation.TYPE_EDITING_HISTORY_DELETION,
	                SequenceAnnotation.TYPE_EDITING_HISTORY_INSERTION,
	                SequenceAnnotation.TYPE_EDITING_HISTORY_REPLACEMENT);
	
	/**
	 * Constant SequenceAnnotation.INVISIBLE_ANNOTATION_TYPE_PREFIX is defined in Geneious version 5.6 but not in 5.5 so
	 * we redefine it here
	 */
	private static final String INVISIBLE_ANNOTATION_TYPE_PREFIX = "invisible";

	@Override
	public String getFileTypeDescription() {
		return "SBOL";
	}

	@Override
	public String getDefaultExtension() {
		return ".xml";
	}

	@Override
	public DocumentSelectionSignature[] getSelectionSignatures() {
		return new DocumentSelectionSignature[] { DocumentSelectionSignature.forNucleotideSequences(1,
		                Integer.MAX_VALUE, true) };
	}
	
	@Override
	public void export(File file, PluginDocument[] docs, ProgressListener progress, Options options) throws IOException {
		export(file, Arrays.asList(docs), progress, options);
	}

	@Override
	public void export(File file, AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws IOException {
		Function<AnnotatedPluginDocument, PluginDocument> transformer = new Function<AnnotatedPluginDocument, PluginDocument>() {
			@Override
            public PluginDocument apply(AnnotatedPluginDocument doc) {
	            return doc.getDocumentOrNull();
            }
		};
		export(file, Lists.transform(Arrays.asList(docs), transformer), progress, options);
	}
	
	private void export(File file, List<PluginDocument> docs, ProgressListener progress, Options options) throws IOException {
		// First we create a writer using the supplied file handle.
		OutputStream out = new FileOutputStream(file);

		try {
	        // Lets give the ProgressListener an informative message.
	        progress.setMessage("Exporting sequences...");

	        SBOLDocument sbolDoc = SublimeSBOLFactory.createDocument();
	        
	        // loop through all the documents in the list.
	        int count = docs.size();
	        for (int i = 0; i < count; i++) {

	        	// update the ProgressListener
	        	progress.setProgress(((double) i) / count);

	        	PluginDocument internalDoc = docs.get(i);			
	        	if (internalDoc == null) {
	        		throw new IOException("Cannot load document");
	        	}
	        	
	        	if (internalDoc instanceof SequenceDocument) {
	        		SequenceDocument sequenceDoc = (SequenceDocument) internalDoc;
	        		processDocument(sbolDoc, sequenceDoc);
	        	}
	        	else if (internalDoc instanceof SequenceListDocument) {
	        		SequenceListDocument listDoc = (SequenceListDocument) internalDoc;
	        		for (SequenceDocument sequenceDoc : listDoc.getNucleotideSequences()) {
	        			processDocument(sbolDoc, sequenceDoc);    
	                }				
	        	}
	        	else if (internalDoc instanceof SequenceAlignmentDocument) {
	        		SublimeAlignmentDocument alignmentDoc = new SublimeAlignmentDocument((SequenceAlignmentDocument) internalDoc);
	        		SequenceDocument designDoc = alignmentDoc.getDesign();		    	
	        		
	        		processDocument(sbolDoc, designDoc, alignmentDoc);
	        	}
	        	else {
	        		// this should never happen due to our selection signature
	        		throw new IllegalArgumentException("Unsupported document: " + internalDoc.getName());
	        	}
	        }
	        
	        LOGGER.debug("Exported SBOL document:");
	        LOGGER.debug(new SBOLTextWriter().write(sbolDoc));
	        
	        // write the sequence string to the file.
	        SublimeSBOLFactory.createNoValidationWriter().write(sbolDoc, out);
        }
        finally {
        	// its always good to perform housekeeping when we're done.
        	out.close();
        }


	}
	
	public SBOLDocument processDocument(SequenceDocument sequenceDoc) {
		SBOLDocument sbolDoc = SublimeSBOLFactory.createDocument();
		processDocument(sbolDoc, sequenceDoc, null);
		return sbolDoc;
	}
	
	private void processDocument(SBOLDocument sbolDoc, SequenceDocument sequenceDoc) {
		processDocument(sbolDoc, sequenceDoc, null);
	}
	
	private void processDocument(SBOLDocument sbolDoc, SequenceDocument sequenceDoc, SublimeAlignmentDocument alignmentDoc) {
		Iterable<SequenceAnnotation> annotations = 
			Iterables.filter(sequenceDoc.getSequenceAnnotations(), new Predicate<SequenceAnnotation>() {
				@Override
                public boolean apply(SequenceAnnotation annotation) {
	                return !IGNORED_ANNOTION_TYPES.contains(annotation.getType())
	                	&& !annotation.getType().startsWith(INVISIBLE_ANNOTATION_TYPE_PREFIX);
                }
			});

		// SBOL components created for each Geneious annotation
		Map<SequenceAnnotation, org.sbolstandard.core.SequenceAnnotation> components = Maps.newHashMap();
		// parent relationships for each annotation 
		Map<SequenceAnnotation, SequenceAnnotation> parents = computeParents(annotations);
		// create a root component 
		DnaComponent rootComponent = createRootComponent(sequenceDoc, components);
		
		for (SequenceAnnotation annotation : annotations) {
			processRegularAnnotation(annotation, components, parents, rootComponent, sequenceDoc.getCharSequence(), alignmentDoc);
		}
		
		for (SequenceAnnotation annotation : annotations) {
			org.sbolstandard.core.SequenceAnnotation sbolAnn = components.get(annotation);
			for (SequenceAnnotationQualifier qualifier : annotation.getQualifiers()) {
	            if (qualifier.getName().equals(GeneiousQualifiers.PRECEDES)) {
	            	URI uri = URI.create(qualifier.getValue());
	            	Predicate<org.sbolstandard.core.SequenceAnnotation> uriFinder = SBOLPredicates.uri(uri);
	            	org.sbolstandard.core.SequenceAnnotation precedes = SBOLObjectFinder.findObject(rootComponent, uriFinder, org.sbolstandard.core.SequenceAnnotation.class);
	            	if (precedes != null) {
	            		sbolAnn.addPrecede(precedes);
	            	}
	            }
            }
		}

		sbolDoc.addContent(rootComponent);
		
		if (alignmentDoc != null) {
			SublimeSequenceAnalysis analysis = SublimeSBOLFactory.createSequenceAnalysis();
			analysis.setURI(alignmentDoc.getURI());
			analysis.setDate(alignmentDoc.getCreationDate());
			analysis.setComponent(rootComponent);
			
			for (SequenceDocument sequencingDoc : alignmentDoc.getSequencingData()) {
	            SublimeSequencingData data = new SublimeSequencingData();
	            data.setURI(GeneiousUtils.getComponentURI(sequencingDoc));
	            data.setDisplayId(sequencingDoc.getName());
	            data.setDate(sequencingDoc.getCreationDate());
	            analysis.addSequencingData(data);
            }
			
			List<SequenceAnnotation> variantAnnotations = GeneiousUtils.getVariantAnnotations(alignmentDoc.getAnnotations());
			parents = computeParents(variantAnnotations, annotations);
			for (SequenceAnnotation variantAnnotation : variantAnnotations) {
				SublimeSequenceVariant variant = createSBOLVariant(variantAnnotation, alignmentDoc);
									
				SequenceAnnotation parent = parents.get(variantAnnotation);
				DnaComponent parentComponent = components.get(parent).getSubComponent();					
				variant.setComponent(parentComponent);
				
				analysis.addVariant(variant);
			}
			
			sbolDoc.addContent(analysis);
		}
	}
	
	private Map<SequenceAnnotation,SequenceAnnotation> computeParents(Iterable<SequenceAnnotation> annotations) {
		return computeParents(annotations, annotations);
	}
	
	private Map<SequenceAnnotation,SequenceAnnotation> computeParents(Iterable<SequenceAnnotation> annotations, 
					Iterable<SequenceAnnotation> parentCandidates) {
		Map<SequenceAnnotation,SequenceAnnotation> parents = Maps.newHashMap();
		
		for (SequenceAnnotation annotation : annotations) {			
			SequenceAnnotation parent = GeneiousUtils.findParentAnnotation(annotation, parentCandidates);
			parents.put(annotation, parent);
        }
		
		return parents;
	}
	
	private DnaComponent createRootComponent(SequenceDocument sequenceDoc, Map<SequenceAnnotation,org.sbolstandard.core.SequenceAnnotation> components) {
		SequenceAnnotation rootAnnotation = GeneiousUtils.findRootAnnotation(sequenceDoc);
		DnaComponent rootComponent = null;
		if (rootAnnotation == null) {	
			rootComponent = SublimeSBOLFactory.createDnaComponent();
		}
		else {
			Map<SequenceAnnotation,SequenceAnnotation> noParents = ImmutableMap.of();
			processRegularAnnotation(rootAnnotation, components, noParents, null, null, null);
			rootComponent = components.get(rootAnnotation).getSubComponent();	
		}
		
		rootComponent.setURI(GeneiousUtils.getComponentURI(sequenceDoc));
		rootComponent.setDisplayId(sequenceDoc.getName());
		rootComponent.setDescription(sequenceDoc.getDescription());
		
		String seqString = sequenceDoc.getSequenceString();
		if (!Strings.isNullOrEmpty(seqString)) {
			DnaSequence seq = SublimeSBOLFactory.createDnaSequence();
			seq.setURI(GeneiousUtils.getSequenceURI(sequenceDoc));
			seq.setNucleotides(CharSequenceUtilities.asLowerCase(sequenceDoc.getCharSequence()).toString());
			rootComponent.setDnaSequence(seq);
		}
		
		return rootComponent;
	}

	private void processRegularAnnotation(SequenceAnnotation annotation,
	                Map<SequenceAnnotation, org.sbolstandard.core.SequenceAnnotation> components,
	                Map<SequenceAnnotation, SequenceAnnotation> parents,
	                DnaComponent rootComponent, SequenceCharSequence sequence, 
	                SublimeAlignmentDocument alignmentDoc) {
		if (components.containsKey(annotation)) {
			return;
		}
		
		DnaComponent parentComponent = null; 
		SequenceAnnotation parent = parents.get(annotation);
		if (parent != null) {
			processRegularAnnotation(parent, components, parents, rootComponent, sequence, alignmentDoc);
			parentComponent = components.get(parent).getSubComponent();
		}
		else {
			parentComponent = rootComponent;
		}
		
		org.sbolstandard.core.SequenceAnnotation ann = createSBOLAnnotation(annotation, alignmentDoc, parent);
		if (parentComponent != null) {
			parentComponent.addAnnotation(ann);
		}
		components.put(annotation, ann);
	}

	private org.sbolstandard.core.SequenceAnnotation createSBOLAnnotation(SequenceAnnotation annotation, SublimeAlignmentDocument alignmentDoc, SequenceAnnotation parent) {
		DnaComponent comp = SublimeSBOLFactory.createDnaComponent();		
		comp.setURI(SBOLUtils.createURI(annotation.getQualifierValue(GeneiousQualifiers.COMPONENT_URI)));
		comp.setDisplayId(annotation.getName());
		
		URI soType = SequenceOntologyUtil.getSOType(annotation);
		if (soType != null) {
			comp.addType(soType);
		}
				
		org.sbolstandard.core.SequenceAnnotation sbolAnnotation = SublimeSBOLFactory.createSequenceAnnotation();
		sbolAnnotation.setURI(SBOLUtils.createURI(annotation.getQualifierValue(GeneiousQualifiers.URI)));
		
		List<SequenceAnnotationInterval> intervals = annotation.getIntervals();
		if (!intervals.isEmpty()) {
			SequenceAnnotationInterval interval = intervals.get(0);
			if (alignmentDoc != null) {
				interval = alignmentDoc.originalInterval(interval);
			}
			if (parent != null && !parent.getIntervals().isEmpty()) {
				SequenceAnnotationInterval parentInterval = parent.getIntervals().get(0);
				if (alignmentDoc != null) {
					parentInterval = alignmentDoc.originalInterval(parentInterval);
				}
				interval = interval.offsetBy(1-parentInterval.getMinimumIndex());
			}
			sbolAnnotation.setBioStart(interval.getMinimumIndex());
			sbolAnnotation.setBioEnd(interval.getMaximumIndex());
			sbolAnnotation.setStrand(GeneiousUtils.mapStrandType(interval.getDirection()));
		}
		sbolAnnotation.setSubComponent(comp);
		
		return sbolAnnotation;
	}

	private SublimeSequenceVariant createSBOLVariant(SequenceAnnotation annotation, SublimeAlignmentDocument alignmentDoc) {
		SublimeSequenceVariant sbolVariant = new SublimeSequenceVariant();
		sbolVariant.setURI(SBOLUtils.createURI());
		sbolVariant.setName(annotation.getName());
		
		String soType = annotation.getQualifierValue(GeneiousQualifiers.SO_TYPE);
		if (soType != null && soType.length() > 0) {		
			sbolVariant.setType(URI.create(soType));
		}
		List<SequenceAnnotationInterval> intervals = annotation.getIntervals();
		if (!intervals.isEmpty()) {
			SequenceAnnotationInterval interval = alignmentDoc.originalInterval(intervals.get(0));		
			sbolVariant.setBioStart(interval.getMinimumIndex());
			sbolVariant.setBioEnd(interval.getMaximumIndex());
		}
		
		sbolVariant.setAmbiguous(GeneiousUtils.mapAmbiguousVerification(annotation.getQualifierValue(GeneiousQualifiers.AMBIGUOUS_VERIFICATION)));
		
		return sbolVariant;
	}
}
