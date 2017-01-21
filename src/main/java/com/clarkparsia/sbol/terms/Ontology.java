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

package com.clarkparsia.sbol.terms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

import com.google.common.collect.Maps;

public class Ontology {		
	private final Map<String, Term> terms = Maps.newHashMap();
	private final Term top;

	public Ontology(InputStream in, final String namespace, final String topTerm) {
		try {
			RDFHandler aHandler = new RDFHandlerBase() {
				@Override
				public void handleStatement(Statement stmt) throws RDFHandlerException {
					Resource subj = stmt.getSubject();
					if (!subj.stringValue().startsWith(namespace)) {
						return;
					}
					Resource pred = stmt.getPredicate();
					Value obj = stmt.getObject();
					if (pred.equals(RDFS.LABEL)) {
						createTerm(subj.stringValue()).setLabel(obj.stringValue());
					}
					else if (pred.equals(RDFS.SUBCLASSOF) && obj.stringValue().startsWith(namespace)) {
						Term sub = createTerm(subj.stringValue());
						Term sup = createTerm(obj.stringValue());
						sup.addSubClass(sub);
					}
				}
			};

			RDFParser aParser = Rio.createParser(RDFFormat.RDFXML);
			aParser.setRDFHandler(aHandler);
			aParser.parse(in, namespace);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {			
			top = terms.get(topTerm);
			
			try {
				if (in != null) {
					in.close();
				}
			}
			catch (IOException e) {
			}
		}
	}
	
	private Term createTerm(String uri) {
		Term term = terms.get(uri);
		if (term == null) {
			term = new Term(uri, null);
			terms.put(uri, term);
		}
		return term;
	}

	public Term getTerm(String uri) {
		return terms.get(uri);
	}

	public Term getTopTerm() {
		return top;
	}
	
	
}
