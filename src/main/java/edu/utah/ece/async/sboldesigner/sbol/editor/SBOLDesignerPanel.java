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
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.TopLevel;
import org.synbiohub.frontend.SynBioHubFrontend;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.eventbus.Subscribe;

import edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.WebOfRegistriesUtil;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.AboutDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.ComponentDefinitionBox;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.MessageDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.DesignChangedEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.io.DocumentIO;
import edu.utah.ece.async.sboldesigner.sbol.editor.io.FileDocumentIO;
import edu.utah.ece.async.sboldesigner.versioning.Infos;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

/**
 * The brains behind SBOLDesigner
 * 
 * @author Evren Sirin
 * @author Michael Zhang
 */
public class SBOLDesignerPanel extends JPanel {
	private final Supplier<Boolean> CONFIRM_SAVE = new Supplier<Boolean>() {
		@Override
		public Boolean get() {
			try {
				return confirmSave();
			} catch (Exception e) {
				MessageDialog.showMessage(null, "There was a problem saving this design: ", e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
	};

	private final SBOLEditorAction NEW_PART = new SBOLEditorAction("New Part", "Create a new part in this document",
			"newFile.png") {
		@Override
		protected void perform() {
			try {
				newPart(false, false);
			} catch (SBOLValidationException e) {
				MessageDialog.showMessage(null, "There was a problem creating this part: ", e.getMessage());
				e.printStackTrace();
			}
		}
	}.precondition(CONFIRM_SAVE);

	private final SBOLEditorAction OPEN_PART = new SBOLEditorAction("Open Part", "Open a part from this document",
			"openFile.png") {
		@Override
		protected void perform() {
			try {
				if (documentIO != null) {
					openDocument(new FileDocumentIO(false));
				} else {
					JOptionPane.showMessageDialog(null, "The current document has not yet been saved.");
				}
			} catch (SBOLValidationException | IOException | SBOLConversionException e) {
				MessageDialog.showMessage(null, "There was a problem opening this design: ", e.getMessage());
				e.printStackTrace();
			}
		}
	}.precondition(CONFIRM_SAVE);

	private final SBOLEditorAction OPEN_DOCUMENT = new SBOLEditorAction("Open Document",
			"Load an existing SBOL document", "openFolder.png") {
		@Override
		protected void perform() {
			int returnVal = fc.showOpenDialog(SBOLDesignerPanel.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				Preferences.userRoot().node("path").put("path", file.getPath());
				try {
					openDocument(new FileDocumentIO(false));
				} catch (SBOLValidationException | IOException | SBOLConversionException e) {
					MessageDialog.showMessage(null, "There was a problem opening this document: ", e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}.precondition(CONFIRM_SAVE);

	private final SBOLEditorAction NEW_DOCUMENT = new SBOLEditorAction("New Document", "Create a new SBOL Document",
			"newFolder.png") {
		@Override
		protected void perform() {
			try {
				newPart(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString().equals("http://www.dummy.org"),
						true);
			} catch (SBOLValidationException e) {
				MessageDialog.showMessage(null, "There was a problem creating this document: ", e.getMessage());
				e.printStackTrace();
			}
		}
	}.precondition(CONFIRM_SAVE);

	private final SBOLEditorAction SAVE = new SBOLEditorAction("Save", "Save your current design", "save.png") {
		@Override
		protected void perform() {
			try {
				save();
			} catch (Exception e) {
				MessageDialog.showMessage(null, "There was a problem saving this design",
						Arrays.asList(e.getMessage()));
				e.printStackTrace();
			}
		}
	};

	private final SBOLEditorAction SAVE_AS = new SBOLEditorAction("Save As",
			"Save your current design as a new document", "save_as.png") {
		@Override
		protected void perform() {
			try {
				saveAs();
			} catch (Exception e) {
				MessageDialog.showMessage(null, "There was a problem saving this design",
						Arrays.asList(e.getMessage()));
				e.printStackTrace();
			}
		}
	};

	private final SBOLEditorAction EXPORT = new SBOLEditorAction("Export", "Export your current design", "export.png") {
		@Override
		protected void perform() {
			try {
				export();
			} catch (SBOLConversionException | IOException | SBOLValidationException e) {
				MessageDialog.showMessage(null, "There was a problem exporting this design: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	final SBOLEditorAction VERSION = new SBOLEditorAction("Track and manage versions", "repository.gif") {
		@Override
		protected void perform() {
			// nothing
		}
	};

	public final SBOLEditorAction SNAPSHOT = new SBOLEditorAction(
			"Takes a snapshot of the design and copies to the clipboard", "snapshot.png") {
		@Override
		protected void perform() {
			editor.takeSnapshot();
		}
	};

	final SBOLEditorAction PREFERENCES = new SBOLEditorAction("Edit preferences and configuration options",
			"configure.png") {
		@Override
		protected void perform() {
			PreferencesDialog.showPreferences(SBOLDesignerPanel.this);
			design.getDesign().setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		}
	};

	final SBOLEditorAction INFO = new SBOLEditorAction("About SBOLDesigner", "info.png") {
		@Override
		protected void perform() {
			// for debugging
			try {
				design.getDesign().write(System.out);

				System.out.println("SynBioHubFrontends:");
				SynBioHubFrontends frontends = new SynBioHubFrontends();
				for (SynBioHubFrontend frontend : frontends.getFrontends()) {
					System.out.println(frontend.getBackendUrl());
				}
			} catch (SBOLConversionException e) {
			}
			AboutDialog.show(SBOLDesignerPanel.this);
		}
	};

	final SBOLEditor editor = new SBOLEditor(true);
	final SBOLDesign design = editor.getDesign();

	SBOLEditor getEditor() {
		return editor;
	}

	SBOLEditorActions TOOLBAR_ACTIONS = new SBOLEditorActions()
			.add(NEW_DOCUMENT, OPEN_DOCUMENT, NEW_PART, OPEN_PART, SAVE, SAVE_AS, EXPORT, DIVIDER)
			.addIf(SBOLEditorPreferences.INSTANCE.isVersioningEnabled(), VERSION, DIVIDER)
			.add(design.EDIT_CANVAS, design.EDIT, design.DELETE, design.FLIP, design.FIND, design.VARIANTS,
					design.COMBINATORIAL, design.UPLOAD, design.BOOST, DIVIDER)
			.add(design.HIDE_SCARS, design.ADD_SCARS, DIVIDER).add(design.FOCUS_IN, design.FOCUS_OUT, DIVIDER, SNAPSHOT)
			.add(PREFERENCES).add(SPACER, INFO);

	JFileChooser fc = SBOLUtils.setupFC();

	DocumentIO documentIO;

	private SBOLDesignerStandalone frame = null;

	public SBOLDesignerPanel(SBOLDesignerStandalone frame) {
		if (frame != null) {
			this.frame = frame;
		}

		initGUI();
		WebOfRegistriesUtil wors = new WebOfRegistriesUtil();
		wors.initRegistries();
		design.setPanel(this);

		editor.getEventBus().register(this);
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

	JToolBar createFocusBar() {
		AddressBar focusbar = new AddressBar(editor);
		focusbar.setFloatable(false);
		focusbar.setAlignmentX(LEFT_ALIGNMENT);
		return focusbar;
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

	/**
	 * Creates a new design to show on the canvas. Asks the user for a
	 * defaultURIprefix if askForURIPrefix is true. Also detaches the current
	 * file/working document if true.
	 */
	void newPart(boolean askForURIPrefix, boolean detachCurrentFile) throws SBOLValidationException {
		SBOLDocument doc = new SBOLDocument();
		if (askForURIPrefix) {
			setURIprefix(doc);
		}
		editor.getDesign().load(doc, null);
		if (detachCurrentFile) {
			setCurrentFile(null);
		}
		updateEnabledButtons(false);
	}

	/**
	 * Saves the default URI prefix and sets it to the SBOL document.
	 */
	void setURIprefix(SBOLDocument doc) {
		// try to set the URI prefix
		try {
			getURIprefix();
			doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		} catch (IllegalArgumentException e) {
			MessageDialog.showMessage(null, "There was an error: ", e.getMessage());
			setURIprefix(doc);
		}
	}

	/**
	 * Brings up a dialog asking for a URI, and saves the URI in
	 * SBOLEditorPreferences.
	 */
	private void getURIprefix() {
		PersonInfo oldUserInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();

		String uri;
		do {
			uri = JOptionPane.showInputDialog("Please enter a valid URI", oldUserInfo.getURI());
			if (uri == null) {
				System.exit(0);
			}
		} while (Strings.isNullOrEmpty(uri));

		PersonInfo userInfo = Infos.forPerson(uri, oldUserInfo.getName(), oldUserInfo.getEmail().toString());
		SBOLEditorPreferences.INSTANCE.saveUserInfo(userInfo);
	}

	void openDocument(DocumentIO documentIO) throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument doc = documentIO.read();
		doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		if (editor.getDesign().load(doc, null)) {
			setCurrentFile(documentIO);
		}
	}

	private void saveAs() throws IOException, Exception {
		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument doc = editor.getDesign().createDocument(root);
		if (SBOLUtils.rootCalledUnamedPart(root.cd, this)) {
			editor.getDesign().editCanvasCD();
			doc = editor.getDesign().createDocument(root);
		}

		File file = SBOLUtils.selectFile(this, fc);
		if (file == null) {
			JOptionPane.showMessageDialog(this, "File selection failed.");
			return;
		}

		if (file.exists()) {
			int selection = chooseSaveOption();
			saveOption(SBOLReader.read(file), doc, root.cd, selection, file);
		} else {
			String fileName = file.getName();
			if (!fileName.contains(".")) {
				file = new File(file + ".xml");
			}

			SBOLWriter.write(doc, new FileOutputStream(file));
		}
	}

	private void export() throws FileNotFoundException, SBOLConversionException, IOException, SBOLValidationException {
		String[] formats = { "GenBank", "FASTA", "SBOL 1.1", "Cancel" };

		int format = JOptionPane.showOptionDialog(this, "Please select an export format", "Export",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, formats, "Cancel");
		if (format == JOptionPane.CLOSED_OPTION || format == 3) {
			return;
		}

		File file = SBOLUtils.selectFile(this, fc);
		if (file == null) {
			return;
		}
		if (file.exists()) {
			JOptionPane.showMessageDialog(this, "This file already exists.");
			return;
		}

		String fileName = file.getName();

		SBOLDocument doc = editor.getDesign().createDocument(null);

		switch (format) {
		case JOptionPane.CLOSED_OPTION:
			break;
		case 0:
			// GenBank
			if (!fileName.contains(".")) {
				file = new File(file + ".gb");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.GENBANK);
			break;
		case 1:
			// FASTA
			if (!fileName.contains(".")) {
				file = new File(file + ".fasta");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.FASTAformat);
			break;
		case 2:
			// SBOL 1.1
			if (!fileName.contains(".")) {
				file = new File(file + ".xml");
			}
			SBOLWriter.write(doc, new FileOutputStream(file), SBOLDocument.RDFV1);
			break;
		default:
			break;
		}
	}

	/**
	 * returns true if it is now safe to throw away the current design
	 */
	boolean confirmSave() throws Exception {
		if (isModified()) {
			int confirmation = JOptionPane.showConfirmDialog(this,
					"Current design has been modified. If you don't save\n"
							+ "the current design, all your changes will be lost.\n\n"
							+ "Do you want to save your changes?",
					"Save changes?", JOptionPane.YES_NO_CANCEL_OPTION);

			if (confirmation == JOptionPane.CANCEL_OPTION) {
				return false;
			} else if (confirmation == JOptionPane.OK_OPTION) {
				return save();
			}
		}
		return true;
	}

	/**
	 * returns true if the design was successfully saved
	 */
	boolean save() throws Exception {
		if (documentIO == null) {
			if (!selectCurrentFile()) {
				return false;
			}
		}

		// save into existing file or into a new file
		if (SBOLUtils.setupFile().exists()) {
			return saveIntoExistingFile();
		} else {
			return saveIntoNewFile();
		}
	}

	/**
	 * returns true if a file got selected
	 */
	boolean selectCurrentFile() {
		String name = design.getRootCD().getDisplayId();
		if (!Strings.isNullOrEmpty(name)) {
			fc.setSelectedFile(SBOLUtils.setupFile());
		}

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Preferences.userRoot().node("path").put("path", file.getPath());
			setCurrentFile(new FileDocumentIO(false));
			return true;
		}

		return false;
	}

	boolean saveIntoNewFile()
			throws FileNotFoundException, SBOLValidationException, SBOLConversionException, IOException {
		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument doc = editor.getDesign().createDocument(root);

		if (SBOLUtils.rootCalledUnamedPart(root.cd, this)) {
			editor.getDesign().editCanvasCD();
			doc = editor.getDesign().createDocument(root);
		}

		documentIO.write(doc);
		updateEnabledButtons(false);

		return true;
	}

	/**
	 * Save design into an existing SBOL file
	 */
	boolean saveIntoExistingFile() throws Exception {
		// the document we are saving into
		SBOLDocument doc = documentIO.read();

		// the document we are saving
		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument currentDesign = design.createDocument(root);
		ComponentDefinition currentRootCD = root.cd;

		if (SBOLUtils.rootCalledUnamedPart(currentRootCD, this)) {
			editor.getDesign().editCanvasCD();
			currentDesign = design.createDocument(root);
			currentRootCD = root.cd;
		}

		int selection;
		if (currentRootCD.getVersion() == null || currentRootCD.getVersion().equals("")) {
			// can only overwrite
			int answer = JOptionPane.showConfirmDialog(this,
					"This design doesn't have a version, so the previous design will be overwritten.  Would you still like to save?",
					"Overwrite", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.NO_OPTION || answer == JOptionPane.CLOSED_OPTION) {
				updateEnabledButtons(true);
				return false;
			} else {
				selection = 0;
			}
		} else {
			selection = chooseSaveOption();
		}

		return saveOption(doc, currentDesign, currentRootCD, selection, SBOLUtils.setupFile());
	}

	private int chooseSaveOption() {
		int selection;
		String[] options = { "Cancel", "Overwrite Document", "New Version", "Overwrite Parts" };
		selection = JOptionPane.showOptionDialog(this,
				"You are saving into an existing SBOL file.  Would you like to overwrite the document, overwrite the parts, or create new versions of parts that already exist in the document?",
				"Save Options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
		return selection;
	}

	private boolean saveOption(SBOLDocument doc, SBOLDocument currentDesign, ComponentDefinition currentRootCD,
			int selection, File file) throws SBOLValidationException, Exception, FileNotFoundException,
					SBOLConversionException, IOException {
		switch (selection) {
		case 0: // canceled
			updateEnabledButtons(true);
			return false;
		case 1: // overwrite document
			doc = currentDesign;
			break;
		case 2: // new version
			URI newRootUri = saveNewVersion(currentRootCD, currentDesign, doc);
			design.load(doc, newRootUri);
			break;
		case 3: // overwrite parts
			// Remove from doc everything contained within currentDesign
			// that exists
			SBOLUtils.insertTopLevels(currentDesign, doc);
			break;
		case JOptionPane.CLOSED_OPTION: // closed
			updateEnabledButtons(true);
			return false;
		default:
			throw new IllegalArgumentException();
		}

		SBOLWriter.write(doc, file);
		updateEnabledButtons(false);
		return true;
	}

	/**
	 * Takes every TopLevel in currentDesign, and copies it over to doc with a
	 * new version.
	 */
	private URI saveNewVersion(TopLevel tl, SBOLDocument currentDesign, SBOLDocument doc)
			throws SBOLValidationException {
		if (tl.getVersion() == null || tl.getVersion().equals("")) {
			return tl.getIdentity();
		}
		if (tl instanceof Sequence) {
			Sequence seq = doc.getSequence(tl.getIdentity());
			if (seq != null && seq.equals(tl)) {
				return tl.getIdentity();
			}
			boolean created = false;
			String version = "";
			int increment = 1;
			while (!created) {
				try {
					version = SBOLUtils.getVersion(tl.getVersion()) + increment + "";
					TopLevel newTl = doc.createCopy(tl, tl.getDisplayId(), version);
					newTl.addWasDerivedFrom(tl.getIdentity());
					created = true;
				} catch (SBOLValidationException e) {
					increment++;
				}
			}
			return doc.getSequence(tl.getDisplayId(), version).getIdentity();
		} else if (tl instanceof ComponentDefinition) {
			for (org.sbolstandard.core2.Component comp : ((ComponentDefinition) tl).getComponents()) {
				if (comp.getDefinition() != null) {
					comp.setDefinition(saveNewVersion(comp.getDefinition(), currentDesign, doc));
				}
			}
			for (Sequence seq : ((ComponentDefinition) tl).getSequences()) {
				((ComponentDefinition) tl).removeSequence(seq.getIdentity());
				((ComponentDefinition) tl).addSequence(saveNewVersion(seq, currentDesign, doc));
			}
			ComponentDefinition CD = doc.getComponentDefinition(tl.getIdentity());
			if (CD != null && CD.equals(tl)) {
				return tl.getIdentity();
			}
			boolean created = false;
			String version = "";
			int increment = 1;
			while (!created) {
				try {
					version = SBOLUtils.getVersion(tl.getVersion()) + increment + "";
					TopLevel newTl = doc.createCopy(tl, tl.getDisplayId(), version);
					newTl.addWasDerivedFrom(tl.getIdentity());
					created = true;
				} catch (SBOLValidationException e) {
					increment++;
				}
			}
			return doc.getComponentDefinition(tl.getDisplayId(), version).getIdentity();
		}
		// Something broke
		return null;
	}

	/**
	 * Returns the String title defined by documentIO
	 */
	void setCurrentFile(DocumentIO documentIO) {
		this.documentIO = documentIO;

		if (frame != null) {
			String title = SBOLDesignerMetadata.NAME + " v" + SBOLDesignerMetadata.VERSION + " - "
					+ (documentIO == null ? "New design" : documentIO);
			frame.setTitle(title);
		}

		updateEnabledButtons(false);
	}

	void updateEnabledButtons(boolean designChanged) {
		// save and export is enabled only if the design changed
		SAVE.setEnabled(designChanged);
	}

	public boolean isModified() {
		return SAVE.isEnabled();
	}

	@Subscribe
	public void designChanged(DesignChangedEvent e) {
		updateEnabledButtons(true);
	}

	private static void setup() {
		setupLogging();
		setupLookAndFeel();
	}

	private static void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setupLogging() {
		final InputStream inputStream = SBOLDesignerPanel.class.getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final Exception e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
}
