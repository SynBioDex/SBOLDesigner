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

package com.clarkparsia.sbol.editor.sparql;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.rio.RDFHandler;

public interface SPARQLEndpoint {
	public String getURL();
	
	public void close();

	public void executeSelectQuery(String query, TupleQueryResultHandler handler) throws QueryEvaluationException;

	public boolean executeAskQuery(String query) throws QueryEvaluationException;

	public void addData(RDFInput file) throws Exception;

	public void addData(RDFInput file, String namedGraph) throws Exception;

	public void removeData(RDFInput file) throws Exception;

	public void removeData(RDFInput file, String namedGraph) throws Exception;

	public void export(RDFHandler handler) throws Exception;

	public void export(RDFHandler handler, String namedGraph) throws Exception;

	public void clear() throws Exception;

	public void clear(String namedGraph) throws Exception;

	public void validate(RDFInput constraints) throws Exception;

	public void validate(RDFInput constraints, String namedGraph) throws Exception;

	public long size() throws Exception;
}