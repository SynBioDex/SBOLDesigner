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

package com.clarkparsia.versioning.sparql;

import java.util.Calendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Infos;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.Tag;
import com.google.common.base.Function;

public class SPARQLRef implements Ref {		
	protected static final String SELECT =
			"PREFIX : <" + Terms.NAMESPACE + ">\n" +
			"PREFIX dc: <" + Terms.DC + ">\n" +
			"PREFIX foaf: <" + Terms.FOAF + ">\n" +
			"PREFIX rdfs: <" + RDFS.NAMESPACE + ">\n" +
			"SELECT *\n" + 
			"FROM <" + Terms.Metadata + "> \n" +
			"WHERE ";

	protected final SPARQLEndpoint endpoint;
	protected final String name;
	protected final URI uri;	
	protected ActionInfo info;
	
    protected final Function<Function<String,Value>,Repository> repoMapper = new Function<Function<String,Value>,Repository>() {			
    	@Override
    	public Repository apply(Function<String,Value> bindings) {
    		return new SPARQLRepository(endpoint, (URI) bindings.apply("x"), ((Literal) bindings.apply("name")).getLabel());
    	}
    };
	
	protected final Function<Function<String,Value>,Branch> branchMapper = new Function<Function<String,Value>,Branch>() {			
    	@Override
    	public Branch apply(Function<String,Value> bindings) {
    		return new SPARQLBranch(endpoint, (URI) bindings.apply("x"), ((Literal) bindings.apply("name")).getLabel());
    	}
    };
	
	protected final Function<Function<String,Value>,Revision> revisionMapper = new Function<Function<String,Value>,Revision>() {			
    	@Override
    	public Revision apply(Function<String,Value> bindings) {
    		return new SPARQLRevision(endpoint, (URI) bindings.apply("x"));
    	}
    };
	
	protected final Function<Function<String,Value>,Tag> tagMapper = new Function<Function<String,Value>,Tag>() {			
    	@Override
    	public Tag apply(Function<String,Value> bindings) {
    		return new SPARQLTag(endpoint, (URI) bindings.apply("x"), ((Literal) bindings.apply("name")).getLabel());
    	}
    };
	
	protected final Function<Function<String,Value>,ActionInfo> infoMapper = new Function<Function<String,Value>,ActionInfo>() {			
    	@Override
    	public ActionInfo apply(Function<String,Value> bindings) {
    		URI user = (URI) bindings.apply("user");
    		Value userName = bindings.apply("userName");
    		URI userEmail = (URI) bindings.apply("userEmail");
    		String msg = ((Literal) bindings.apply("msg")).getLabel();
    		Calendar time = ((Literal) bindings.apply("time")).calendarValue().toGregorianCalendar();
    		return Infos.forAction(Infos.forPerson(user, userName == null ? "" : userName.stringValue(), userEmail), msg, time);
    	}
    };
	
	protected static final Function<Function<String,Value>,Value> valueMapper = new Function<Function<String,Value>,Value>() {			
    	@Override
    	public Value apply(Function<String,Value> bindings) {
    		return bindings.apply("x");
    	}
    };
	
	public SPARQLRef(SPARQLEndpoint endpoint, URI baseURI, String name) {
	    this.endpoint = endpoint;
	    this.uri = baseURI;
	    this.name = name;
    }

	@Override
    public String getName() {
	    return name;
    }

	@Override
    public URI getURI() {
	    return uri;
    }

	@Override
    public ActionInfo getActionInfo() {
	    if (info == null) {
	    	String query = SELECT + " {" +
	    		"  ?x dc:creator ?user ;\n" +
	    		"     rdfs:comment ?msg ;\n" +
	    		"     dc:date ?time .\n" +
	    		"  OPTIONAL { ?user rdfs:label ?userName } \n" +
	    		"  OPTIONAL { ?user foaf:mbox ?userEmail } \n" +
	    		"} \n" +
	    		"LIMIT 1";
	    		
			info = SPARQLQuery.create(endpoint, infoMapper, query).binding("x", uri).executeSelectOnlyElement();
	    }
	    return info;
    }
	
	@Override
	public SPARQLEndpoint getEndpoint() {
		return endpoint;
	}

	protected <T> SPARQLQuery<T> propertyQuery(URI prop, Function<Function<String,Value>, T> function) {
		return propertyQuery(prop, function, false, false);
	}

	protected <T> SPARQLQuery<T> propertyQueryNamed(URI prop, Function<Function<String,Value>, T> function) {
		return propertyQuery(prop, function, false, true);
	}

	protected <T> SPARQLQuery<T> propertyQueryInv(URI prop, Function<Function<String,Value>, T> function) {
		return propertyQuery(prop, function, true, false);
	}

	protected <T> SPARQLQuery<T> propertyQueryInvNamed(URI prop, Function<Function<String,Value>, T> function) {
		return propertyQuery(prop, function, true, true);
	}

	private <T> SPARQLQuery<T> propertyQuery(URI prop, Function<Function<String,Value>, T> function, boolean inverse, boolean named) {
		StringBuilder query = new StringBuilder(SELECT);
		query.append("{\n");
		if (inverse) {
			query.append("?x ?p ?uri");
		}
		else {
			query.append("?uri ?p ?x");
		}
		query.append(" .\n");
		if (named) {
			query.append("?x rdfs:label ?name .\n");
		}		
		query.append("}");
		return SPARQLQuery.create(endpoint, function, query.toString()).binding("uri", uri).binding("p", prop);
	}
	
	@Override
    public boolean equals(Object obj) {
	    if (!(obj instanceof Ref)) {
	    	return false;
	    }
	    return uri.equals(((Ref) obj).getURI());
    }

	@Override
    public int hashCode() {
	    return uri.hashCode();
    }

	@Override
    public String toString() {
	    return getClass().getSimpleName() + "(" + uri + ")";
    }
	
	
}
