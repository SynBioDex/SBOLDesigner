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

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;

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
	 * Returns an int which guarantees a unique URI.
	 */
	public static int getUniqueNumber(ComponentDefinition comp, String displayId, String dataType) {
		// if can get using some displayId, then try the next number
		switch (dataType) {
		case "CD":
			for (int i = 1; true; i++) {
				if (SBOLFactory.getComponentDefinition(displayId + i, "") == null) {
					return i;
				}
			}
		case "SequenceAnnotation":
			for (int i = 1; true; i++) {
				if (comp.getSequenceAnnotation(displayId + i) == null) {
					return i;
				}
			}
		case "Component":
			for (int i = 1; true; i++) {
				if (comp.getComponent(displayId + i) == null) {
					return i;
				}
			}
		case "Sequence":
			for (int i = 1; true; i++) {
				if (SBOLFactory.getSequence(displayId + i, "") == null) {
					return i;
				}
			}
		default:
			throw new IllegalArgumentException();
		}
	}

	// public static URI createURI() {
	// return URI.create("http://" + UUID.randomUUID());
	// }

	// public static URI createURI(String uri) {
	// return uri == null || uri.length() == 0 ? createURI() : URI.create(uri);
	// }

	private static String getNucleotides(ComponentDefinition comp) {
		// Sequence seq = comp.getSequence();
		// TODO potentially losing information because only looking at the first
		// sequence; loop through sequences and find the one with DNA encoding.
		// Otherwise return null.
		Sequence seq = null;
		if (comp.getSequences().size() > 0) {
			seq = comp.getSequences().iterator().next();
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

	public static Iterator<ComponentDefinition> getRootComponentDefinitions(SBOLDocument doc) {
		// return Iterators.filter(doc.getContents().iterator(),
		// ComponentDefinition.class);
		return Iterators.filter(doc.getRootComponentDefinitions().iterator(), ComponentDefinition.class);
	}

	private static Sequence createSequence(String nucleotides) {
		try {
			int unique = SBOLUtils.getUniqueNumber(null, "Sequence", "Sequence");
			return SBOLFactory.createSequence("Sequence" + unique, nucleotides, Sequence.IUPAC_DNA);
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
		renameObj(comp);
		renameObj(comp.getSequence());
		for (SequenceAnnotation ann : comp.getAnnotations()) {
			renameObj(ann);
		}
	}

	private static void renameObj(SBOLObject obj) {
		if (obj != null) {
			obj.setURI(SBOLUtils.createURI());
		}
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
