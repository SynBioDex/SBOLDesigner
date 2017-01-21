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

import org.openrdf.model.Value;

import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.Listable;
import com.google.common.base.Function;

public class SPARQLListable<T> implements Listable<T> {
	private final SPARQLQuery<T> query;
	
	public SPARQLListable(String query, SPARQLEndpoint endpoint, Function<Function<String,Value>,T> function) {
	    this.query = SPARQLQuery.create(endpoint, function, query);
    }
	
	public SPARQLListable(SPARQLQuery<T> query) {
	    this.query = query;
    }

	@Override
    public T get(String name) {
		List<T> results = query.binding("name", name).executeSelect();
		int size = results.size();
		if (size == 0) {
			throw new IllegalArgumentException("Not found: "+ name);
		}
		else if (size > 1) {
			throw new IllegalArgumentException("Invalid data, multiple instances found for: "+ name);
		}
		return results.get(0);
    }

	@Override
    public boolean contains(String name) {
		return !query.binding("name", name).executeSelect().isEmpty();
	}
	
	@Override
	public List<T> list() {
		return query.executeSelect();
	}
}
