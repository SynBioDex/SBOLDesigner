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

import org.openrdf.model.URI;

import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Listable;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Tag;

public class SPARQLRepository extends SPARQLRef implements Repository {	
	private final Listable<Branch> branches;	
	
	private final Listable<Tag> tags;
	
	public SPARQLRepository(SPARQLEndpoint endpoint, URI uri, String name) {
		super(endpoint, uri, name);
		
		branches = new SPARQLListable<Branch>(propertyQueryNamed(Terms.hasBranch, branchMapper));
		
		tags = new SPARQLListable<Tag>(propertyQueryNamed(Terms.hasTag, tagMapper));
    }
	
	@Override
    public Listable<Branch> branches() {
	    return branches;
    }
	
	@Override
    public Listable<Tag> tags() {
	    return tags;
    }
}
