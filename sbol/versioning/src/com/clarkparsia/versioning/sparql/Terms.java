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
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

public class Terms {
	private static final ValueFactory VF = ValueFactoryImpl.getInstance();

	private Terms() {
	};

	public static final String NAMESPACE = "tag:rvt:";

	public static final URI Metadata = uri(NAMESPACE, "Metadata");

	public static final URI System = Metadata;

	public static final URI Repository = uri(NAMESPACE, "Repository");
	public static final URI hasBranch = uri(NAMESPACE, "hasBranch");
	
	public static final URI Branch = uri(NAMESPACE, "Branch");
	public static final URI hasParent = uri(NAMESPACE, "hasParent");
	public static final URI hasHead = uri(NAMESPACE, "hasHead");

	public static final URI Revision = uri(NAMESPACE, "Revision");
	
	public static final URI hasTag = uri(NAMESPACE, "hasTag");
	public static final URI Tag = uri(NAMESPACE, "Tag");
	public static final URI hasRevision = uri(NAMESPACE, "hasRevision");
	
	public static final URI name = RDFS.LABEL;

	public static final String DC = "http://purl.org/dc/elements/1.1/";
	
	public static final URI creator = uri(DC, "creator");
	public static final URI date = uri(DC, "date");
	public static final URI message = RDFS.COMMENT;
	

	public static final String FOAF = "http://xmlns.com/foaf/0.1/";
	
	public static final URI Person = uri(FOAF, "Person");
	public static final URI email = uri(FOAF, "mbox");
	
	public static URI uri(String uri) {
		return VF.createURI(uri);
	}
	
	public static URI uri(String namespace, String name) {
		return VF.createURI(namespace + name);
	}
	
	public static URI unique(String type) {
		return VF.createURI(NAMESPACE + type + ":" + UUID.randomUUID());		
	}
	
	public static Literal literal(String label) {
		return VF.createLiteral(label);		
	}
	
	public static Literal literal(Calendar date) {
		return VF.createLiteral(DatatypeConverter.printDateTime(date), XMLSchema.DATETIME);		
	}

	public static Statement stmt(Resource s, URI p, Value o) {
		return VF.createStatement(s, p, o);
	}
}
