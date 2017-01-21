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

import org.openrdf.rio.RDFFormat;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;

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
		File file = SBOLUtils.setupFile();
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
		File file = SBOLUtils.setupFile();
		SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		SBOLReader.setCompliant(true);
		FileInputStream stream = new FileInputStream(file);
		SBOLDocument doc = SBOLReader.read(stream);
		stream.close();
		Preferences.userRoot().node("path").put("path", file.getPath());
		doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		return doc;
	}

	@Override
	public void write(SBOLDocument doc) throws SBOLValidationException, SBOLConversionException, IOException {
		// writer.write(doc, new FileOutputStream(file));
		File file = SBOLUtils.setupFile();
		String fileName = file.getName();
		if (!fileName.contains(".")) {
			file = new File(file + ".xml");
			Preferences.userRoot().node("path").put("path", file.getPath());
		}
		SBOLWriter.write(doc, new FileOutputStream(file));
	}

	@Override
	public String toString() {
		File file = SBOLUtils.setupFile();
		return file.getName();
	}

}
