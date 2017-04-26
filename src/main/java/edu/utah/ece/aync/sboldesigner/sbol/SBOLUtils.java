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

package edu.utah.ece.aync.sboldesigner.sbol;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import edu.utah.ece.aync.sboldesigner.sbol.editor.Part;
import edu.utah.ece.aync.sboldesigner.sbol.editor.SBOLEditorPreferences;

public class SBOLUtils {
	/**
	 * Returns an int which guarantees a unique URI. Pass in the parent CD (if
	 * dataType isn't a TopLevel), the displayId you want, the version (if
	 * dataType is a TopLevel), the type of object, and the SBOLDocument
	 * containing the design.
	 */
	public static String getUniqueDisplayId(ComponentDefinition comp, String displayId, String version, String dataType,
			SBOLDocument design) {
		// if can get using some displayId, then try the next number
		switch (dataType) {
		case "CD":
			for (int i = 1; true; i++) {
				if (i == 1 && design.getComponentDefinition(displayId, version) == null) {
					return displayId;
				}
				if (design.getComponentDefinition(displayId + i, version) == null) {
					return displayId + i;
				}
			}
		case "SequenceAnnotation":
			for (int i = 1; true; i++) {
				if (i == 1 && comp.getSequenceAnnotation(displayId) == null) {
					return displayId;
				}
				if (comp.getSequenceAnnotation(displayId + i) == null) {
					return displayId + i;
				}
			}
		case "SequenceConstraint":
			for (int i = 1; true; i++) {
				if (i == 1 && comp.getSequenceConstraint(displayId) == null) {
					return displayId;
				}
				if (comp.getSequenceConstraint(displayId + i) == null) {
					return displayId + i;
				}
			}
		case "Component":
			for (int i = 1; true; i++) {
				if (i == 1 && comp.getComponent(displayId) == null) {
					return displayId;
				}
				if (comp.getComponent(displayId + i) == null) {
					return displayId + i;
				}
			}
		case "Sequence":
			for (int i = 1; true; i++) {
				if (i == 1 && design.getSequence(displayId, version) == null) {
					return displayId;
				}
				if (design.getSequence(displayId + i, version) == null) {
					return displayId + i;
				}
			}
		case "Range":
			test: for (int i = 1; true; i++) {
				for (SequenceAnnotation sa : comp.getSequenceAnnotations()) {
					if (i == 1 && sa.getLocation(displayId) != null) {
						continue test;
					}
					if (sa.getLocation(displayId + i) != null) {
						continue test;
					}
				}
				// This will always return Range, Range2, Range3... etc,
				// skipping Range1
				return i == 1 ? displayId : displayId + i;
			}
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Creates an alphabetized String[] representing SO names of descendant
	 * roles based on the passed in part's role.
	 */
	public static String[] createRefinements(Part part) {
		SequenceOntology so = new SequenceOntology();
		String[] descendantNames;
		if (part.getRole() != null) {
			descendantNames = so.getDescendantNamesOf(part.getRole()).toArray(new String[0]);
			Arrays.sort(descendantNames);
		} else {
			descendantNames = new String[0];
		}
		String[] refine = new String[descendantNames.length + 1];
		refine[0] = "None";
		for (int i = 1; i < descendantNames.length + 1; i++) {
			refine[i] = descendantNames[i - 1];
		}
		return refine;
	}

	/**
	 * Returns a list of all roles of a CD that are descendants of the part's
	 * role.
	 */
	public static List<URI> getRefinementRoles(Identified comp, Part part) {
		ArrayList<URI> list = new ArrayList<URI>();
		SequenceOntology so = new SequenceOntology();
		Set<URI> roles;
		if (comp instanceof ComponentDefinition) {
			roles = ((ComponentDefinition) comp).getRoles();
		} else if (comp instanceof Component) {
			roles = ((Component) comp).getRoles();
		} else if (comp instanceof SequenceAnnotation) {
			roles = ((SequenceAnnotation) comp).getRoles();
		} else {
			return list;
		}
		for (URI r : roles) {
			// assumes the part role is always the first role in the list
			if (so.isDescendantOf(r, part.getRole())) {
				list.add(r);
			}
		}
		return list;
	}

	/**
	 * Prompts the user to choose a file and reads it, returning the output
	 * SBOLDocument. If the user cancels or the file in unable to be imported,
	 * returns null.
	 * 
	 * importPath is different from path
	 */
	public static SBOLDocument importDoc() {
		String path = Preferences.userRoot().node("path").get("importPath", setupFile().getPath());
		JFileChooser fc = new JFileChooser(new File(path));
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File directory = fc.getCurrentDirectory();
			Preferences.userRoot().node("path").put("importPath", directory.getPath());
			SBOLDocument doc = null;
			try {
				SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
				SBOLReader.setCompliant(true);
				doc = SBOLReader.read(fc.getSelectedFile());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "This file is unable to be imported: " + e1.getMessage());
				e1.printStackTrace();
			}
			return doc;
		}
		return null;
	}

