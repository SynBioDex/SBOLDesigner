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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.util.SBOLDeepEquality;

import com.clarkparsia.sbol.editor.sparql.LocalEndpoint;

public class SBOLSPARQLReaderTests {
	@Test
	public void simpleTest() throws Exception {
		fileTest("test/data/BBa_I0462.xml");
	}
	
	private void fileTest(String fileName) throws Exception {
		File file = new File(fileName);
		SBOLDocument doc1 = SublimeSBOLFactory.read(new FileInputStream(file));
		String uri = SBOLUtils.getRootComponent(doc1).getURI().toString();
		LocalEndpoint endpoint = new LocalEndpoint(file.toURI().toString());
		SBOLDocument doc2 = new SBOLSPARQLReader(endpoint, false).read(uri);		
		assertTrue(SBOLDeepEquality.isDeepEqual(doc1, doc2));
	}
}
