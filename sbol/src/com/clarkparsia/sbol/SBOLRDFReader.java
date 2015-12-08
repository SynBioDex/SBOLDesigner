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
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.sbolstandard.core.Collection;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core.SBOLReader;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLValidationException;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;
import org.sbolstandard.core.impl.CollectionImpl;
import org.sbolstandard.core.impl.DnaComponentImpl;
import org.sbolstandard.core.impl.DnaSequenceImpl;
import org.sbolstandard.core.impl.SBOLValidatorImpl;
import org.sbolstandard.core.impl.SequenceAnnotationImpl;

import com.clarkparsia.sbol.editor.sparql.RDFSource;

/**
 * Utility class to write the contents of an SBOL document in a text-based, human-readable format. This format is used
 * for information purposes and not intended to be an exchange syntax.
 * 
 * @author Evren Sirin
 */
public class SBOLRDFReader implements SBOLReader {
	private final RDFFormat format;
	private final SBOLValidatorImpl validator;

	public SBOLRDFReader(boolean validate) {
		this(RDFFormat.RDFXML, validate);
	}

	public SBOLRDFReader(RDFFormat format) {
		this(format, true);
	}

	public SBOLRDFReader(RDFFormat format, boolean validate) {
		this.format = format;
		this.validator = validate ? new SBOLValidatorImpl() : null;
	}

	@Override
	public SBOLDocument read(final InputStream in) throws IOException, SBOLValidationException {
		final RDFParser parser = RDFParserRegistry.getInstance().get(format).getParser();
		return read(new RDFSource() {			
			@Override
			public void read(RDFHandler handler) throws IOException {
				try {
					parser.setRDFHandler(handler);
	                parser.parse(in, SBOLVocabulary.NAMESPACE);
                }
                catch (Exception e) {
	                throw new IOException(e);
                }
			}
		});
	}
	
