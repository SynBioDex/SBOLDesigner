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

package edu.utah.ece.async.sboldesigner.sbol.editor;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.utah.ece.async.sboldesigner.sbol.editor.Part.ImageType;

public class Parts {
	private Parts() {
	}

	private static final Map<URI, Part> PARTS = Maps.newHashMap();

	private static final List<Part> PARTS_LIST = Lists.newArrayList();

	// unspecified part is for parts without roles that get opened
	public static final Part UNSPECIFIED = createPart("Unspecified", "IDK", "unspecified.png",
			ImageType.TALL_OVER_BASELINE, new String[0]);
	public static final Part GENERIC = createPart("Engineered Region", "Gen", "engineered-region.png",
			ImageType.SHORT_OVER_BASELINE, SequenceOntology.ENGINEERED_REGION);
	public static final Part PROMOTER = createPart("Promoter", "Pro", "promoter.png", ImageType.TALL_OVER_BASELINE,
			SequenceOntology.PROMOTER);
	public static final Part RBS = createPart("Ribosome Binding Site", "RBS", "translational-start-site.png",
			ImageType.SHORT_OVER_BASELINE, SequenceOntology.RIBOSOME_ENTRY_SITE);
	public static final Part CDS = createPart("Coding Sequence", "CDS", "cds.png", ImageType.CENTERED_ON_BASELINE,
			SequenceOntology.CDS);
	public static final Part TERMINATOR = createPart("Terminator", "Ter", "terminator.png",
			ImageType.SHORT_OVER_BASELINE, SequenceOntology.TERMINATOR);
	public static final Part CIRCULAR = createPart("Circular Backbone", "Cir", "blank-backbone.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0000755");
	public static final Part NON_CODING_RNA_GENE = createPart("Non-Coding RNA Gene", "gRNA", "non-coding-rna-gene.png",
			ImageType.SHORT_OVER_BASELINE, "SO:0001263");
	public static final Part ORI = createPart("Origin of Replication", "Ori", "origin-of-replication.png",
			ImageType.CENTERED_ON_BASELINE, SequenceOntology.ORIGIN_OF_REPLICATION);
	public static final Part ORIT = createPart("Origin of Transfer", "OriT", "origin-of-transfer.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0000724");
	public static final Part PBS = createPart("Primer Binding Site", "PBS", "primer-binding-site.png",
			ImageType.SHORT_OVER_BASELINE, SequenceOntology.PRIMER_BINDING_SITE);
	public static final Part CUT5 = createPart("5' Sticky Restriction Site", "_5SRS", "cut.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001975");
	public static final Part CUT3 = createPart("3' Sticky Restriction Site", "_3SRS", "cut2.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001976");
	public static final Part SCAR = createPart("Assembly Scar", "Scar", "assembly-junction.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001953");
	public static final Part OP = createPart("Operator", "Op", "operator.png", ImageType.CENTERED_ON_BASELINE,
			SequenceOntology.OPERATOR);
	public static final Part INS = createPart("Insulator", "Ins", "insulator.png", ImageType.SHORT_OVER_BASELINE,
			SequenceOntology.INSULATOR);
	public static final Part BRS = createPart("Blunt Restriction Site", "BRS", "blunt-restriction-site.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001691");
	public static final Part FIVEOH = createPart("5' Overhang", "_5OH", "five-prime-overhang.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001932");
	public static final Part THREEOH = createPart("3' Overhang", "_3OH", "three-prime-overhang.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0001933");
	public static final Part APTAMER = createPart("Aptamer", "APT", "aptamer.png", ImageType.TALL_OVER_BASELINE,
			"SO:0000031");
	public static final Part POLYA = createPart("PolyA Site", "PolyA", "poly-a-site.png", ImageType.SHORT_OVER_BASELINE,
			"SO:0000553");
	public static final Part SRS = createPart("Specific Recombination Site", "SRS", "specific-recombination-site.png",
			ImageType.CENTERED_ON_BASELINE, "SO:0000299");
	public static final Part NO_GLYPH_ASSIGNED = createPart("No Glyph Assigned", "NGA", "no-glyph-assigned.png",
			ImageType.TALL_OVER_BASELINE, SequenceOntology.SEQUENCE_FEATURE);
	public static final Part SIGNATURE = createPart("Signature", "Sig", "signature.png", ImageType.TALL_OVER_BASELINE,
			"SO:0001978");
	public static final Part BS = createPart("Base", "BS", "base.png", ImageType.SHORT_OVER_BASELINE, "SO:0001236");
	public static final Part JUN = createPart("Junction", "Jun", "junction.png", ImageType.SHORT_OVER_BASELINE,
			"SO:0000699");
	public static final Part AA = createPart("Amino Acid", "AA", "amino-acid.png", ImageType.SHORT_OVER_BASELINE,
			"SO:0001237");
	public static final Part RERS = createPart("Restriction Enzyme Recognition Site", "RERS",
			"restriction-enzyme-recognition-site.png", ImageType.SHORT_OVER_BASELINE, "SO:0001687");
	public static final Part RS = createPart("Ribonuclease Site", "RS", "ribonuclease-site.png",
			ImageType.SHORT_OVER_BASELINE, "SO:0001977");
	public static final Part PS = createPart("Protease Site", "PS", "protease-site.png", ImageType.SHORT_OVER_BASELINE,
			"SO:0001956");
	public static final Part RSE = createPart("RNA Stability Element", "RSE", "rna-stability-element.png",
			ImageType.SHORT_OVER_BASELINE, "SO:0001979");
	public static final Part PSE = createPart("Protein Stability Element", "PSE", "protein-stability-element.png",
			ImageType.SHORT_OVER_BASELINE, "SO:0001955");

