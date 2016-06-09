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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openrdf.rio.RDFFormat;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import com.clarkparsia.sbol.editor.SBOLEditorPreferences;

import uk.ac.ncl.intbio.core.io.CoreIoException;

/**
 * 
 * @author Evren Sirin
 */
public class FileDocumentIO implements DocumentIO {
	static {
		RDFFormat.register(RDFFormat.RDFXML);
	}

	// private final SBOLReader reader;
	// private final SBOLWriter writer;

	public FileDocumentIO(boolean validate) {
		File file = setupFile();
		String fileName = file.getName();
		RDFFormat format = fileName.endsWith(".xml") ? RDFFormat.RDFXML
				: RDFFormat.forFileName(fileName, RDFFormat.RDFXML);
		// reader = SublimeSBOLFactory.createReader(format, validate);
		// writer = SublimeSBOLFactory.createWriter(format, validate);
	}

	@Override
	public SBOLDocument read()
			throws SBOLValidationException, FileNotFoundException, IOException, SBOLConversionException {
		// return reader.read(new FileInputStream(file));
		File file = setupFile();
		SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		SBOLReader.setCompliant(true);
		return SBOLReader.read(new FileInputStream(file));
	}

	@Override
	public void write(SBOLDocument doc) throws SBOLValidationException, SBOLConversionException, IOException {
		// writer.write(doc, new FileOutputStream(file));
		File file = setupFile();
		String[] formats = { "SBOL 2.0", "SBOL 1.1", "GenBank", "FASTA" };
		int format = JOptionPane.showOptionDialog(null, "Please select an output format", "Save as",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, formats, "SBOL 2.0");

		String fileName = file.getName();

		switch (format) {
		case JOptionPane.CLOSED_OPTION:
			break;
		case 0:
			// SBOL 2.0
			if (!fileName.contains(".")) {
				file = new File(file + ".rdf");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.RDF);
			break;
		case 1:
			// SBOL 1.1
			if (!fileName.contains(".")) {
				file = new File(file + ".rdf");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.RDFV1);
			break;
		case 2:
			// GenBank
			if (!fileName.contains(".")) {
				file = new File(file + ".gb");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.GENBANK);
			break;
		case 3:
			// FASTA
			if (!fileName.contains(".")) {
				file = new File(file + ".fasta");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.FASTAformat);
			break;
		}
	}

	@Override
	public String toString() {
		File file = setupFile();
		return file.getName();
	}

	/**
	 * Gets the path from Preferences and returns a File
	 */
	public static File setupFile() {
		String path = Preferences.userRoot().node("path").get("path", "");
		return new File(path);
	}
}
