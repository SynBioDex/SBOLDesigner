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

import static com.clarkparsia.sbol.editor.SBOLEditorAction.DIVIDER;
import static com.clarkparsia.sbol.editor.SBOLEditorAction.SPACER;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLValidationException;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;

/**
 * The plugin instantiation of SBOLDesigner
 * 
 * @author Michael Zhang
 *
 */
public class SBOLDesignerPlugin extends SBOLDesignerPanel {

	SBOLEditorActions TOOLBAR_ACTIONS = new SBOLEditorActions()// .add(NEW,
																// OPEN,
																// SAVE,
																// DIVIDER)
			// .addIf(SBOLEditorPreferences.INSTANCE.isVersioningEnabled(),
			// VERSION, DIVIDER)
			.add(design.EDIT_CANVAS, design.EDIT, design.FIND, design.DELETE, design.FLIP, DIVIDER)
			.add(design.HIDE_SCARS, design.ADD_SCARS, DIVIDER).add(design.FOCUS_IN, design.FOCUS_OUT, DIVIDER, SNAPSHOT)
			.add(PREFERENCES).add(SPACER, INFO);

	private String fileName;

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRootDisplayId() {
		return design.getRootCD().getDisplayId();
	}

	private String path;

	public SBOLDesignerPlugin(String path, String fileName)
			throws SBOLValidationException, IOException, SBOLConversionException {
		super(null);
		fc = new JFileChooser(SBOLUtils.setupFile());
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));
		this.path = path;
		this.fileName = fileName;

		initGUI();

		editor.getEventBus().subscribe(this);

		// Only ask for a URI prefix if the current one is
		// "http://www.dummy.org"
		if (fileName.equals("")) {
			newDesign(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString().equals("http://www.dummy.org"));
			try {
				SBOLFactory.write(path + this.fileName);
			} catch (IOException | SBOLConversionException e) {
				e.printStackTrace();
			}
		} else {
			File file = new File(path + this.fileName);
			Preferences.userRoot().node("path").put("path", file.getPath());
			openDesign(new FileDocumentIO(false));
		}
	}

	public void saveSBOL() throws Exception {
		save();
		updateEnabledButtons(false);
	}

	public void exportSBOL(String exportFileName) {
		try {
			SBOLDocument doc = editor.getDesign().createDocument();
			File file = new File(exportFileName);
			Preferences.userRoot().node("path").put("path", file.getPath());
			DocumentIO exportIO = new FileDocumentIO(false);
			exportIO.write(doc);

			updateEnabledButtons(false);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Creates a new design to show on the canvas. Asks the user for a
	 * defaultURIprefix if askForURIPrefix is true.
	 * 
	 * @throws SBOLValidationException
	 */
	private void newDesign(boolean askForURIPrefix) throws SBOLValidationException {
		SBOLDocument doc = new SBOLDocument();
		if (askForURIPrefix) {
			setURIprefix(doc);
		}
		SBOLFactory.setSBOLDocument(doc);
		editor.getDesign().load(doc);
		fileName = design.getRootCD().getDisplayId() + ".sbol";
		setCurrentFile(null);
	}
}
