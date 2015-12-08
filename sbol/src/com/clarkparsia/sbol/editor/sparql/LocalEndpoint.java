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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Evren Sirin
 */
public class LocalEndpoint extends AbstractEndpoint {
	private final Repository repo;
	private final String url;
	
	public LocalEndpoint() {
		this(null);
	}

	public LocalEndpoint(String inputURL) {
		this.repo = new SailRepository(new MemoryStore());
		this.url = inputURL;

		try {
			repo.initialize();

			if (inputURL != null) {
				RepositoryConnection conn = repo.getConnection();
				conn.add(new URL(inputURL), "", RDFFormat.RDFXML);
				conn.close();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getURL() {
		return url;
	}

	@Override
	public void close() {
		try {
			repo.shutDown();
		}
		catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void executeSelectQuery(String query, TupleQueryResultHandler handler) throws QueryEvaluationException {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			conn.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate(handler);
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
		finally {
			closeQuietly(conn);
		}
	}
	
	private void closeQuietly(RepositoryConnection conn) {
		if (conn != null) {
			try {
	            conn.close();
            }
            catch (RepositoryException e) {
	            e.printStackTrace();
            }
		}
	}
	
	private Resource[] context(String namedGraph) {
		return (namedGraph == null) ? new Resource[0] : new Resource[] { ValueFactoryImpl.getInstance().createURI(namedGraph) };
	}

	@Override
    public boolean executeAskQuery(String query) throws QueryEvaluationException {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			return conn.prepareBooleanQuery(QueryLanguage.SPARQL, query).evaluate();
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (Exception e) {
			throw new QueryEvaluationException(e);
		}
		finally {
			closeQuietly(conn);
		}
    }

	@Override
    public void addData(RDFInput input, String namedGraph) throws Exception {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			if (input.isFile()) {
				File file = input.getFile();
				conn.add(file, "", RDFFormat.forFileName(file.getName()), context(namedGraph));
			}
			else if (input.isStream()) {				
				conn.add(input.getStream(), "", RDFFormat.RDFXML, context(namedGraph));				
			}
			else if (input.isStatements()) {				
				conn.add(input.getStatements(), context(namedGraph));				
			}
			else {
				throw new AssertionError();
			}
		}
		finally {
			closeQuietly(conn);
		}
    }

	@Override
    public void removeData(RDFInput input, String namedGraph) throws Exception {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			Iterable<? extends Statement> stmts;
			if (input.isStatements()) {				
				stmts = input.getStatements();				
			}
			else {
				RDFFormat format = RDFFormat.RDFXML;
				InputStream stream = null;				
				if (input.isFile()) {
					File file = input.getFile();
					format = RDFFormat.forFileName(file.getName());
					stream = new FileInputStream(file);
				}
				else if (input.isStream()) {				
					stream = input.getStream();				
				} 
				else {
					throw new AssertionError();
				}
				
				StatementCollector collector = new StatementCollector();
				RDFParser parser = Rio.createParser(format);
				parser.setRDFHandler(collector);
				parser.parse(stream, "");
				stmts = collector.getStatements();
			}
			
			conn.remove(stmts, context(namedGraph));
		}
		finally {
			closeQuietly(conn);
		}
	}
	
	@Override
	public void export(RDFHandler handler, String namedGraph) throws Exception {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();			
			conn.export(new NoContextHandler(handler), context(namedGraph));
		}
		finally {
			closeQuietly(conn);
		}
	}

	@Override
    public void clear(String namedGraph) throws Exception {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();			
			conn.clear(context(namedGraph));
		}
		finally {
			closeQuietly(conn);
		}
    }
	
	@Override
	public void validate(RDFInput constraints, String namedGraph) throws Exception {
	    throw new UnsupportedOperationException();
	}

	@Override
    public long size() throws Exception {
		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();			
			return conn.size();
		}
		finally {
			closeQuietly(conn);
		}
    }
	
	private static class NoContextHandler implements RDFHandler {
		private static final ValueFactory VF = ValueFactoryImpl.getInstance();
		private final RDFHandler delegate;

		private NoContextHandler(RDFHandler delegate) {
	        super();
	        this.delegate = delegate;
        }

		@Override
        public void endRDF() throws RDFHandlerException {
	        delegate.endRDF();
        }

		@Override
        public void handleComment(String comment) throws RDFHandlerException {
	        delegate.handleComment(comment);
        }

		@Override
        public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
	        delegate.handleNamespace(prefix, uri);
        }

		@Override
        public void handleStatement(Statement stmt) throws RDFHandlerException {
	        delegate.handleStatement(VF.createStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject()));
	        
        }

		@Override
        public void startRDF() throws RDFHandlerException {
	        delegate.startRDF();
        }
	}
}