	private static Iterable<Part> SORTED_PARTS;

	private static Part createPart(String name, String displayId, String imageFileName, ImageType imageType,
			String... soIDs) {
		URI[] roles = new URI[soIDs.length];
		SequenceOntology so = new SequenceOntology();
		for (int i = 0; i < soIDs.length; i++) {
			roles[i] = so.getURIbyId(soIDs[i]);
		}
		return createPart(name, displayId, imageFileName, imageType, roles);
	}

	private static Part createPart(String name, String displayId, String imageFileName, ImageType imageType,
			URI... roles) {
		Part part = new Part(name, displayId, imageFileName, imageType, roles);
		for (URI role : roles) {
			if (!PARTS.containsKey(role)) {
				PARTS.put(role, part);
			}
		}
		PARTS_LIST.add(part);
		return part;
	}

	public static Iterable<Part> all() {
		return PARTS_LIST;
	}

	public static Iterable<Part> sorted() {
		if (SORTED_PARTS == null) {
			// SORTED_PARTS = Ordering.usingToString().sortedCopy(all());
			SORTED_PARTS = PARTS_LIST;
		}

		return SORTED_PARTS;
	}

	/**
	 * Returns a part for the given role.
	 */
	public static Part forRole(URI role) {
		if (PARTS.get(role) != null) {
			return PARTS.get(role);
		}

		SequenceOntology so = new SequenceOntology();
		for (Part part : PARTS.values()) {
			if (part == UNSPECIFIED) {
				// UNSPECIFIED has role SequenceFeature, which all other roles
				// inherit from. Must therefore skip and let forComponent return
				// UNSPECIFIED if nothing else matches.
				continue;
			}
			if (so.isDescendantOf(role, part.getRole())) {
				return part;
			}
		}
		return null;
	}

	public static Part forIdentified(Identified identified) {
		Part result = null;
		Collection<URI> roles;
		if (identified instanceof ComponentDefinition) {
			roles = ((ComponentDefinition) identified).getRoles();
		} else if (identified instanceof Component) {
			roles = ((Component) identified).getRoles();
		} else if (identified instanceof SequenceAnnotation) {
			roles = ((SequenceAnnotation) identified).getRoles();
		} else {
			return UNSPECIFIED;
		}
		if (!roles.isEmpty()) {
			for (URI role : roles) {
				Part part = Parts.forRole(role);
				if (part != null) {
					result = part;
					break;
				}
			}
		}

		return result != null ? result : UNSPECIFIED;
	}
}
