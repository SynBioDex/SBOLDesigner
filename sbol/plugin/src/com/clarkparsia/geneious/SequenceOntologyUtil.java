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

package com.clarkparsia.geneious;

import java.net.URI;
import java.util.Collection;

import org.sbolstandard.core.util.SequenceOntology;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.terms.SO;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SequenceOntologyUtil {
	private static final BiMap<URI, String> GENEIOUS_TYPES = HashBiMap.create();
	
	public static final URI DELETION = SequenceOntology.type("SO_0000159");

	public static final URI INSERTION = SequenceOntology.type("SO_0000667");
		
	public static final URI INVERSION = SequenceOntology.type("SO_1000036");
	
	public static final URI COMPLEX_SUBSTITUTION = SequenceOntology.type("SO_1000005");
	
	public static final URI POINT_MUTATION = SequenceOntology.type("SO_1000008");
		
	public static final URI A_to_G_transition = SequenceOntology.type("SO_1000015");
	public static final URI G_to_A_transition = SequenceOntology.type("SO_1000016");
	public static final URI C_to_T_transition = SequenceOntology.type("SO_1000011");
	public static final URI T_to_C_transition = SequenceOntology.type("SO_1000013");
	public static final URI A_to_C_transversion = SequenceOntology.type("SO_1000024");
	public static final URI A_to_T_transversion = SequenceOntology.type("SO_1000025");
	public static final URI G_to_C_transversion = SequenceOntology.type("SO_1000026");
	public static final URI G_to_T_transversion = SequenceOntology.type("SO_1000027");
	public static final URI C_to_A_transversion = SequenceOntology.type("SO_1000019");
	public static final URI C_to_G_transversion = SequenceOntology.type("SO_1000020");
	public static final URI T_to_A_transversion = SequenceOntology.type("SO_1000021");
	public static final URI T_to_G_transversion = SequenceOntology.type("SO_1000022");
	
	private static final URI[][] SINGLE_SUBSTITUTIONS = {
		{null,                A_to_C_transversion, A_to_G_transition,   A_to_T_transversion},
		{C_to_A_transversion, null,                C_to_G_transversion, C_to_T_transition},
		{G_to_A_transition,   G_to_C_transversion, null,                G_to_T_transversion},
		{T_to_A_transversion, T_to_C_transition,   T_to_G_transversion, null}
	};

	private static void init() {
		if (GENEIOUS_TYPES.isEmpty()) {
			initTypeMappings();
		}
	}

	private static void initTypeMappings() {
		GENEIOUS_TYPES.put(SequenceOntology.PROMOTER, SequenceAnnotation.TYPE_PROMOTER);
		GENEIOUS_TYPES.put(SequenceOntology.CDS, SequenceAnnotation.TYPE_CDS);
		GENEIOUS_TYPES.put(Parts.RBS.getType(), "RBS");
		GENEIOUS_TYPES.put(SequenceOntology.FIVE_PRIME_UTR, SequenceAnnotation.TYPE_UTR_5);
		GENEIOUS_TYPES.put(SequenceOntology.TERMINATOR, SequenceAnnotation.TYPE_TERMINATOR);
		GENEIOUS_TYPES.put(SequenceOntology.ORIGIN_OF_REPLICATION, SequenceAnnotation.TYPE_ORIGIN_OF_REPLICATION);
		GENEIOUS_TYPES.put(SequenceOntology.PRIMER_BINDING_SITE, SequenceAnnotation.TYPE_PRIMER_BIND);
		GENEIOUS_TYPES.put(SequenceOntology.RESTRICTION_ENZYME_RECOGNITION_SITE,
		                SequenceAnnotation.TYPE_RESTRICTION_SITE);
		GENEIOUS_TYPES.put(Parts.SCAR.getType(), "Scar");
		
		GENEIOUS_TYPES.put(DELETION, SequenceVariantType.DELETION.name());
		GENEIOUS_TYPES.put(INSERTION, SequenceVariantType.INSERTION.name());
		GENEIOUS_TYPES.put(INVERSION, SequenceVariantType.INVERSION.name());
		
		GENEIOUS_TYPES.forcePut(A_to_G_transition, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(G_to_A_transition, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(C_to_T_transition, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(T_to_C_transition, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(A_to_C_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(A_to_T_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(G_to_C_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(G_to_T_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(C_to_A_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(C_to_G_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(T_to_A_transversion, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(T_to_G_transversion, SequenceVariantType.SUBSTITUTION.name());				
		GENEIOUS_TYPES.forcePut(POINT_MUTATION, SequenceVariantType.SUBSTITUTION.name());
		GENEIOUS_TYPES.forcePut(COMPLEX_SUBSTITUTION, SequenceVariantType.SUBSTITUTION.name());
	}

	public static String mapToGeniuousType(URI typeURI) {
		init();
		
		String type = GENEIOUS_TYPES.get(typeURI);
		if (type == null) {
			type = getSOLabel(typeURI);
		}
		return type == null ? SequenceAnnotation.TYPE_MISC_FEATURE : type;
	}

	public static URI findMappableType(Collection<URI> typeURIs) {
		for (URI typeURI : typeURIs) {
			String type = mapToGeniuousType(typeURI);
			if (type != SequenceAnnotation.TYPE_MISC_FEATURE) {
				return typeURI;
			}
		}

		return null;
	}

	public static String getSOLabel(URI typeURI) {
		init();
		
		return typeURI == null ? null : SO.getInstance().getTerm(typeURI.toString()).getLabel();
	}
	
	public static URI getSOType(SequenceAnnotation annotation) {
		init();
		
		String soType = annotation.getQualifierValue(GeneiousQualifiers.SO_TYPE);
		
		return !Strings.isNullOrEmpty(soType) ? URI.create(soType) : GENEIOUS_TYPES.inverse().get(annotation.getType());
	}
	
	public static URI getSOType(SequenceVariantType variant) {
		switch (variant) {
			case DELETION:
				return SequenceOntologyUtil.DELETION;

			case INSERTION:
			case DUPLICATION:	
				return SequenceOntologyUtil.INSERTION;
				
			case INVERSION:
				return SequenceOntologyUtil.INVERSION;
				
			case SUBSTITUTION:
				return SequenceOntologyUtil.COMPLEX_SUBSTITUTION;
				
			default:
				break;
		}
		
		return null;
	}
	
	public static URI getSOSubstitutionType(char expected, char actual) {
		int i1 = getIndex(expected);
		int i2 = getIndex(actual);

		if (i1 == -1 || i2 == -1 || i1 == i2) {
			return POINT_MUTATION;
		}
		
		return SINGLE_SUBSTITUTIONS[i1][i2];
	}
	
	private static int getIndex(char c) {
		switch (c) {
			case 'A' : return 0;
			case 'C' : return 1;
			case 'G' : return 2;
			case 'T' : return 3;
			default: return -1;
		}
	}
}