	public SBOLDocument read(RDFSource source) throws IOException, SBOLValidationException {
		Handler handler = new Handler();
		try {
			source.read(handler);
			SBOLDocument doc = handler.getDocument();
			if (validator != null) {
				validator.validateWithoutSchema(doc);
			}
			return doc;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private static java.net.URI asJavaURI(Value val) {
		if (val instanceof URI) {
			try {
				return new java.net.URI(val.stringValue());
			}
			catch (Exception e) {
				throw new SBOLValidationException("Invalid uri: " + val);
			}
		}
		
		throw new SBOLValidationException("Expecting URI but got: " + val);
	}
	
	private static URI asURI(Value val) {
		if (val instanceof URI) {
			return (URI) val;
		}
		
		throw new SBOLValidationException("Expecting URI but got: " + val);
	}
	
	private static Literal asLiteral(Value val) {
		if (val instanceof Literal) {
			return (Literal) val;
		}
		
		throw new SBOLValidationException("Expecting literal but got: " + val);
	}		
	
	private static StrandType asStrandType(Value val) {
		String strand = asLiteral(val).stringValue();
		if (strand.equals("+")) {
			return StrandType.POSITIVE;
		}
		if (strand.equals("-")) {
			return StrandType.NEGATIVE;
		}
		throw new SBOLValidationException("Invalid strand value: " + strand);
	}		
	
	/**
	 * @author Evren Sirin
	 */
	protected static class Handler extends RDFHandlerBase implements SublimeSBOLVisitor {
		private static URI DUMMY = ValueFactoryImpl.getInstance().createURI("urn:dummy");
		
		private static SBOLMappers MAPPERS = new SBOLMappers(
			new SBOLMapper<DnaComponent>(SBOLVocabulary.DnaComponent, DnaComponentImpl.class),
			new SBOLMapper<DnaSequence>(SBOLVocabulary.DnaSequence, DnaSequenceImpl.class),
			new SBOLMapper<SequenceAnnotation>(SBOLVocabulary.SequenceAnnotation, SequenceAnnotationImpl.class),
			new SBOLMapper<Collection>(SBOLVocabulary.Collection, CollectionImpl.class),
			new SBOLMapper<SublimeSequenceAnalysis>(SublimeVocabulary.SequenceAnalysis, SublimeSequenceAnalysis.class),
			new SBOLMapper<SublimeSequencingData>(SublimeVocabulary.SequencingData, SublimeSequencingData.class),
			new SBOLMapper<SublimeSequenceVariant>(SublimeVocabulary.SequenceVariant, SublimeSequenceVariant.class)
		);
		
		private final SBOLDocument doc = SublimeSBOLFactory.createDocument();
		private final Map<Value, SBOLObject> sbolObjects = new HashMap<Value, SBOLObject>();
		
		private Resource subj = DUMMY;
		private URI prop;
		private Value obj;

		private SBOLObject sbol;

		public SBOLDocument getDocument() {
			return doc;
		}

		private <T extends SBOLObject> T createSBOL(Value uri, URI type) throws SBOLValidationException {
			SBOLMapper<T> mapper = MAPPERS.get(type);
			if (mapper == null) {
				throw new SBOLValidationException("Unknown type: " + type);
			}

			SBOLObject cached = sbolObjects.get(uri);
			if (cached != null) {
				return mapper.cast(cached);
			}

			T sbolObject = mapper.create(uri);
			
			sbolObjects.put(uri, sbolObject);
			
			return sbolObject;
		}

		@Override
		public void handleStatement(Statement stmt) throws RDFHandlerException {
			prop = stmt.getPredicate();
			obj = stmt.getObject();
			
			if (!subj.equals(stmt.getSubject())) {
				subj = stmt.getSubject();
	
				sbol = sbolObjects.get(subj);
				if (sbol == null) {
					if (!prop.equals(RDF.TYPE)) {
						throw new SBOLValidationException("Expecting rdf:type value but got: " + stmt);
					}
					sbol = createSBOL(subj, asURI(obj));
					if (sbol instanceof SBOLRootObject) {
						doc.addContent((SBOLRootObject) sbol);
					}
					else {
						throw new SBOLValidationException("Invalid root object: " + subj);
					}
					return;
				}
				else if (prop.equals(RDF.TYPE)) {
					SBOLMapper<?> mapper = MAPPERS.get(obj);
					if (mapper != null) {
						 if (mapper.isValidObject(sbol)) {
							 return;
						 }
						 else {
							 throw new SBOLValidationException("Multiple objects with same URI: " + subj);
						 }
					}
				}
			}

			sbol.accept(this);
		}

		@Override
		public void visit(Collection coll) {
			if (prop.equals(SBOLVocabulary.name)) {
				coll.setName(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.description)) {
				coll.setDescription(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.displayId)) {
				coll.setDisplayId(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.component)) {
				DnaComponent comp = createSBOL(obj, SBOLVocabulary.DnaComponent);
				coll.addComponent(comp);
			}
			else {
				// ignore
			}
		}

		@Override
		public void visit(DnaComponent comp) {
			if (prop.equals(SBOLVocabulary.name)) {
				comp.setName(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.description)) {
				comp.setDescription(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.displayId)) {
				comp.setDisplayId(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.dnaSequence)) {
				DnaSequence seq = createSBOL(obj, SBOLVocabulary.DnaSequence);
				comp.setDnaSequence(seq);
			}
			else if (prop.equals(SBOLVocabulary.annotation)) {
				SequenceAnnotation ann = createSBOL(obj, SBOLVocabulary.SequenceAnnotation);
				comp.addAnnotation(ann);
			}
			else if (prop.equals(RDF.TYPE)) {
				comp.addType(asJavaURI(obj));
			}
			else {
				// ignore
			}
		}

		@Override
		public void visit(DnaSequence seq) {
			if (prop.equals(SBOLVocabulary.nucleotides)) {
				seq.setNucleotides(obj.stringValue());
			}
			else {
				// ignore
			}
		}

		@Override
		public void visit(SequenceAnnotation ann) {
			if (prop.equals(SBOLVocabulary.bioStart)) {
				ann.setBioStart(asLiteral(obj).intValue());
			}
			else if (prop.equals(SBOLVocabulary.bioEnd)) {
				ann.setBioEnd(asLiteral(obj).intValue());
			}
			else if (prop.equals(SBOLVocabulary.strand)) {
				ann.setStrand(asStrandType(obj));
			}
			else if (prop.equals(SBOLVocabulary.precedes)) {
				SequenceAnnotation prec = createSBOL(obj, SBOLVocabulary.SequenceAnnotation);
				ann.addPrecede(prec);
			}
			else if (prop.equals(SBOLVocabulary.subComponent)) {
				DnaComponent comp = createSBOL(obj, SBOLVocabulary.DnaComponent);
				ann.setSubComponent(comp);
			}
			else {
				// ignore
			}
		}

		@Override
        public void visit(SublimeSequenceAnalysis ann) {
			if (prop.equals(SublimeVocabulary.date)) {
				ann.setDate(asLiteral(obj).calendarValue().toGregorianCalendar().getTime());
			}
			else if (prop.equals(SublimeVocabulary.conclusion)) {
				ann.setConclusion(asLiteral(obj).stringValue());
			}
			else if (prop.equals(SublimeVocabulary.dataAnalyzed)) {
				SublimeSequencingData data = createSBOL(obj, SublimeVocabulary.SequencingData);
				ann.addSequencingData(data);
			}
			else if (prop.equals(SublimeVocabulary.variantFound)) {
				SublimeSequenceVariant variant = createSBOL(obj, SublimeVocabulary.SequenceVariant);
				ann.addVariant(variant);
			}
			else {
				// ignore
			}
        }

		@Override
        public void visit(SublimeSequencingData data) {
			if (prop.equals(SBOLVocabulary.displayId)) {
				data.setDisplayId(asLiteral(obj).stringValue());
			}
			else if (prop.equals(SublimeVocabulary.date)) {
				data.setDate(asLiteral(obj).calendarValue().toGregorianCalendar().getTime());
			}
			else if (prop.equals(SublimeVocabulary.dataFile)) {
				data.setDataFile(asLiteral(obj).stringValue());
			}
			else if (prop.equals(SublimeVocabulary.orderNumber)) {
				data.setOrderNumber(asLiteral(obj).stringValue());
			}
			else {
				// ignore
			}
        }

		@Override
        public void visit(SublimeSequenceVariant ann) {
			if (prop.equals(SBOLVocabulary.name)) {
				ann.setName(obj.stringValue());
			}
			else if (prop.equals(SBOLVocabulary.bioStart)) {
				ann.setBioStart(asLiteral(obj).intValue());
			}
			else if (prop.equals(SBOLVocabulary.bioEnd)) {
				ann.setBioEnd(asLiteral(obj).intValue());
			}
			else if (prop.equals(SublimeVocabulary.ambiguous)) {
				ann.setAmbiguous(asLiteral(obj).booleanValue());
			}
			else if (prop.equals(SublimeVocabulary.observedIn)) {
				DnaComponent comp = createSBOL(obj, SBOLVocabulary.DnaComponent);
				ann.setComponent(comp);
			}
			else if (prop.equals(RDF.TYPE)) {
				ann.setType(asJavaURI(obj));
			}
			else {
				// ignore
			}
        }

		@Override
		public void visit(SBOLDocument doc) {
			throw new SBOLValidationException("Only one SBOL document can exist");
		}
	}
	
	protected static class SBOLMappers {
		private final Map<URI, SBOLMapper<?>> mappers = new HashMap<URI, SBOLMapper<?>>();
	
		private SBOLMappers(SBOLMapper<?>... mappers) {
			for (SBOLMapper<?> mapper : mappers) {
				this.mappers.put(mapper.type, mapper);
            }
		}
	
		@SuppressWarnings("unchecked")
        private <T extends SBOLObject> SBOLMapper<T> get(Value type) {
			return (SBOLMapper<T>) mappers.get(type);
		}	
	}
	
	protected static class SBOLMapper<T extends SBOLObject> {
		private final URI type;
		private final Class<? extends T> cls;
		
		public SBOLMapper(URI type, Class<? extends T> cls) {
	        this.type = type;
	        this.cls = cls;
        }

		private T create(Value uri) {
			try {
	            T sbol = cls.newInstance();
	            sbol.setURI(asJavaURI(uri));
	            return sbol;
            }
			catch (RuntimeException e) {
	            throw e;
            }
            catch (Exception e) {
	            throw new SBOLValidationException(e);
            }
		}		
		
		private boolean isValidObject(SBOLObject obj) {
			return cls.isInstance(obj);
		}
		
		private T cast(SBOLObject obj) {
			return cls.cast(obj);
		}			
		
		public URI getType() {
			return type;
		}
	}
}
