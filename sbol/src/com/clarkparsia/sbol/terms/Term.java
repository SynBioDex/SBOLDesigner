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

package com.clarkparsia.sbol.terms;

import java.util.List;

import com.google.common.collect.Lists;

public class Term {
	String label;
	String uri;
	final List<Term> subclasses = Lists.newArrayList();

	public Term() {			
	}
	
	public Term(String uri, String label) {
        this.uri = uri;
        this.label = label;
    }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public void addSubClass(Term term) {
		subclasses.add(term);
	}

	public List<Term> getSubClasses() {
		return subclasses;
	}
	
	public String toString() {
		return label == null ? uri : label;
	}
}