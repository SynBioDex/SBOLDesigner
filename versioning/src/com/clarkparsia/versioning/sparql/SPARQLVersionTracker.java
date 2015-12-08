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


import java.util.List;
import java.util.UUID;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Listable;
import com.clarkparsia.versioning.PersonInfo;
import com.clarkparsia.versioning.RVT;
import com.clarkparsia.versioning.Repository;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SPARQLVersionTracker extends SPARQLRef implements RVT {
	
	private final Listable<Repository> repos = new SPARQLListable<Repository>(SELECT + "{ ?x a :Repository ; rdfs:label ?name }", endpoint, repoMapper);	
	
	public SPARQLVersionTracker(SPARQLEndpoint endpoint) {
		this(endpoint, false);
	}
	
	public SPARQLVersionTracker(SPARQLEndpoint endpoint, boolean init) {
		super(endpoint, getOrSetURI(endpoint, init), "");		
	}
	
	private static URI getOrSetURI(SPARQLEndpoint endpoint, boolean init) {
		List<Value> uris = SPARQLQuery.create(endpoint, valueMapper, SELECT + "{ ?x a :Metadata }").executeSelect();
		
		if (uris.size() > 1 || (uris.size() == 1 && !(uris.get(0) instanceof URI))) {
			throw new IllegalStateException("The metadata for this endpoint is corrupted");
		}
		
		if (uris.size() == 1) {
			return (URI) uris.get(0);
		}
		
		if (init) {			
			URI baseURI = Terms.uri(Terms.NAMESPACE, "system:" + UUID.randomUUID());
			try {
	            endpoint.addData(RDFInput.forStatements(Terms.stmt(baseURI, RDF.TYPE, Terms.System)), Terms.Metadata.stringValue());
            }
            catch (Exception e) {
	            throw new RuntimeException(e);
            }
			
			return baseURI;
		}
		else {			
			throw new IllegalArgumentException("The endpoint is not initialized");       
		}
	}
		
	@Override
    public Listable<Repository> repos() {
	    return repos;
    }

	@Override
    public Repository createRepo(String name, ActionInfo info) {
		Preconditions.checkState(!repos.contains(name), "Repository '%s' already exists", name);
		
		URI repoURI = Terms.unique("repository");
	    URI branchURI = Terms.unique("branch");
	    RDFInput additions = RDFInput.forStatements(
	    	Terms.stmt(repoURI, RDF.TYPE, Terms.Repository),
			Terms.stmt(repoURI, Terms.name, Terms.literal(name)),
			Terms.stmt(repoURI, Terms.creator, info.getAuthor().getURI()),
			Terms.stmt(repoURI, Terms.message, Terms.literal(info.getMessage())),
			Terms.stmt(repoURI, Terms.date, Terms.literal(info.getDate())),
			Terms.stmt(repoURI, Terms.hasBranch, branchURI),
			Terms.stmt(branchURI, RDF.TYPE, Terms.Branch),
			Terms.stmt(branchURI, Terms.name, Terms.literal(Branch.MASTER)),
			Terms.stmt(branchURI, Terms.creator, info.getAuthor().getURI()),
			Terms.stmt(branchURI, Terms.message, Terms.literal(Branch.MASTER)),
			Terms.stmt(branchURI, Terms.date, Terms.literal(info.getDate()))
		);
		
		try {
	        endpoint.addData(additions, Terms.Metadata.stringValue());
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
		
		return new SPARQLRepository(endpoint, repoURI, name);
    }

	@Override
    public void addPersonInfo(PersonInfo info) {
		try {
			List<Statement> removals = computePersonDataRemove(info.getURI());
			List<Statement> additions = computePersonDataAdd(info);
			
			endpoint.removeData(RDFInput.forStatements(removals), Terms.Metadata.stringValue());
	        endpoint.addData(RDFInput.forStatements(additions), Terms.Metadata.stringValue());
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
	    
    }

	private List<Statement> computePersonDataAdd(PersonInfo info) {
		URI personURI = info.getURI();
		
		Preconditions.checkNotNull(info.getURI(), "URI for person cannot be null");	
		
		List<Statement> additions = Lists.newArrayList();
		
		additions.add(Terms.stmt(personURI, RDF.TYPE, Terms.Person));
		if (info.getName() != null) {
			additions.add(Terms.stmt(personURI, Terms.name, Terms.literal(info.getName())));
		}
		if (info.getEmail() != null) {
			additions.add(Terms.stmt(personURI, Terms.email, info.getEmail()));
		}
		
		return additions;	    
    }
	
	private List<Statement> computePersonDataRemove(URI personURI) {
		String query = SELECT + " {" +
    					"  ?person a foaf:Person \n" +
    		    		"  { ?person rdfs:label ?name } \n" +
    		    		"  UNION \n" +
    		    		"  { ?person foaf:mbox ?email } \n" +
    		    		"}";
    		    		
    	List<Statement> removals = Lists.newArrayList();
		for (Function<String, Value> binding : SPARQLQuery.create(endpoint, query).binding("person", personURI).executeSelect()) {
			Value name = binding.apply("name");
			if (name != null) {
				removals.add(Terms.stmt(personURI, Terms.name, name));
			}
			Value email = binding.apply("email");
			if (email != null) {
				removals.add(Terms.stmt(personURI, Terms.email, email));
			}
		}
		
		return removals;		
	}
}
