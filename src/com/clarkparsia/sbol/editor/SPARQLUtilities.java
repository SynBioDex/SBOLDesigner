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

import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.util.SequenceOntology;

import com.clarkparsia.sbol.editor.dialog.UserCredentialsDialog;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.google.common.collect.Lists;

public class SPARQLUtilities {


	public static List<DnaComponent> findMatchingParts(final SPARQLEndpoint endpoint) {
		return findMatchingParts(endpoint, new Part(null, "All", "All parts")); 
	}

	public static List<DnaComponent> findMatchingParts(final SPARQLEndpoint endpoint, final Part part) {
		if (endpoint == null) {
			return Collections.emptyList();
		}
		
		final boolean findAllParts = (part.getType() == null);
		String partType = !findAllParts ? "<" + part.getType() + ">" : "?type . FILTER (regex(str(?type), \""
		                + SequenceOntology.NAMESPACE + ".*\"))";

		String query = "PREFIX :<http://sbols.org/v1#>\n" 
						+ "SELECT * WHERE {\n" 
						+ "  ?part a " + partType + "\n"
		                + "  OPTIONAL { ?part :displayId ?displayId }\n" 
						+ "  OPTIONAL { ?part :name ?name }\n"
		                + "  OPTIONAL { ?part :description ?desc }\n" 
						+ "  ?part :dnaSequence ?seq .\n"
		                + "  ?seq :nucleotides ?nucleotides .\n" 
						+ "}\n"
		                + "ORDER BY ?displayId\n" 
//						+ "LIMIT 50"
		                ;

//		System.out.format("Query for part %s (%s):%n%s%n", part.getDisplayId(), part.getName(), query);

		final List<DnaComponent> parts = Lists.newArrayList();
		try {
			endpoint.executeSelectQuery(query, new TupleQueryResultHandlerBase() {
				@Override
				public void handleSolution(BindingSet binding) throws TupleQueryResultHandlerException {
					String partURI = getBindingAsString(binding, "part");
					String name = getBindingAsString(binding, "name");
					String displayId = getBindingAsString(binding, "displayId");
					String description = getBindingAsString(binding, "desc");

					URI partTypeURI = findAllParts ? URI.create(getBindingAsString(binding, "type")) : part.getType();

					DnaComponent comp = SBOLFactory.createDnaComponent();
					comp.setURI(URI.create(partURI));
					comp.setName(name);
					comp.setDisplayId(displayId);
					comp.setDescription(description);
					comp.addType(partTypeURI);

					String seqURI = getBindingAsString(binding, "seq");
					String nucleotides = getBindingAsString(binding, "nucleotides");
					DnaSequence seq = SBOLFactory.createDnaSequence();
					seq.setURI(URI.create(seqURI));
					seq.setNucleotides(nucleotides);
					comp.setDnaSequence(seq);

					parts.add(comp);
				}
			});

			return parts;
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "There was an error retrieving parts");
			return Collections.emptyList();
		}
	}

	private static String getBindingAsString(BindingSet bindings, String name) {
		Binding binding = bindings.getBinding(name);
		return binding == null ? null : binding.getValue().stringValue();
	}
	
	public static boolean setCredentials(SPARQLEndpoint endpoint) {
		if (endpoint instanceof StardogEndpoint) {
			StardogEndpoint stardog = (StardogEndpoint) endpoint;
			if (stardog.isAnonymous()) {
				PasswordAuthentication credentials = new UserCredentialsDialog(null).getInput();
				if (credentials == null) {
					return false;
				}
				stardog.setCredentials(credentials.getUserName(), new String(credentials.getPassword()));
			}
		}
		
		return true;
	}
}
