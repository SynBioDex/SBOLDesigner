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

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SequenceAnnotation;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class SBOLTests {
	@Test
	public void fileTest() throws Exception {
		SBOLDocument doc = SublimeSBOLFactory.read(new FileInputStream("test/data/BBa_I0462.xml"));
		
		DnaComponent comp = SBOLUtils.getRootComponent(doc);
		String sequence = SBOLUtils.getNucleotides(comp);
		Map<Integer, DnaSequence> uncoveredSequences = SBOLUtils.findUncoveredSequences(comp, comp.getAnnotations());
		List<DnaSequence> sequences = Lists.newArrayList(Iterators.transform(comp.getAnnotations().iterator(), new Function<SequenceAnnotation, DnaSequence>() {
			@Override
            public DnaSequence apply(SequenceAnnotation ann) {
	            return ann.getSubComponent().getDnaSequence();
            }
		}));
		

		int insertCount = 0;
		for (Entry<Integer, DnaSequence> entry : uncoveredSequences.entrySet()) {
			int index = entry.getKey();
			if (index >= 0) {
				int updateIndex = index + insertCount;
				DnaSequence seq = entry.getValue();	        
	        	sequences.set(updateIndex, seq);
			}
			else {
		        int insertIndex = -index - 1 + insertCount++;
		        DnaSequence seq = entry.getValue();	        
		        sequences.add(insertIndex, seq);
			}
        }
		
		StringBuilder sb = new StringBuilder();
		for (DnaSequence seq : sequences) {
	        sb.append(seq.getNucleotides());
        }
		
		assertEquals(sequence, sb.toString());
	}
}