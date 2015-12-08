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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import jebl.util.ProgressListener;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLValidationException;

import com.biomatters.geneious.publicapi.documents.sequence.EditableSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.utilities.ProgressInputStream;
import com.clarkparsia.sbol.SublimeSBOLBaseVisitor;
import com.clarkparsia.sbol.SublimeSBOLFactory;
import com.clarkparsia.sbol.SublimeSequenceVariant;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLImporter extends DocumentFileImporter {
	public static class Visitor extends SublimeSBOLBaseVisitor {
	    private List<EditableSequenceDocument> sequenceDocuments = Lists.newArrayList();
	    private List<SequenceAnnotation> annotations = Lists.newArrayList();
	    private boolean createNewDocument = true;
	    private boolean topLevel = true;

	    public Visitor() {
	    }

		public List<EditableSequenceDocument> importDocument(SBOLDocument doc) {
			createNewDocument = true;
			sequenceDocuments.clear();
			
			visit(doc);
			
        	return sequenceDocuments;
        }

		public void importContents(SBOLDocument doc, EditableSequenceDocument sequenceDoc) {
			createNewDocument = false;
			sequenceDocuments.clear();
			sequenceDocuments.add(sequenceDoc);
			
			visit(doc);
        }

		private String getDnaSequence(SBOLObject obj) {
			if (obj instanceof DnaComponent) {
				DnaSequence seq = ((DnaComponent) obj).getDnaSequence();
				if (seq != null && seq.getNucleotides() != null) {
					return seq.getNucleotides();
				}				
			}
			return null;
		}
		
	    public void visit(SBOLDocument sbolDocument) {
			for (SBOLRootObject rootObj : sbolDocument.getContents()) {
				topLevel = true;
				
				rootObj.accept(this);
				
				String seq = getDnaSequence(rootObj);
				EditableSequenceDocument doc = sequenceDocuments.get(sequenceDocuments.size() - 1);
				if (createNewDocument || seq == null) {
					doc.setAnnotations(annotations);
				}
				else {
					doc.setSequenceAndAnnotations(seq, annotations);
				}
			}
	    }

	    public void visit(DnaComponent dnaComponent) {
	    	if (topLevel && createNewDocument) {
	    		EditableSequenceDocument doc = processRootComponent(dnaComponent);
	    		sequenceDocuments.add(doc);
	    	}
	    	
	    	super.visit(dnaComponent);
	    }

	    public EditableSequenceDocument processRootComponent(DnaComponent dnaComponent) {
	    	URI uri = dnaComponent.getURI();
	    	String name = dnaComponent.getDisplayId();
	    	String description = dnaComponent.getDescription();
	    	DnaSequence dnaSeq = dnaComponent.getDnaSequence();
	    	String sequence = (dnaSeq == null) ? "" : dnaSeq.getNucleotides();
	    	
	    	DefaultNucleotideSequence doc = new DefaultNucleotideSequence(name, description, sequence, new Date());
	    	GeneiousUtils.setComponentURI(doc, uri);
	    	if (dnaSeq != null) {
	    		GeneiousUtils.setSequenceURI(doc, dnaSeq.getURI());
	    	}
	    	return doc;
	    }

	    @Override
	    public void visit(org.sbolstandard.core.SequenceAnnotation sbolAnnotation) {
	    	DnaComponent dnaComponent = sbolAnnotation.getSubComponent();
	    	String name = dnaComponent.getDisplayId();
	    	URI soType = SequenceOntologyUtil.findMappableType(dnaComponent.getTypes());
	    	String geniousType = SequenceOntologyUtil.mapToGeniuousType(soType);
	    	Integer bioStart = sbolAnnotation.getBioStart();
	    	Integer bioEnd = sbolAnnotation.getBioEnd();

	    	SequenceAnnotation annotation = new SequenceAnnotation(name, geniousType);
	    	annotation.addQualifier(GeneiousQualifiers.URI, sbolAnnotation.getURI().toString());
	    	annotation.addQualifier(GeneiousQualifiers.COMPONENT_URI, dnaComponent.getURI().toString());
	    	
	    	if (bioStart != null && bioEnd != null) {
	    		Direction direction = GeneiousUtils.mapDirection(sbolAnnotation.getStrand());
	    		annotation.addInterval(bioStart, bioEnd, direction);
	    	}
	    	
	    	for (org.sbolstandard.core.SequenceAnnotation ann : sbolAnnotation.getPrecedes()) {
	            annotation.addQualifier(GeneiousQualifiers.PRECEDES, ann.getURI().toString());
            }
	    	
	    	if (soType != null) {
	    		annotation.addQualifier(GeneiousQualifiers.SO_TYPE, soType.toString());
	    	}
	    	
	    	annotations.add(annotation);
	    }

	    @Override
	    public void visit(SublimeSequenceVariant variant) {
	    	String name = variant.getName();
	    	String type = SequenceOntologyUtil.mapToGeniuousType(variant.getType());
	    	Integer bioStart = variant.getBioStart();
	    	Integer bioEnd = variant.getBioEnd();

	    	if (name == null) {
	    		throw new SBOLValidationException("Missing name for sequence variant: " + variant);
	    	}
	    	
	    	SequenceAnnotation annotation = new SequenceAnnotation(name, type);
	    	annotation.addQualifier(GeneiousQualifiers.URI, variant.getURI().toString());
	    	Boolean isAmbiguous = variant.isAmbiguous();
	    	if (isAmbiguous != null) {
	    		annotation.addQualifier(GeneiousQualifiers.AMBIGUOUS_VERIFICATION, isAmbiguous ? "yes" : "no");
	    	}
	    	annotation.setType(type);
	    	if (bioStart != null && bioEnd != null) {
	    		annotation.addInterval(bioStart, bioEnd);
	    	}
	    	
	    	annotations.add(annotation);
	    }
    }

	public String[] getPermissibleExtensions() {
		return new String[] { ".xml", ".rdf" };
	}

	public String getFileTypeDescription() {
		return "SBOL";
	}

	public AutoDetectStatus tentativeAutoDetect(File file, String fileContentsStart) {
		if (fileContentsStart.startsWith("<?xml") || fileContentsStart.startsWith("<rdf:RDF")) {
			return AutoDetectStatus.MAYBE;
		}
		else {
			return AutoDetectStatus.REJECT_FILE;
		}
	}

	public void importDocuments(final File file, final ImportCallback callback, final ProgressListener progressListener)
	                throws IOException, DocumentImportException {
		InputStream in = new ProgressInputStream(progressListener, file);
		try {
			SBOLDocument sbolDocument = SublimeSBOLFactory.createNoValidationReader().read(in);
			for (SequenceDocument doc : new Visitor().importDocument(sbolDocument)) {
				callback.addDocument(doc);
			}
		}
		catch (SBOLValidationException e) {
			throw new DocumentImportException("Invalid SBOL file: " + e.getMessage());
		}
		finally {
			in.close();
		}
	}
}