	/**
	 * Prompts the user to choose a file and returns it. Returns null otherwise.
	 * 
	 * importPath is different from path
	 */
	public static File importFile() {
		String path = Preferences.userRoot().node("path").get("importPath", setupFile().getPath());
		JFileChooser fc = new JFileChooser(new File(path));
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File directory = fc.getCurrentDirectory();
			Preferences.userRoot().node("path").put("importPath", directory.getPath());
			return fc.getSelectedFile();
		}
		return null;
	}

	/**
	 * Gets the path from Preferences and returns a File
	 * 
	 * importPath is different from path
	 */
	public static File setupFile() {
		String path = Preferences.userRoot().node("path").get("path", "");
		return new File(path);
	}

	private static String getNucleotides(ComponentDefinition comp) {
		// Sequence seq = comp.getSequence();
		Sequence seq = null;
		if (comp.getSequences().size() > 0) {
			seq = comp.getSequenceByEncoding(Sequence.IUPAC_DNA);
		}
		return (seq == null) ? null : seq.getElements();
	}

	public static ComponentDefinition getRootCD(SBOLDocument doc) {
		return Iterators.getOnlyElement(
				Iterators.filter(doc.getRootComponentDefinitions().iterator(), ComponentDefinition.class), null);
	}

	/**
	 * Pass in the nucleotides and the SBOLDocument you want to create the
	 * Sequence in.
	 */
	private static Sequence createSequence(String nucleotides, SBOLDocument design) {
		try {
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, "Sequence", "1", "Sequence", design);
			return design.createSequence(uniqueId, "1", nucleotides, Sequence.IUPAC_DNA);
		} catch (SBOLValidationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isRegistryComponent(ComponentDefinition comp) {
		URI uri = comp.getIdentity();
		return uri != null
				&& !uri.toString().startsWith(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
	}

	/**
	 * Finds the UncoveredSequences of comp using SAs. This is all inside of
	 * design.
	 */
	public static Map<Integer, Sequence> findUncoveredSequences(ComponentDefinition comp,
			List<SequenceAnnotation> annotations, SBOLDocument design) {
		String sequence = SBOLUtils.getNucleotides(comp);
		if (sequence == null) {
			return ImmutableMap.of();
		}

		Map<Integer, Sequence> uncoveredSequences = Maps.newLinkedHashMap();
		int size = annotations.size();
		int location = 1;
		for (int i = 0; i < size; i++) {
			SequenceAnnotation ann = annotations.get(i);

			// Integer start = ann.getBioStart();
			// Integer end = ann.getBioEnd();
			Integer start = null;
			Integer end = null;
			Location loc = ann.getLocations().iterator().next();
			// TODO Only taking into account locations of type Range.
			if (loc instanceof Range) {
				Range range = (Range) loc;
				start = range.getStart();
				end = range.getEnd();
			}

			if (start == null || end == null) {
				return null;
			}

			if (start > location) {
				Sequence seq = SBOLUtils.createSequence(sequence.substring(location - 1, start - 1), design);
				uncoveredSequences.put(-i - 1, seq);
			}

			if (ann.isSetComponent()) {
				if (SBOLUtils.getNucleotides(ann.getComponentDefinition()) == null) {
					Sequence seq = SBOLUtils.createSequence(sequence.substring(start - 1, end), design);
					uncoveredSequences.put(i, seq);
				}
			}

			location = end + 1;
		}

		if (location < sequence.length()) {
			Sequence seq = SBOLUtils.createSequence(sequence.substring(location - 1, sequence.length()), design);
			uncoveredSequences.put(-size - 1, seq);
		}

		return uncoveredSequences;
	}

	public enum Types {
		All_types, DNA, Complex, Effector, Protein, RNA, Small_molecule;
	}

	/**
	 * Turns the type specified in types to a HashSet<URI> of types
	 */
	public static Set<URI> convertTypesToSet(Types types) {
		URI uri;
		switch (types) {
		case All_types:
			return new HashSet<URI>();
		case DNA:
			uri = ComponentDefinition.DNA;
			break;
		case Complex:
			uri = ComponentDefinition.COMPLEX;
			break;
		case Effector:
			uri = ComponentDefinition.EFFECTOR;
			break;
		case Protein:
			uri = ComponentDefinition.PROTEIN;
			break;
		case RNA:
			uri = ComponentDefinition.RNA;
			break;
		case Small_molecule:
			uri = ComponentDefinition.SMALL_MOLECULE;
			break;
		default:
			System.out.println("Invalid type");
			return new HashSet<URI>();
		}
		HashSet<URI> set = new HashSet<URI>();
		set.add(uri);
		return set;
	}

	/**
	 * Returns the Types enum associated with a type URI in types. If none
	 * exist, returns null.
	 */
	public static Types convertURIsToType(Set<URI> types) {
		for (URI type : types) {
			if (type.equals(ComponentDefinition.DNA)) {
				return Types.DNA;
			} else if (type.equals(ComponentDefinition.COMPLEX)) {
				return Types.Complex;
			} else if (type.equals(ComponentDefinition.EFFECTOR)) {
				return Types.Effector;
			} else if (type.equals(ComponentDefinition.PROTEIN)) {
				return Types.Protein;
			} else if (type.equals(ComponentDefinition.RNA)) {
				return Types.RNA;
			} else if (type.equals(ComponentDefinition.SMALL_MOLECULE)) {
				return Types.Small_molecule;
			}
		}
		return null;
	}

	/**
	 * Returns a list with all the CDs in list which contain type (URI).
	 */
	public static List<ComponentDefinition> getCDOfType(List<ComponentDefinition> list, Types type) {
		if (type == Types.All_types) {
			return list;
		}
		URI uri = convertTypesToSet(type).iterator().next();
		List<ComponentDefinition> result = new ArrayList<ComponentDefinition>();
		for (ComponentDefinition CD : list) {
			if (CD.getTypes().contains(uri)) {
				result.add(CD);
			}
		}
		return result;
	}

	/**
	 * Returns all the CDs in doc with the same role as that of part. If the
	 * part doesn't have any roles, returns all the CDs.
	 */
	public static List<ComponentDefinition> getCDOfRole(SBOLDocument doc, Part part) {
		return getCDOfRole(doc.getComponentDefinitions(), part);
	}

	/**
	 * Returns all the CDs in setCD with the same role as that of part. If the
	 * part doesn't have any roles, returns all the CDs.
	 */
	public static List<ComponentDefinition> getCDOfRole(Set<ComponentDefinition> setCD, Part part) {
		List<ComponentDefinition> list = new ArrayList<ComponentDefinition>();

		if (part == null || part.getRoles() == null || part.getRoles().isEmpty()) {
			// roles don't exist
			for (ComponentDefinition cd : setCD) {
				list.add(cd);
			}
		} else {
			// roles exist
			for (ComponentDefinition cd : setCD) {
				SequenceOntology so = new SequenceOntology();
				for (URI role : cd.getRoles()) {
					if (so.isDescendantOf(role, part.getRole()) || role.equals(part.getRole())) {
						list.add(cd);
						break;
					}
				}
			}
		}
		return list;
	}

	/**
	 * Inserts all the TopLevels (CDs and Sequences) in doc into design. If a
	 * TopLevel already exists, it will be overwritten.
	 */
	public static void insertTopLevels(SBOLDocument doc, SBOLDocument design) throws Exception {
		for (TopLevel tl : doc.getTopLevels()) {
			if (design.getTopLevel(tl.getIdentity()) != null) {
				if (tl instanceof ComponentDefinition) {
					if (!design.removeComponentDefinition(design.getComponentDefinition(tl.getIdentity()))) {
						throw new Exception("ERROR: " + tl.getDisplayId() + " didn't get removed");
					}
				} else if (tl instanceof Sequence) {
					if (!design.removeSequence(design.getSequence(tl.getIdentity()))) {
						throw new Exception("ERROR: " + tl.getDisplayId() + " didn't get removed");
					}
				}
			}
		}
		design.createCopy(doc);
	}

	/**
	 * Returns an int that represents the passed in version. 0 if version isn't
	 * a number.
	 */
	public static int getVersion(String version) {
		int v;
		try {
			v = Integer.parseInt(version);
		} catch (NumberFormatException e) {
			v = 0;
		}
		return v;
	}
}
