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

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JPanel;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core.SequenceAnnotation;

import com.clarkparsia.sbol.editor.SBOLDesign;
import com.clarkparsia.sbol.editor.SBOLEditor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;


public class SBOLUtils {
	/**
     * Returns a random, unique URI.
     */
    public static URI createURI() {
    	return URI.create("urn:" + UUID.randomUUID());
    }

	public static URI createURI(String uri) {
    	return uri == null || uri.length() == 0 ? createURI() : URI.create(uri);
    }
	
	public static String getNucleotides(DnaComponent comp) {
		DnaSequence seq = comp.getDnaSequence();
		return (seq == null) ? null : seq.getNucleotides();
	}
	
	public static SBOLDocument createdDocument(DnaComponent comp) {
		SBOLDocument doc = SBOLFactory.createDocument();
		doc.addContent(comp);
		return doc;		
	}
	
	public static DnaComponent getRootComponent(SBOLDocument doc) {
		return Iterators.getOnlyElement(Iterators.filter(doc.getContents().iterator(), DnaComponent.class), null);		
	}
	
	public static Iterator<DnaComponent> getRootComponents(SBOLDocument doc) {
		return Iterators.filter(doc.getContents().iterator(), DnaComponent.class);		
	}
	
	public static DnaSequence createDnaSequence(String nucleotides) {
		DnaSequence seq = SublimeSBOLFactory.createDnaSequence();
		seq.setURI(SBOLUtils.createURI());
		seq.setNucleotides(nucleotides);
		return seq;
	}
	
	public static boolean isRegistryComponent(DnaComponent comp) {
		URI uri = comp.getURI();
		return uri != null && uri.toString().startsWith("http://partsregistry");
	}
	
	public static Map<Integer, DnaSequence> findUncoveredSequences(DnaComponent comp, List<SequenceAnnotation> annotations) {
		String sequence = SBOLUtils.getNucleotides(comp);
		if (sequence == null) {
			return ImmutableMap.of();
		}
		
		Map<Integer, DnaSequence> uncoveredSequences = Maps.newLinkedHashMap();
		int size = annotations.size();
		int location = 1;				
		for (int i = 0; i < size; i++) {
			SequenceAnnotation ann = annotations.get(i);
			
			Integer start = ann.getBioStart();
			Integer end = ann.getBioEnd();
			if (start == null || end == null) {
				return null;
			}
			
			if (start > location) {
				DnaSequence seq = SBOLUtils.createDnaSequence(sequence.substring(location - 1, start - 1));
				uncoveredSequences.put(-i - 1, seq);
			}
			
			if (SBOLUtils.getNucleotides(ann.getSubComponent()) == null) {
				DnaSequence seq = SBOLUtils.createDnaSequence(sequence.substring(start - 1, end));
				uncoveredSequences.put(i, seq);				
			}
			
			location = end + 1;
		}
		
		if (location < sequence.length()) {
			DnaSequence seq = SBOLUtils.createDnaSequence(sequence.substring(location - 1, sequence.length()));
			uncoveredSequences.put(-size - 1, seq);			
		}
		
		return uncoveredSequences;
	}
	
	public static void rename(DnaComponent comp) {		
		renameObj(comp);		
		renameObj(comp.getDnaSequence());
		for (SequenceAnnotation ann : comp.getAnnotations()) {
			renameObj(ann);
        }
	}
	
	private static void renameObj(SBOLObject obj) {
		if (obj != null) {
			obj.setURI(SBOLUtils.createURI());
		}
	}
	
	public static BufferedImage getImage(DnaComponent comp) {
		SBOLEditor editor = new SBOLEditor(false);
		SBOLDesign design = editor.getDesign();
		SBOLDocument doc = SBOLFactory.createDocument();
		doc.addContent(comp);
		design.load(doc);

		JPanel panel = design.getPanel();
		panel.addNotify();
		panel.setSize(panel.getPreferredSize());
		panel.validate();

		return design.getSnapshot();
	}
}
