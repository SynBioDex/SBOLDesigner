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

import java.util.Arrays;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.helpers.StatementCollector;

import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SPARQLBranch extends SPARQLRef implements Branch {
	public SPARQLBranch(SPARQLEndpoint endpoint, URI baseURI, String name) {
	    super(endpoint, baseURI, name);
    }
	
	@Override
	public Revision getParent() {
		return propertyQueryNamed(Terms.hasParent, revisionMapper).executeSelectOnlyElement(null);
	}

	@Override
	public Revision getHead() {
		return propertyQuery(Terms.hasHead, revisionMapper).executeSelectOnlyElement();
	}

	@Override
	public Revision getTail() {
    	String query = SELECT + "{" +
    		    		"  ?x a :Revision .\n" +
    		    		"  ?x :hasBranch ?uri .\n" +
    		    		"  FILTER NOT EXISTS {\n" +
    		    		"     ?x :hasParent ?parentRev .\n" +
    		    		"     ?parentRev :hasBranch ?uri .\n" +
    		    		"  }\n" +
    		    		"}";
		return SPARQLQuery.create(endpoint, revisionMapper, query).binding("uri", uri).executeSelectOnlyElement();
	}

	@Override
	public Revision commit(RDFInput input, ActionInfo info) {
        Revision headRevision = propertyQuery(Terms.hasHead, revisionMapper).executeSelectOnlyElement(null);
        Iterable<Revision> parents = (headRevision == null) ? ImmutableList.<Revision>of() : ImmutableList.<Revision>of(headRevision); 
        return commit(input, info, headRevision, parents);
	}

	@Override
	public Revision merge(Revision revision, ActionInfo info) {
    	Iterable<Statement> revStmts = checkout(revision); 
        Iterable<Statement> mergeStmts = revStmts; 
        
        Revision headRevision = getHead();        
        Revision commonAncestor = getAncestorInBranch(revision, this);
        
        boolean fastForward = commonAncestor.equals(headRevision); 
        if (!fastForward) {
        	Iterable<Statement> ancestorStmts = checkout(commonAncestor);
        	RDFDiff diff = RDFDiff.compute(ancestorStmts, revStmts);
        	Iterable<Statement> headStmts = checkout(headRevision);
        	mergeStmts = diff.apply(headStmts);
        }
        
        return commit(RDFInput.forStatements(mergeStmts), info, headRevision, Arrays.asList(headRevision, revision));
	}
	
	private Iterable<Statement> checkout(Revision revision) {
		StatementCollector collector = new StatementCollector();
    	revision.checkout(collector);
    	return collector.getStatements(); 
	}

	private Revision getAncestorInBranch(Revision startRev, Branch targetBranch) {
    	String query = SELECT + "{" +
    		    		"  ?startRev :hasParent+ ?x .\n" +
    		    		"  ?x :hasBranch ?targetBranch .\n" +
    		    		"  ?x dc:date ?date .\n" +
    		    		"}\n" +
    		    		"ORDER BY DESC(?date)\n" +
    		    		"LIMIT 1";
		return SPARQLQuery.create(endpoint, revisionMapper, query)
				.binding("startRev", startRev.getURI())
				.binding("targetBranch", targetBranch.getURI())
				.executeSelectOnlyElement();
	}
	
	private Revision commit(RDFInput input, ActionInfo info, Revision headRevision, Iterable<Revision> parents) {
		try {
	        URI revisionURI = Terms.unique("revision");
	        
	        if (headRevision != null) {
	        	RDFInput removals = RDFInput.forStatements(
	        		Terms.stmt(uri, Terms.hasHead, headRevision.getURI())
	        	);
	        	endpoint.removeData(removals, Terms.Metadata.stringValue());	
	        }
	        
	        List<Statement> additions = Lists.newArrayList(
	        	Terms.stmt(uri, Terms.hasHead, revisionURI),
	        	Terms.stmt(revisionURI, RDF.TYPE, Terms.Revision),
	        	Terms.stmt(revisionURI, Terms.hasBranch, uri),
	        	Terms.stmt(revisionURI, Terms.creator, info.getAuthor().getURI()),
	        	Terms.stmt(revisionURI, Terms.message, Terms.literal(info.getMessage())),
	        	Terms.stmt(revisionURI, Terms.date, Terms.literal(info.getDate()))
	        );
	        
	        for (Revision parent : parents) {
	        	additions.add(Terms.stmt(revisionURI, Terms.hasParent, parent.getURI()));
	        }
	        
	        endpoint.addData(RDFInput.forStatements(additions), Terms.Metadata.stringValue());
	        
	        Revision revision = new SPARQLRevision(endpoint, revisionURI);		
	        endpoint.addData(input, revisionURI.stringValue());
	        
	        return revision;
        }
        catch (Exception e) {
	        throw new RuntimeException(e);
        }
	}

	public Repository getRepository() {
		return propertyQueryInvNamed(Terms.hasBranch, repoMapper).executeSelectOnlyElement();
    }

}
