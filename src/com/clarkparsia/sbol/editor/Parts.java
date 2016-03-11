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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.util.SequenceOntology;

import com.clarkparsia.sbol.editor.Part.ImageType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class Parts {
	private Parts() {}
	
	private static final Map<URI, Part> PARTS = Maps.newHashMap();
	
	private static final List<Part> PARTS_LIST = Lists.newArrayList();
	
	public static final Part GENERIC = createPart("Generic", "Gen", "generic.png", ImageType.SHORT_OVER_BASELINE, SequenceOntology.type("SO_0000110"));
	
	public static final Part PROMOTER = createPart("Promoter", "Pro", "promoter.png", ImageType.TALL_OVER_BASELINE, 
					"SO_0000167", 
					"SO_0000315",
					"SO_0001055");
	public static final Part RBS = createPart("Ribosome Binding Site", "RBS", "translational-start-site.png", ImageType.SHORT_OVER_BASELINE, 
					"SO_0000139",
					"SO_0000140",
					"SO_0000581",
					"SO_00001647");
	public static final Part CDS = createPart("Coding Sequence", "CDS", "cds.png", ImageType.CENTERED_ON_BASELINE, 
					"SO_0000316",
					"SO_0000004",
					"SO_0000104",
					"SO_0000120",
					"SO_0000147",
					"SO_0000195",
					"SO_0000196",
					"SO_0000197",
					"SO_0000200",
					"SO_0000236",
					"SO_0000316",
					"SO_0000332",
					"SO_0000717",
					"SO_0000839",
					"SO_0000851",
					"SO_0000852",
					"SO_0001215"
					);
	public static final Part TERMINATOR = createPart("Terminator", "Ter", "terminator.png", ImageType.SHORT_OVER_BASELINE, 
					"SO_0000141",
					"SO_0000616");
	
	public static final Part ORI  = createPart("Origin of Replication", "Ori", "origin-of-replication.png", ImageType.CENTERED_ON_BASELINE, 
					"SO_0000296",
					"SO_0000436",
					"SO_0000724",
					"SO_0000340",
					"SO_0001235");
	
	public static final Part PBS  = createPart("Primer Binding Site", "PBS", "primer-binding-site.png", ImageType.SHORT_OVER_BASELINE, SequenceOntology.PRIMER_BINDING_SITE);
	public static final Part CUT = createPart("Sticky End Restriction Enzyme Cleavage Site", "CUT", "restriction-enzyme-recognition-site.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.type("SO_0001692"));
	public static final Part SCAR = createPart("Assembly Scar", "Scar", "assembly-junction.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.type("SO_0000699"), URI.create("http://partsregistry.org/type/scar"));
	public static final Part OP = createPart("Operator", "Op", "operator.png", ImageType.CENTERED_ON_BASELINE, 
					"SO_0000057",
					"SO_0000165",
					"SO_0000235",
					"SO_0000409",
					"SO_0000410",
					"SO_0000625",
					"SO_0000727",
					"SO_0001654");
	public static final Part INS = createPart("Insulator", "Ins", "insulator.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.INSULATOR);
	
	public static final Part RSE = createPart("RNA Stability Element", "RSE", "rna-stability-element.png", ImageType.SHORT_OVER_BASELINE, SequenceOntology.type("SO_0001957"));
	public static final Part PSE = createPart("Protein Stability Element", "PSE", "protein-stability-element.png", ImageType.SHORT_OVER_BASELINE, SequenceOntology.type("SO_0001955"));
	public static final Part RS = createPart("Ribonuclease Site", "RS", "ribonuclease-site.png", ImageType.SHORT_OVER_BASELINE, GENERIC.getType());
	public static final Part PS = createPart("Protease Site", "PS", "protease-site.png", ImageType.SHORT_OVER_BASELINE, SequenceOntology.type("SO_0001956"));
	
	public static final Part BRS = createPart("Blunt Restriction Site", "BRS", "blunt-restriction-site.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.type("SO_0001691"));	
	
	public static final Part FIVEOH = createPart("5' Overhang", "5'OH", "five-prime-overhang.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.type("SO_0001933"));
	public static final Part THREEOH = createPart("3' Overhang", "3'OH", "three-prime-overhang.png", ImageType.CENTERED_ON_BASELINE, SequenceOntology.type("SO_0001932"));
	
	
	private static Iterable<Part> SORTED_PARTS;

	private static Part createPart(String name, String displayId, String imageFileName, ImageType imageType, String... soIDs) {
		URI[] types = new URI[soIDs.length];
		for (int i = 0; i < soIDs.length; i++) {
			types[i] = SequenceOntology.type(soIDs[i]);
        }
		return createPart(name, displayId, imageFileName, imageType, types);
	}
	
	private static Part createPart(String name, String displayId, String imageFileName, ImageType imageType, URI... types) {
		Part part = new Part(name, displayId, imageFileName, imageType, types); 
		for (URI type : types) {
			if (!PARTS.containsKey(type)) {
				PARTS.put(type, part);
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
			SORTED_PARTS = Ordering.usingToString().sortedCopy(all());
		}

		return SORTED_PARTS;
	}

	public static Part forType(URI type) {
		return PARTS.get(type);
	}
	
	public static Part forComponent(DnaComponent comp) {
		Part result = null;
		Collection<URI> types = comp.getTypes();
		if (!types.isEmpty()) {
			for (URI type : types) {
				Part part = Parts.forType(type);
				if (part != null) {
					result = part;
					break;
				}
				else if (result == null) {
					result = generic(type);
				}
			}
		}
		
		return result != null ? result : GENERIC;
	}

	public static Part generic(URI type) {
		return new Part("", "", "generic.png", ImageType.CENTERED_ON_BASELINE, type);
	}	
}
