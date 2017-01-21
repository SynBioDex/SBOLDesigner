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

package com.clarkparsia.sbol;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary terms used by the Sublime extensions of SBOL serialization. Note that these extensions should be in a
 * different namespace than the official SBOL namespace otherwise regular serializations will not be officially
 * compliant with the SBOL syntax.
 * 
 * @author Evren Sirin
 */
public class SublimeVocabulary {
	private SublimeVocabulary() {
	}

	private static ValueFactory FACTORY = ValueFactoryImpl.getInstance();

	public static final String NAMESPACE = "http://clarkparsia.com/sublime#";

	private static final URI uri(String localName) {
		return FACTORY.createURI(NAMESPACE + localName);
	}

	public static final URI SequenceAnalysis = uri("SequenceAnalysis");
	public static final URI design = uri("design");
	public static final URI dataAnalyzed = uri("dataAnalyzed");
	public static final URI variantFound = uri("variantFound");
	public static final URI conclusion = uri("conclusion");

	public static final URI SequencingData = uri("SequencingData");
	public static final URI orderNumber = uri("orderNumber");
	public static final URI dataFile = uri("datafile");
	public static final URI date = uri("date");
	
	public static final URI SequenceVariant = uri("SequenceVariant");
	public static final URI ambiguous = uri("ambiguous");
	public static final URI observedIn = uri("observedIn");
}
