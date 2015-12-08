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
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import jebl.util.ProgressListener;

import org.junit.Test;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.util.SBOLDeepEquality;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter.ImportCallback;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.clarkparsia.geneious.SBOLExporter;
import com.clarkparsia.geneious.SBOLImporter;
import com.clarkparsia.sbol.SublimeSBOLFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ImportExportTests {
	private SBOLImporter importer = new SBOLImporter();
	private SBOLExporter exporter = new SBOLExporter();

	@Test
	public void testImportI0462() throws Exception {
		String fileName = "test/data/BBa_I0462.xml";
		SBOLDocument expectedDoc = SublimeSBOLFactory.read(new FileInputStream(fileName));
		DnaComponent comp = Iterables.getOnlyElement(Iterables.filter(expectedDoc.getContents(), DnaComponent.class), null);
		
		PluginDocument doc = importDocument(fileName);
		assertTrue(doc instanceof NucleotideSequenceDocument);
		NucleotideSequenceDocument seq = (NucleotideSequenceDocument) doc;
		
		assertEquals(comp.getDnaSequence().getNucleotides(), seq.getSequenceString());	
		
		Map<String,SequenceAnnotationInterval> expectedAnnotations = Maps.newHashMap();
		expectedAnnotations.put("BBa_B0034", new SequenceAnnotationInterval(1, 12, Direction.leftToRight));
		expectedAnnotations.put("BBa_C0062", new SequenceAnnotationInterval(19, 774, Direction.leftToRight));
		expectedAnnotations.put("BBa_B0015", new SequenceAnnotationInterval(808, 936, Direction.leftToRight));
		
		List<SequenceAnnotation> actualAnnotations = seq.getSequenceAnnotations();
		for (SequenceAnnotation annotation : actualAnnotations) {
			SequenceAnnotationInterval actualInterval = Iterables.getOnlyElement(annotation.getIntervals());
			SequenceAnnotationInterval expectedInterval = expectedAnnotations.remove(annotation.getName());
			assertEquals(expectedInterval, actualInterval);
        }
	}
	
	@Test
	public void testRoundTripI0462() throws Exception {
		testRoundTrip("test/data/BBa_I0462.xml");
	}
	
	private void testRoundTrip(String fileName) throws Exception {
		SBOLDocument expectedDoc = SublimeSBOLFactory.read(new FileInputStream(fileName));
		
		PluginDocument[] importedDocs = importDocuments(fileName);
		
		SBOLDocument actualDoc = exportDocuments(importedDocs);		
		
		assertTrue(SBOLDeepEquality.isDeepEqual(expectedDoc, actualDoc));
	}
	
	private SBOLDocument exportDocuments(PluginDocument[] docs) throws Exception {
		File tempFile = File.createTempFile(ImportExportTests.class.getName(), ".rdf");
		exporter.export(tempFile, docs, ProgressListener.EMPTY, new Options(getClass()));
		
		return SublimeSBOLFactory.read(new FileInputStream(tempFile));				
	}
	
	private PluginDocument importDocument(String fileName) throws Exception {
		PluginDocument[] importedDocs = importDocuments(fileName);
		assertEquals(1, importedDocs.length);
		return importedDocs[0];
	}
	
	private PluginDocument[] importDocuments(String fileName) throws Exception {
		File file = new File(fileName);
		DocumentCallback callback = new DocumentCallback();
		importer.importDocuments(file, callback, ProgressListener.EMPTY);
		return callback.getDocuments();
	}
	
	private static class DocumentCallback extends ImportCallback {		
		private PluginDocument doc;
		
		private PluginDocument[] getDocuments()  {
			assertNotNull(doc);
			return new PluginDocument[] { doc };
		}
		
		@Override
		public AnnotatedPluginDocument addDocument(AnnotatedPluginDocument doc) {
			try {
	            this.doc = doc.getDocument();
            }
            catch (DocumentOperationException e) {
	            e.printStackTrace();
            }
			return null;
		}
		
		@Override
		public AnnotatedPluginDocument addDocument(PluginDocument doc) {
			this.doc = doc;
			return null;
		}
	};
}
