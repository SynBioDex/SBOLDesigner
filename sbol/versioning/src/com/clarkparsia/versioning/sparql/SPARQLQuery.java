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
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;

import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SPARQLQuery<T> {
	private final SPARQLEndpoint endpoint;
	private final String query;
	private final Function<Function<String,Value>, T> transformer;
	private final Map<String,Value> bindings;
	
	private SPARQLQuery(SPARQLEndpoint endpoint, String query, Function<Function<String,Value>, T> transformer) {
		this.endpoint = endpoint;
	    this.query = query;
	    this.transformer = transformer;
	    this.bindings = Maps.newHashMap();
    }
	
	public SPARQLQuery<T> binding(String var, Value value) {
		bindings.put(var, value);
		return this;
	}
	
	public SPARQLQuery<T> binding(String var, String value) {
		return binding(var, Terms.literal(value));
	}

	public List<T> executeSelect() {
		String query = query();
//		System.out.println(query);
		
		final List<T> result = Lists.newArrayList();

		try {
	        endpoint.executeSelectQuery(query, new TupleQueryResultHandlerBase() {
	        	@Override
	        	public void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException {
	        		Function<String,Value> varBindings = new Function<String,Value>() {
						@Override
                        public Value apply(String varName) {
	                        Value value = bindings.get(varName);
	                        return value != null ? value : bindingSet.getValue(varName);
                        }	        			
	        		};
	        		result.add(transformer.apply(varBindings));
	        	}
	        });
        }
        catch (QueryEvaluationException e) {
        	throw new RuntimeException(e);
        }

		return result;
	}

	public T executeSelectOnlyElement() {
		return Iterables.getOnlyElement(executeSelect());
	}

	public T executeSelectOnlyElement(T defaultValue) {
		return Iterables.getOnlyElement(executeSelect(), defaultValue);
	}
	
	public boolean executeAsk() {
		try {
	        return endpoint.executeAskQuery(query());
        }
        catch (QueryEvaluationException e) {
	        throw new RuntimeException(e);
        }
	}
	
	private String query() {
		if (bindings.isEmpty()) {
			return query;
		}
		
		String result = query;
		for (Entry<String,Value> binding : bindings.entrySet()) {
			result = result.replace("?" + binding.getKey(), str(binding.getValue()));
		}
		
		return result;
	}
	
	private String str(Value s) {
		if (s instanceof URI) {
			return "<" + s + ">";
		}
		else if (s instanceof BNode) {
			return s.toString();
		}
		else if (s instanceof Literal) {
			return s.toString();
		}
		
		throw new AssertionError();
	}
	
	public static final SPARQLQuery<Function<String,Value>> create(SPARQLEndpoint endpoint, String query) {
		return new SPARQLQuery<Function<String,Value>>(endpoint, query, Functions.<Function<String,Value>> identity());
	}
	
	public static final <T> SPARQLQuery<T> create(SPARQLEndpoint endpoint, Function<Function<String,Value>, T> transformer, String query) {
		return new SPARQLQuery<T>(endpoint, query, transformer);
	}
}
