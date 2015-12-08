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

package com.clarkparsia.sbol.editor;

import java.io.Serializable;

import com.clarkparsia.sbol.editor.sparql.LocalEndpoint;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.google.common.base.Preconditions;

public class Registry implements Serializable {
	private final String name;
	private final String description;
	private final String url;
	
	public static final Registry BUILT_IN = new Registry("Built-in parts",
	                "Built-in registry with minimal set of parts", 
	                Registries.class.getResource("cmyk_parts.rdf").toString());

	public static final Registry SBPKB = new Registry("SBPkb (Cloud)",
	                "The Standard Biological Parts knowledgebase (SBPkb) is a Semantic Web resource " +
	                "which uses SBOL-semantic to represent standard biological parts from the Registry " +
	                "of Standard Biological Parts at MIT.", 
	                "http://ec2-174-129-47-60.compute-1.amazonaws.com:5822/SBPkb");
	

	public Registry(String name, String description, String url) {
		Preconditions.checkNotNull(name, "Name cannot be null");
		Preconditions.checkNotNull(url, "URL cannot be null");
	    this.name = name;
	    this.description = description;
	    this.url = url;
    }
	
	public String getName() {
    	return name;
    }
	
	public String getDescription() {
    	return description;
    }
	
	public String getURL() {
    	return url;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (!(obj instanceof Registry))
		    return false;
	    Registry that = (Registry) obj;
	    return this.url.equals(that.url);
    }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((url == null) ? 0 : url.hashCode());
	    return result;
    }
		
	public boolean isBuiltin() {
		return this.equals(Registry.BUILT_IN);
	}
	
	public SPARQLEndpoint createEndpoint() {
		return isBuiltin() ? new LocalEndpoint(url) : new StardogEndpoint(url);
	}

	@Override
    public String toString() {
	    return isBuiltin() ? name : name + " (" + url + ")";
    }
}
