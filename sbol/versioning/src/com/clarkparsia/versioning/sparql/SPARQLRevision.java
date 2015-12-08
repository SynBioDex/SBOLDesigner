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

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;

import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.Tag;

public class SPARQLRevision extends SPARQLRef implements Revision {
	public SPARQLRevision(SPARQLEndpoint endpoint, URI uri) {
	    super(endpoint, uri, uri.getLocalName());
    }
	
	@Override
	public List<Revision> getParents() {
		return propertyQuery(Terms.hasParent, revisionMapper).executeSelect();
	}
	
	@Override
	public List<Tag> getTags() {
		return propertyQueryInvNamed(Terms.hasRevision, tagMapper).executeSelect();
	}

	@Override
    public Branch getBranch() {
		return propertyQueryNamed(Terms.hasBranch, branchMapper).executeSelectOnlyElement();
    }

	@Override
    public void checkout(RDFHandler handler) {
		try {
	        endpoint.export(handler, getURI().stringValue());
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }

	@Override
    public SPARQLBranch branch(String name, ActionInfo info) {
	    URI branchURI = Terms.unique("branch");
	    
	    Repository repo = getBranch().getRepository();	    
	    RDFInput additions = RDFInput.forStatements(
	    	Terms.stmt(repo.getURI(), Terms.hasBranch, branchURI),
			Terms.stmt(branchURI, RDF.TYPE, Terms.Branch),
			Terms.stmt(branchURI, Terms.name, Terms.literal(name)),
			Terms.stmt(branchURI, Terms.creator, info.getAuthor().getURI()),
			Terms.stmt(branchURI, Terms.message, Terms.literal(info.getMessage())),
			Terms.stmt(branchURI, Terms.date, Terms.literal(info.getDate())),
			Terms.stmt(branchURI, Terms.hasHead, uri),
			Terms.stmt(branchURI, Terms.hasParent, uri)
		);
		
		try {
	        endpoint.addData(additions, Terms.Metadata.stringValue());
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }		
	    	    
	    return new SPARQLBranch(endpoint, branchURI, name);
    }

	@Override
    public SPARQLTag tag(String name, ActionInfo info) {
	    URI tagURI = Terms.unique("tag");
	    
	    Repository repo = getBranch().getRepository();	
	    RDFInput additions = RDFInput.forStatements(
	    	Terms.stmt(repo.getURI(), Terms.hasTag, tagURI),
			Terms.stmt(tagURI, RDF.TYPE, Terms.Tag),
			Terms.stmt(tagURI, Terms.name, Terms.literal(name)),
			Terms.stmt(tagURI, Terms.creator, info.getAuthor().getURI()),
			Terms.stmt(tagURI, Terms.message, Terms.literal(info.getMessage())),
			Terms.stmt(tagURI, Terms.date, Terms.literal(info.getDate())),
			Terms.stmt(tagURI, Terms.hasRevision, uri)
		);
		
		try {
	        endpoint.addData(additions, Terms.Metadata.stringValue());
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }		
	    	    
	    return new SPARQLTag(endpoint, tagURI, name);
    }
}
