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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.openrdf.model.Statement;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLParser;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParser;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.google.common.io.ByteStreams;

/**
 * 
 * @author Evren Sirin
 */
public class StardogEndpoint extends AbstractEndpoint {
	private final SPARQLResultsXMLParser tupleParser = new SPARQLResultsXMLParser();
	private final SPARQLBooleanXMLParser booleanParser = new SPARQLBooleanXMLParser();
	private final RDFParserRegistry registry = RDFParserRegistry.getInstance();

	private String url;

	private HttpClient client;
	
	private boolean isAnonymous;

	public StardogEndpoint(String url) {
		this(url, "anonymous", "anonymous");
	}
	
	public StardogEndpoint(String url, String username, String passwd) {
		this.url = url;

		HttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20);
		params.setStaleCheckingEnabled(false);
		manager.setParams(params);

		HttpClientParams clientParams = new HttpClientParams();

		client = new HttpClient(clientParams, manager);

		setCredentials(username, passwd);
	}
	
	public String getURL() {
		return url;
	}
	
	public void setCredentials(String username, String passwd) {
		try {
	        client.getState().setCredentials(new AuthScope(new URL(url).getHost(), -1, null),
	                        new UsernamePasswordCredentials(username, passwd));
	        isAnonymous = username.equals("anonymous");
        }
        catch (MalformedURLException e) {
	        System.err.println("Cannot set the crenetials for the malformed URL: " + url);
        }
	}
	
	public boolean isAnonymous() {
		return isAnonymous;
	}

	@Override
	public void close() {
		client = null;
	}

	@Override
	public void executeSelectQuery(String query, TupleQueryResultHandler handler) throws QueryEvaluationException {
		try {
			boolean complete = false;
			HttpMethod response = executeQuery(query);
			try {
				tupleParser.setTupleQueryResultHandler(handler);
				byte[] bytes = response.getResponseBody();
				tupleParser.parse(new ByteArrayInputStream(bytes));
				complete = true;
			}
			catch (HttpException e) {
				throw new QueryEvaluationException(e);
			}
			catch (QueryResultParseException e) {
				throw new QueryEvaluationException(e);
			}
			catch (TupleQueryResultHandlerException e) {
				throw new QueryEvaluationException(e);
			}
			finally {
				if (!complete) {
					response.abort();
				}
			}
		}
		catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public boolean executeAskQuery(String query) throws QueryEvaluationException {
		try {
			Boolean result = null;
			HttpMethod response = executeQuery(query);
			try {
				InputStream in = response.getResponseBodyAsStream();
				result = booleanParser.parse(in);
				return result.booleanValue();
			}
			catch (HttpException e) {
				throw new QueryEvaluationException(e);
			}
			catch (QueryResultParseException e) {
				throw new QueryEvaluationException(e);
			}
			finally {
				if (result == null) {
					response.abort();
				}
			}
		}
		catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}
	
	private String startTransaction() throws Exception {
		PostMethod post = new PostMethod(url + "/transaction/begin");
		execute(post);
		return post.getResponseBodyAsString();
	}
	
	private void commitTransaction(String txId) throws Exception {
		PostMethod post = new PostMethod(url + "/transaction/commit/" + txId);
		execute(post);
	}

	@Override
	public void addData(RDFInput input, String namedGraph) throws Exception {
		String txId = startTransaction();
		
		RequestEntity entity = createEntity(input);		
		PostMethod post = new PostMethod(url + "/" + txId + "/add");
		if (namedGraph != null) {
			post.setQueryString(new NameValuePair[] { new NameValuePair("graph-uri", namedGraph) });
		}
		post.setRequestEntity(entity);

		execute(post);
		
		commitTransaction(txId);
	}

	@Override
	public void removeData(RDFInput input, String namedGraph) throws Exception {
		String txId = startTransaction();
		
		RequestEntity entity = createEntity(input);		
		PostMethod post = new PostMethod(url + "/" + txId + "/remove");
		if (namedGraph != null) {
			post.setQueryString(new NameValuePair[] { new NameValuePair("graph-uri", namedGraph) });
		}
		post.setRequestEntity(entity);

		execute(post);
		
		commitTransaction(txId);
	}
	
	private RequestEntity createEntity(RDFInput input) throws IOException {
		if (input.isFile()) {
			File file = input.getFile();
			return new FileRequestEntity(file, input.getFormat().getDefaultMIMEType());
		}
		else if (input.isStream()) {				
			return new InputStreamRequestEntity(input.getStream(), input.getFormat().getDefaultMIMEType());				
		}
		else if (input.isStatements()) {				
			return createEntity(input.getStatements());				
		}
		else {
			throw new AssertionError();
		}
	}

	private ByteArrayRequestEntity createEntity(Iterable<? extends Statement> theGraph) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, out);
			writer.startRDF();
			for (Statement stmt : theGraph) {
				writer.handleStatement(stmt);
			}
			writer.endRDF();
			return new ByteArrayRequestEntity(out.toByteArray(), RDFFormat.TURTLE.getDefaultMIMEType());
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	private NameValuePair graphParam(String namedGraph) {
		return (namedGraph == null) ? new NameValuePair("default", "") : new NameValuePair("graph", namedGraph);
	}

	@Override
	public void export(RDFHandler handler, String namedGraph) throws HttpException, IOException, QueryEvaluationException {
		boolean complete = false;
		try {
			GetMethod response = new GetMethod(url);
			response.setQueryString(new NameValuePair[] { graphParam(namedGraph) });
			execute(response);
			try {
				RDFParser parser = getParser(response);
				parser.setRDFHandler(handler);
				parser.parse(response.getResponseBodyAsStream(), url);
				complete = true;
			}
			catch (HttpException e) {
				throw new QueryEvaluationException(e);
			}
			catch (RDFParseException e) {
				throw new QueryEvaluationException(e);
			}
			catch (RDFHandlerException e) {
				throw new QueryEvaluationException(e);
			}
			finally {
				if (!complete)
					response.abort();
			}
		}
	    catch (IOException e) {
	      throw new QueryEvaluationException(e);
	    }
	}

	private RDFParser getParser(HttpMethod response) {
		for (Header header : response.getResponseHeaders("Content-Type")) {
			for (HeaderElement headerEl : header.getElements()) {
				String mimeType = headerEl.getName();
				if (mimeType != null) {
					RDFFormat format = (RDFFormat) this.registry.getFileFormatForMIMEType(mimeType);

					RDFParserFactory factory = (RDFParserFactory) this.registry.get(format);
					if (factory != null)
						return factory.getParser();
				}
			}
		}
		throw new UnsupportedQueryResultFormatException("No parser factory available for this graph query result format");
	}

	@Override
    public void clear() throws Exception {
		String txId = startTransaction();
		
		PostMethod post = new PostMethod(url + "/" + txId + "/clear");

		execute(post);
		
		commitTransaction(txId);
	}

	@Override
	public void clear(String namedGraph) throws HttpException, IOException, QueryEvaluationException {
		DeleteMethod delete = new DeleteMethod(url);
		delete.setQueryString(new NameValuePair[] { graphParam(namedGraph) });
		execute(delete);
	}

	protected HttpMethodBase executeQuery(String query) throws HttpException, IOException, QueryEvaluationException {
		PostMethod post = new PostMethod(url + "/query");
		post.addParameter("query", query);
		post.addRequestHeader("Accept", tupleParser.getTupleQueryResultFormat().getDefaultMIMEType());

		execute(post);

		return post;
	}

	protected void execute(HttpMethodBase post) throws HttpException, IOException, QueryEvaluationException {
		post.setDoAuthentication(true);

		boolean completed = false;
		try {
			int resultCode = client.executeMethod(post);
			if (resultCode >= 400) {
				throw new HttpException("Code: " + resultCode + " " + post.getResponseBodyAsString());
			}
			completed = true;
		}
		finally {
			if (!completed) {
				post.abort();
			}
		}
	}
	
	@Override
	public void validate(RDFInput constraints, String namedGraph) throws Exception {
		RequestEntity entity = createEntity(constraints);		
		PostMethod post = new PostMethod(url + "/icv/violations");
		if (namedGraph != null) {
			post.setQueryString(new NameValuePair[] { new NameValuePair("graph-uri", namedGraph) });
		}
		post.setRequestEntity(entity);

		execute(post);
		
		ByteStreams.copy(post.getResponseBodyAsStream(), System.out);
	}

	@Override
	public long size() throws HttpException, IOException, QueryEvaluationException {
		GetMethod get = new GetMethod(url + "/size");

		execute(get);

		return Long.parseLong(get.getResponseBodyAsString());
	}
}
