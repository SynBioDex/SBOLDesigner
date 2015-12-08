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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.rio.RDFFormat;
import org.sbolstandard.core.Collection;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLReader;
import org.sbolstandard.core.SBOLValidationException;
import org.sbolstandard.core.SBOLValidator;
import org.sbolstandard.core.SBOLWriter;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.impl.CollectionImpl;
import org.sbolstandard.core.impl.DnaComponentImpl;
import org.sbolstandard.core.impl.DnaSequenceImpl;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.SBOLValidatorImpl;
import org.sbolstandard.core.impl.SequenceAnnotationImpl;

import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;

/**
 * Extended SBOL factory that create Sublime specific implementations of sBOL interfaces (where applicable) and uses
 * RDF-based readers and writers.
 * 
 * @author Evren Sirin
 */
public class SublimeSBOLFactory {
	// This class only provides static fields, cannot be instantiated
	private SublimeSBOLFactory() {		
	}

	private static final SBOLReader READER = createReader();
	
	private static final SBOLWriter WRITER = createWriter();
	
	private static final SBOLValidator VALIDATOR = createValidator();
	
	/**
	 * Creates a fresh SBOL document instance and populates its contents from the given XML source. The reader will
	 * perform validation automatically. The documents read by this reader are guaranteed to be valid so it is not 
	 * necessary to perform {@link #validate(SBOLDocument) validation}. If the contents of the document is changed
	 * afterwards, validation will be needed.
	 * 
	 * @throws SBOLValidationException if the contents of the document is not valid
	 */
	public static SBOLDocument read(InputStream in) throws IOException, SBOLValidationException {
		return READER.read(in); 
	}
	
	/**
	 * Writes the contents of an SBOL document in XML format to the given output stream. The writer will
	 * perform validation automatically.
	 * 
	 * @throws SBOLValidationException if the contents of the document is not valid
	 */
	public static void write(SBOLDocument doc, OutputStream out) throws IOException, SBOLValidationException {
		WRITER.write(doc, out);
	}

	/**
	 * Validates the contents of the given SBOL document.
	 * 
	 * @throws SBOLValidationException if the contents of the document is not valid
	 */
	public static void validate(SBOLDocument doc) throws SBOLValidationException {
		VALIDATOR.validate(doc);
	}
	
	/**
	 * Creates a new reader instance for reading from XML documents that will automatically perform validation and
	 * will refuse to load invalid documents. 
	 */
	public static SBOLReader createReader() {
		return new SBOLRDFReader(true);
	}

	/**
	 * Creates a new reader instance for writing SBOL documents as XML that will NOT perform validation. The documents 
	 * read by this reader may not be written successfully if the writer performs validation.
	 * 
	 */
	public static SBOLReader createNoValidationReader() {
		return new SBOLRDFReader(false);
	}
	
	public static SBOLReader createReader(RDFFormat format, boolean validate) {
		return new SBOLRDFReader(format, validate);
	}

	/**
	 * Creates a new writer instance for writing SBOL documents as XML that will automatically perform validation and
	 * will refuse to write invalid documents. WARNING: If validation fails while writing the contents of the document,
	 * the output may still contain partial serialization results that has been written before the error is discovered. 
	 */
	public static SBOLWriter createWriter() {
		return new SBOLRDFWriter(true);
	}

	/**
	 * Creates a new writer instance for writing SBOL documents as XML that will NOT perform validation. THe documents 
	 * written by this writer may not be read successfully if the reader performs validation.
	 */
	public static SBOLWriter createNoValidationWriter() {
		return new SBOLRDFWriter(false);
	}
	
	public static SBOLWriter createWriter(RDFFormat format, boolean validate) {
		return new SBOLRDFWriter(format, validate);
	}
	
	public static SBOLSPARQLReader createReader(SPARQLEndpoint endpoint, boolean validate) {
		return new SBOLSPARQLReader(endpoint, validate);
	}
	
	public static SBOLSPARQLWriter createWriter(SPARQLEndpoint endpoint, boolean validate) {
		return new SBOLSPARQLWriter(endpoint, validate);
	}
	
	/**
	 * Creates a new validator instance.
	 */
	public static SBOLValidator createValidator() {
		return new SBOLValidatorImpl();
	}
	
	/**
	 * Creates a new {@link SBOLDocument} with the given contents.
	 */
	public static SBOLDocument createDocument() {
		return new SBOLDocumentImpl();
	}

	/**
	 * Creates a new empty {@link Collection} instance.
	 */
	public static Collection createCollection() {
		return new CollectionImpl();
	}

	/**
	 * Creates a new empty {@link DnaComponent} instance. 
	 */
	public static DnaComponent createDnaComponent() {
		return new DnaComponentImpl();
	}

	/**
	 * Creates a new empty {@link DnaSequence} instance. 
	 */
	public static DnaSequence createDnaSequence() {
		return new DnaSequenceImpl();
	}

	/**
	 * Creates a new empty {@link SequenceAnnotation} instance. 
	 */
	public static SequenceAnnotation createSequenceAnnotation() {
		return new SequenceAnnotationImpl();
	}
	
	public static SublimeSequenceAnalysis createSequenceAnalysis() {
		return new SublimeSequenceAnalysis();
	}
	
	public static SublimeSequencingData createSequencingData() {
		return new SublimeSequencingData();
	}
	
	public static SublimeSequenceVariant createSequenceVariant() {
		return new SublimeSequenceVariant();
	}
}
