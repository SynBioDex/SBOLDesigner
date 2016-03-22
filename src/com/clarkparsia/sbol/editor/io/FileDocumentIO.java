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

package com.clarkparsia.sbol.editor.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openrdf.rio.RDFFormat;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import uk.ac.ncl.intbio.core.io.CoreIoException;

/**
 * 
 * @author Evren Sirin
 */
public class FileDocumentIO implements DocumentIO {
	static {
		RDFFormat.register(RDFFormat.RDFXML);
	}

	private final File file;
	// private final SBOLReader reader;
	// private final SBOLWriter writer;

	public FileDocumentIO(File file, boolean validate) {
		this.file = file;

		String fileName = file.getName();
		RDFFormat format = fileName.endsWith(".xml") ? RDFFormat.RDFXML
				: RDFFormat.forFileName(fileName, RDFFormat.RDFXML);
		// reader = SublimeSBOLFactory.createReader(format, validate);
		// writer = SublimeSBOLFactory.createWriter(format, validate);
	}

	@Override
	public SBOLDocument read() throws SBOLValidationException, IOException, CoreIoException, XMLStreamException,
			FactoryConfigurationError {
		// return reader.read(new FileInputStream(file));
		return SBOLReader.read(new FileInputStream(file));
	}

	@Override
	public void write(SBOLDocument doc) throws SBOLValidationException, IOException, XMLStreamException,
			FactoryConfigurationError, CoreIoException {
		// writer.write(doc, new FileOutputStream(file));
		SBOLWriter.write(doc, new FileOutputStream(file));
	}

	@Override
	public String toString() {
		return file.getName();
	}
}
