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

package com.clarkparsia.sbol.editor.sparql;

import org.openrdf.rio.RDFHandler;

/**
 * @author Evren Sirin
 */
public abstract class AbstractEndpoint implements SPARQLEndpoint {
	@Override
    public void addData(RDFInput input) throws Exception {
		addData(input, null);
    }
	
	@Override
    public void removeData(RDFInput input) throws Exception {
		removeData(input, null);
    }
	
	@Override
	public void export(RDFHandler handler) throws Exception {
		export(handler, null);
	}

	@Override
    public void clear() throws Exception {
		clear(null);
	}
	
	@Override
	public void validate(RDFInput constraints) throws Exception {
	    validate(constraints, null);
	}
}
