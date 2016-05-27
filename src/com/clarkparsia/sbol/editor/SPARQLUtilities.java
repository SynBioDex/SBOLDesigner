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

import java.io.File;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;

import com.clarkparsia.sbol.editor.dialog.UserCredentialsDialog;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.google.common.collect.Lists;

public class SPARQLUtilities {

	public static List<ComponentDefinition> findMatchingParts(final SPARQLEndpoint endpoint) {
		return findMatchingParts(endpoint, new Part(null, "All", "All parts"));
	}

	/////////////////////////////////////////////////////////////////
	public static List<ComponentDefinition> findMatchingParts(final SPARQLEndpoint endpoint, final Part part) {
		SBOLDocument doc = importDoc();
		Object[] arr = doc.getComponentDefinitions().toArray();
		ArrayList<ComponentDefinition> list = new ArrayList<ComponentDefinition>();
		for (int i = 0; i < arr.length; i++) {
			list.add(((ComponentDefinition) arr[i]));
		}
		return list;
	}

	/**
	 * Prompts the user to choose a file and reads it, returning the output
	 * SBOLDocument. If the user cancels or the file in unable to be imported,
	 * returns null.
	 */
	private static SBOLDocument importDoc() {
		JFileChooser fc = new JFileChooser(new File("."));
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			SBOLDocument doc = null;
			try {
				SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
				SBOLReader.setCompliant(true);
				doc = SBOLReader.read(file);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "This file is unable to be imported: " + e1.getMessage());
				e1.printStackTrace();
			}
			return doc;
		}
		return null;
	}

	/**
	 * TODO Returns a list of CDs based on the endpoint (part location) and
	 * part. DEPRECATED by renaming to findMatchingPartsRemoved
	 */
	public static List<ComponentDefinition> findMatchingPartsRemoved(final SPARQLEndpoint endpoint, final Part part) {
		if (endpoint == null) {
			return Collections.emptyList();
		}

		final boolean findAllParts = (part.getRole() == null);
		String partType = !findAllParts ? "<" + part.getRole() + ">"
				: "?type . FILTER (regex(str(?type), \"" + SequenceOntology.NAMESPACE + ".*\"))";

		String query = "PREFIX :<http://sbols.org/v1#>\n" + "SELECT * WHERE {\n" + "  ?part a " + partType + "\n"
				+ "  OPTIONAL { ?part :displayId ?displayId }\n" + "  OPTIONAL { ?part :name ?name }\n"
				+ "  OPTIONAL { ?part :description ?desc }\n" + "  ?part :Sequence ?seq .\n"
				+ "  ?seq :nucleotides ?nucleotides .\n" + "}\n" + "ORDER BY ?displayId\n"
				// + "LIMIT 50"
				;

		// System.out.format("Query for part %s (%s):%n%s%n",
		// part.getDisplayId(), part.getName(), query);

		final List<ComponentDefinition> parts = Lists.newArrayList();
		try {
			// TODO This hangs, which causes find registry part to not work
			endpoint.executeSelectQuery(query, new TupleQueryResultHandlerBase() {
				@Override
				public void handleSolution(BindingSet binding) throws TupleQueryResultHandlerException {
					String partURI = getBindingAsString(binding, "part");
					String name = getBindingAsString(binding, "name");
					String displayId = getBindingAsString(binding, "displayId");
					String description = getBindingAsString(binding, "desc");

					URI partTypeURI = findAllParts ? URI.create(getBindingAsString(binding, "type")) : part.getRole();
					Set<URI> types = new HashSet<URI>();
					types.add(partTypeURI);

					ComponentDefinition comp;
					try {
						comp = SBOLFactory.createComponentDefinition(displayId, types);
						comp.setName(name);
						comp.setDescription(description);
						comp.addType(partTypeURI);

						String seqURI = getBindingAsString(binding, "seq");
						String nucleotides = getBindingAsString(binding, "nucleotides");
						Sequence seq = new Sequence(URI.create(seqURI).toString(), "no displayId", "", nucleotides,
								Sequence.IUPAC_DNA);
						comp.addSequence(seq);

						parts.add(comp);
					} catch (SBOLValidationException e) {
						e.printStackTrace();
					}

				}
			});

			return parts;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "There was an error retrieving parts");
			return Collections.emptyList();
		}
	}
	////////////////////////////////////////////////////////

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
