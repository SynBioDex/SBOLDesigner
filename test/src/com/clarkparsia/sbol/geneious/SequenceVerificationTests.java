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

package com.clarkparsia.sbol.geneious;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jebl.util.ProgressListener;

import org.junit.BeforeClass;
import org.junit.Test;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator.SelectionRange;
import com.clarkparsia.geneious.GeneiousQualifiers;
import com.clarkparsia.geneious.SequenceOntologyUtil;
import com.clarkparsia.geneious.SequenceVariantType;
import com.clarkparsia.geneious.SequenceVerificationAnnotationGenerator;
import com.clarkparsia.geneious.SequenceVerificationAnnotationGenerator.SequenceVerificationAnnotationGeneratorOptions;

/**
 * @author Evren Sirin
 */
public class SequenceVerificationTests {
	@BeforeClass
	public static void beforeClass() {
		TestGeneious.initialize();
	}

	@Test
	public void testSubstitution() throws Exception {
		AnnotatedPluginDocument[] docs = PluginUtilities.importDocuments(new File("test/data/alignment.geneious"), ProgressListener.EMPTY).toArray(new AnnotatedPluginDocument[0]);
		
		SequenceVerificationAnnotationGenerator gen = new SequenceVerificationAnnotationGenerator();
		SelectionRange selection = new SelectionRange(0, 0, 905, 905);
		SequenceVerificationAnnotationGeneratorOptions options = gen.getOptions(docs, selection);
		
		List<List<SequenceAnnotation>> result = gen.generateAnnotations(docs, selection, ProgressListener.EMPTY, options);
		assertEquals(4, result.size());
		for (int i = 0; i < 3; i++) {
			assertEquals(Collections.emptyList(), result.get(i));	
		}
		
		SequenceAnnotation expected = new SequenceAnnotation("c.38C>G", SequenceVariantType.SUBSTITUTION.toString(), new SequenceAnnotationInterval(906, 906, Direction.none)); 
		expected.addQualifier(GeneiousQualifiers.SO_TYPE_NAME, "C_to_G_transversion");
		expected.addQualifier(GeneiousQualifiers.SO_TYPE, SequenceOntologyUtil.C_to_G_transversion.toString());
		expected.addQualifier(GeneiousQualifiers.AMBIGUOUS_VERIFICATION, "no");
		expected.addQualifier(GeneiousQualifiers.AFFECTED_COMPONENT, "stem loop");
	 
		assertEquals(Arrays.asList(expected), result.get(3));			
	}

	@Test
	public void testDeletion() throws Exception {
		AnnotatedPluginDocument[] docs = PluginUtilities.importDocuments(new File("test/data/alignment.geneious"), ProgressListener.EMPTY).toArray(new AnnotatedPluginDocument[0]);
		
		SequenceVerificationAnnotationGenerator gen = new SequenceVerificationAnnotationGenerator();
		SelectionRange selection = new SelectionRange(0, 0, 913, 916);
		SequenceVerificationAnnotationGeneratorOptions options = gen.getOptions(docs, selection);
		
		List<List<SequenceAnnotation>> result = gen.generateAnnotations(docs, selection, ProgressListener.EMPTY, options);
		assertEquals(4, result.size());
		for (int i = 0; i < 3; i++) {
			assertEquals(Collections.emptyList(), result.get(i));	
		}
		
		SequenceAnnotation expected = new SequenceAnnotation("c.46_49del", SequenceVariantType.DELETION.toString(), new SequenceAnnotationInterval(914, 917, Direction.none)); 
		expected.addQualifier(GeneiousQualifiers.SO_TYPE_NAME, "deletion");
		expected.addQualifier(GeneiousQualifiers.SO_TYPE, SequenceOntologyUtil.DELETION.toString());
		expected.addQualifier(GeneiousQualifiers.AMBIGUOUS_VERIFICATION, "no");
		expected.addQualifier(GeneiousQualifiers.AFFECTED_COMPONENT, "stem loop");
	 
		assertEquals(Arrays.asList(expected), result.get(3));			
	}
}
