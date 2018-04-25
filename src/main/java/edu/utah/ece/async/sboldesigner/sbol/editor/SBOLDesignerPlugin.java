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

package edu.utah.ece.async.sboldesigner.sbol.editor;

import static edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorAction.DIVIDER;
import static edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorAction.SPACER;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.httpclient.URIException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.synbiohub.frontend.SynBioHubException;

import com.google.common.eventbus.Subscribe;

import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.ComponentDefinitionBox;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.DesignChangedEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.io.DocumentIO;
import edu.utah.ece.async.sboldesigner.sbol.editor.io.FileDocumentIO;
import edu.utah.ece.async.sboldesigner.versioning.Infos;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

/**
 * The plugin instantiation of SBOLDesigner
 * 
 * @author Michael Zhang
 *
 */
public class SBOLDesignerPlugin extends SBOLDesignerPanel {

	SBOLEditorActions TOOLBAR_ACTIONS = new SBOLEditorActions()
			.add(design.EDIT_CANVAS, design.EDIT, design.FIND, design.DELETE, design.FLIP, DIVIDER)
			.add(design.HIDE_SCARS, design.ADD_SCARS, DIVIDER).add(design.FOCUS_IN, design.FOCUS_OUT, DIVIDER, SNAPSHOT)
			.add(SPACER, INFO);

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
		if (design == null || design.getRootCD() == null)
			return null;
		return design.getRootCD().getDisplayId();
	}

	private String path;

	private URI rootURI;

	private String URIprefix;

	public SBOLDesignerPlugin(String path, String fileName, URI rootURI, String URIprefix)
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
		this.rootURI = rootURI;
		this.URIprefix = URIprefix;
		saveURIprefix();

		initGUI();

		editor.getEventBus().register(this);

		File file = new File(path + this.fileName);
		Preferences.userRoot().node("path").put("path", file.getPath());
		openDocument(new FileDocumentIO(false));
	}

	private void saveURIprefix() {
		PersonInfo oldUserInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
		PersonInfo userInfo = Infos.forPerson(URIprefix, oldUserInfo.getName(), oldUserInfo.getEmail().toString());
		SBOLEditorPreferences.INSTANCE.saveUserInfo(userInfo);
	}

	private void initGUI() {
		Box topPanel = Box.createVerticalBox();
		topPanel.add(createActionBar());
		topPanel.add(createFocusBar());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(editor, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
	}

	private JToolBar createActionBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setAlignmentX(LEFT_ALIGNMENT);

		// adds all the actions to the toolbar
		for (SBOLEditorAction action : TOOLBAR_ACTIONS) {
			if (action == DIVIDER) {
				toolbar.addSeparator();
			} else if (action == SPACER) {
				toolbar.add(Box.createHorizontalGlue());
			} else {
				AbstractButton button = action.createButton();
				toolbar.add(button);
			}
		}
		// toolbar.add(Box.createHorizontalGlue());
		return toolbar;
	}

	void openDocument(DocumentIO documentIO) throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument doc = documentIO.read();
		doc.setDefaultURIprefix(URIprefix);

		if (rootURI != null) {
			SBOLDocument newDoc = doc.createRecursiveCopy(doc.getComponentDefinition(rootURI));
			SBOLUtils.copyReferencedCombinatorialDerivations(newDoc, doc);
			doc = newDoc;
		}

		if (editor.getDesign().load(doc, rootURI)) {
			setCurrentFile(documentIO);
		}
	}

	public void saveSBOL() throws Exception {
		save();
		updateEnabledButtons(false);
	}

	boolean selectCurrentFile() {
		File file = new File(path + fileName);
		Preferences.userRoot().node("path").put("path", file.getPath());
		setCurrentFile(new FileDocumentIO(false));
		return true;
	}

	boolean save() throws Exception {
		if (documentIO == null) {
			if (!selectCurrentFile()) {
				return false;
			}
		}

		// save into existing file or into a new file
		if (!SBOLUtils.setupFile().exists()) {
			saveIntoNewFile();
		} else {
			saveIntoExistingFile();
		}
		return true;
	}

	public void uploadSBOL(java.awt.Component panel) throws URIException, SynBioHubException, SBOLValidationException {
		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument uploadDoc = editor.getDesign().createDocument(root);
		SBOLDesign.uploadDesign(panel, uploadDoc, null);
	}

	public void exportSBOL(String exportFileName, String fileType)
			throws FileNotFoundException, SBOLConversionException, IOException, SBOLValidationException {
		// String[] formats = { "SBOL 2.0", "SBOL 1.1", "GenBank", "FASTA",
		// "Cancel" };
		// int format = JOptionPane.showOptionDialog(this, "Please select an
		// export format", "Export",
		// JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		// formats, "SBOL 2.0");
		// if (format == JOptionPane.CLOSED_OPTION) {
		// return;
		// }
		File file = new File(exportFileName);
		Preferences.userRoot().node("path").put("path", file.getPath());
		String fileName = file.getName();
		SBOLDocument doc = editor.getDesign().createDocument(null);
		switch (fileType) {
		// case JOptionPane.CLOSED_OPTION:
		// break;
		case "SBOL":
			// SBOL 2.0
			if (!fileName.contains(".")) {
				file = new File(file + ".xml");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.RDF);
			break;
		case "SBOL1":
			// SBOL 1.1
			if (!fileName.contains(".")) {
				file = new File(file + ".xml");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.RDFV1);
			break;
		case "GenBank":
			// GenBank
			if (!fileName.contains(".")) {
				file = new File(file + ".gb");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.GENBANK);
			break;
		case "Fasta":
			// FASTA
			if (!fileName.contains(".")) {
				file = new File(file + ".fasta");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.FASTAformat);
			break;
		}
	}

	@Subscribe
	public void designChanged(DesignChangedEvent e) {
		updateEnabledButtons(true);
	}

	// /**
	// * Creates a new design to show on the canvas. Asks the user for a
	// * defaultURIprefix if askForURIPrefix is true.
	// *
	// * @throws SBOLValidationException
	// */
	// private void newDesign(boolean askForURIPrefix) throws
	// SBOLValidationException {
	// SBOLDocument doc = new SBOLDocument();
	// if (askForURIPrefix) {
	// setURIprefix(doc);
	// }
	// SBOLFactory.setSBOLDocument(doc);
	// editor.getDesign().load(doc);
	// fileName = design.getRootCD().getDisplayId() + ".sbol";
	// setCurrentFile(null);
	// }
}
