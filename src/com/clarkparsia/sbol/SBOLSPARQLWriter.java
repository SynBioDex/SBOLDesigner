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

import java.io.ByteArrayOutputStream;

import org.openrdf.query.QueryEvaluationException;
import org.sbolstandard.core.SBOLValidationException;
import org.sbolstandard.core.SBOLVisitable;

import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;

/**
 * Utility class to read a DNAComponent from a SPARQL endpoint.
 * 
 * @author Evren Sirin
 */
public class SBOLSPARQLWriter {
	private final SPARQLEndpoint endpoint;
	private final SBOLRDFWriter writer;
	
	public SBOLSPARQLWriter(SPARQLEndpoint endpoint) {
		this(endpoint, true);
	}

	public SBOLSPARQLWriter(SPARQLEndpoint endpoint, boolean validate) {
		this.endpoint = endpoint;
		this.writer = new SBOLRDFWriter(validate);
	}

	public void write(SBOLVisitable visitable) throws QueryEvaluationException, SBOLValidationException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try {
			writer.write(visitable, bytes);
			endpoint.addData(RDFInput.forBytes(bytes.toByteArray()));
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
	}
}
