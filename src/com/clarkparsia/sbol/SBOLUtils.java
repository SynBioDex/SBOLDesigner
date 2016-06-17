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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.TopLevel;

import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * Being used:
 * 
 * SBOLUtils.getRootComponent(doc)
 * 
 * SBOLUtils.rename(comp)
 * 
 * SBOLUtils.createSequence(seq)
 * 
 * SBOLUtils.isRegistryComponent(comp)
 * 
 * SBOLUtils.createURI()
 * 
 * SBOLUtils.findUncoveredSequences(currentComponent, Lists.transform(elements,
 * new Function<DesignElement, SequenceAnnotation>() {
 * 
 * @Override public SequenceAnnotation apply(DesignElement e) { return
 *           e.getAnnotation(); } }))
 */

public class SBOLUtils {
	/**
	 * Returns an int which guarantees a unique URI. Pass in the parent CD (if
	 * dataType isn't a TopLevel), the displayId you want, the version (if
	 * dataType is a TopLevel), and the type of object.
	 */
	public static String getUniqueDisplayId(ComponentDefinition comp, String displayId, String version,
			String dataType) {
		// if can get using some displayId, then try the next number
		switch (dataType) {
		case "CD":
			for (int i = 1; true; i++) {
				if (i == 1 && SBOLFactory.getComponentDefinition(displayId, version) == null) {
					return displayId;
				}
				if (SBOLFactory.getComponentDefinition(displayId + i, version) == null) {
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
				if (i == 1 && SBOLFactory.getSequence(displayId, version) == null) {
					return displayId;
				}
				if (SBOLFactory.getSequence(displayId + i, version) == null) {
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
	 * Prompts the user to choose a file and reads it, returning the output
	 * SBOLDocument. If the user cancels or the file in unable to be imported,
	 * returns null.
	 */
	public static SBOLDocument importDoc() {
		JFileChooser fc = new JFileChooser(SBOLUtils.setupFile());
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));

		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getCurrentDirectory();
			Preferences.userRoot().node("path").put("path", file.getPath());

			file = fc.getSelectedFile();
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
	 * Gets the path from Preferences and returns a File
	 */
	public static File setupFile() {
		String path = Preferences.userRoot().node("path").get("path", "");
		return new File(path);
	}

	// public static URI createURI() {
	// return URI.create("http://" + UUID.randomUUID());
	// }

	// public static URI createURI(String uri) {
	// return uri == null || uri.length() == 0 ? createURI() : URI.create(uri);
	// }

	private static String getNucleotides(ComponentDefinition comp) {
		// Sequence seq = comp.getSequence();
		Sequence seq = null;
		if (comp.getSequences().size() > 0) {
			seq = comp.getSequenceByEncoding(Sequence.IUPAC_DNA);
		}
		return (seq == null) ? null : seq.getElements();
	}

	// public static SBOLDocument createdDocument(ComponentDefinition comp) {
	// SBOLDocument doc = SBOLFactory.createDocument();
	// doc.addContent(comp);
	// return doc;
	// }

	public static ComponentDefinition getRootComponentDefinition(SBOLDocument doc) {
		// return
		// Iterators.getOnlyElement(Iterators.filter(doc.getContents().iterator(),
		// ComponentDefinition.class), null);
		return Iterators.getOnlyElement(
				Iterators.filter(doc.getRootComponentDefinitions().iterator(), ComponentDefinition.class), null);
	}

	// public static Iterator<ComponentDefinition>
	// getRootComponentDefinitions(SBOLDocument doc) {
	// // return Iterators.filter(doc.getContents().iterator(),
	// // ComponentDefinition.class);
	// return Iterators.filter(doc.getRootComponentDefinitions().iterator(),
	// ComponentDefinition.class);
	// }

	private static Sequence createSequence(String nucleotides) {
		try {
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, "Sequence", "", "Sequence");
			return SBOLFactory.createSequence(uniqueId, "1", nucleotides, Sequence.IUPAC_DNA);
		} catch (SBOLValidationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isRegistryComponent(ComponentDefinition comp) {
		URI uri = comp.getIdentity();
		return uri != null && uri.toString().startsWith("http://partsregistry");
	}

	public static Map<Integer, Sequence> findUncoveredSequences(ComponentDefinition comp,
			List<SequenceAnnotation> annotations) {
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
				Sequence seq = SBOLUtils.createSequence(sequence.substring(location - 1, start - 1));
				uncoveredSequences.put(-i - 1, seq);
			}

			if (SBOLUtils.getNucleotides(ann.getComponentDefinition()) == null) {
				Sequence seq = SBOLUtils.createSequence(sequence.substring(start - 1, end));
				uncoveredSequences.put(i, seq);
			}

			location = end + 1;
		}

		if (location < sequence.length()) {
			Sequence seq = SBOLUtils.createSequence(sequence.substring(location - 1, sequence.length()));
			uncoveredSequences.put(-size - 1, seq);
		}

		return uncoveredSequences;
	}

	public static void rename(ComponentDefinition comp) {
		// renameObj(comp);
		// renameObj(comp.getSequence());
		// for (SequenceAnnotation ann : comp.getAnnotations()) {
		// renameObj(ann);
		// }
	}

	private static void renameObj(SBOLObject obj) {
		// if (obj != null) {
		// obj.setURI(SBOLUtils.createURI());
		// }
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

		if (part.getRoles() == null || part.getRoles().isEmpty()) {
			// roles don't exist
			for (ComponentDefinition cd : setCD) {
				list.add(cd);
			}
		} else {
			// roles exist
			for (ComponentDefinition cd : setCD) {
				// TODO should use SequenceOntology for better role selecting
				if (cd.getRoles().contains(part.getRole())) {
					list.add(cd);
				}
			}
		}
		return list;
	}

	/**
	 * Inserts all the TopLevels (CDs and Sequences) in doc which aren't already
	 * in the SBOLFactory.
	 */
	public static void insertTopLevels(SBOLDocument doc) {
		for (TopLevel tl : doc.getTopLevels()) {
			if (!SBOLFactory.getTopLevels().contains(tl)) {
				try {
					SBOLFactory.createCopy(tl);
				} catch (SBOLValidationException e) {
					JOptionPane.showMessageDialog(null, "There was an error copying over data: " + e.getMessage());
				}
			}
		}
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

	// public static BufferedImage getImage(ComponentDefinition comp) {
	// SBOLEditor editor = new SBOLEditor(false);
	// SBOLDesign design = editor.getDesign();
	// SBOLDocument doc = new SBOLDocument();
	// doc.addContent(comp);
	// design.load(doc);
	//
	// JPanel panel = design.getPanel();
	// panel.addNotify();
	// panel.setSize(panel.getPreferredSize());
	// panel.validate();
	//
	// return design.getSnapshot();
	// }
}
