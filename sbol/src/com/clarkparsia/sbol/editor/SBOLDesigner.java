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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;

import com.adamtaft.eb.EventHandler;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.dialog.AboutDialog;
import com.clarkparsia.sbol.editor.dialog.CheckoutDialog;
import com.clarkparsia.sbol.editor.dialog.CheckoutDialog.CheckoutResult;
import com.clarkparsia.sbol.editor.dialog.CreateBranchDialog;
import com.clarkparsia.sbol.editor.dialog.CreateTagDialog;
import com.clarkparsia.sbol.editor.dialog.CreateVersionDialog;
import com.clarkparsia.sbol.editor.dialog.HistoryDialog;
import com.clarkparsia.sbol.editor.dialog.MergeBranchDialog;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog;
import com.clarkparsia.sbol.editor.dialog.QueryVersionsDialog;
import com.clarkparsia.sbol.editor.dialog.SwitchBranchDialog;
import com.clarkparsia.sbol.editor.event.DesignChangedEvent;
import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.sbol.editor.io.ReadOnlyDocumentIO;
import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.Branch;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;

/**
 * @author Evren Sirin
 */
public class SBOLDesigner extends JFrame {
	private final Supplier<Boolean> CONFIRM_SAVE = new Supplier<Boolean>() {
		@Override
        public Boolean get() {
            return confirmSave();
        }
	};
	
	private final SBOLEditorAction NEW = new SBOLEditorAction("New", "Create a new design", "new.gif") {		
		@Override
		protected void perform() {			
			newDesign();
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction OPEN = new SBOLEditorAction("Open", "Load a design from an SBOL file", "open.gif") {		
		@Override
		protected void perform() {			
			int returnVal = fc.showOpenDialog(SBOLDesigner.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				openDesign(new FileDocumentIO(file, false));
			}
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction SAVE = new SBOLEditorAction("Save", "Save your current design", "save.gif") {		
		@Override
		protected void perform() {
			save();
		}
	};
	
	private final SBOLEditorAction VERSION = new SBOLEditorAction("Track and manage versions", "repository.gif") {		
		@Override
		protected void perform() {
			// nothing
		}
	};
	
	public final SBOLEditorAction SNAPSHOT = new SBOLEditorAction("Takes a snapshot of the design and copies to the clipboard", "snapshot.png") {		
		@Override
		protected void perform() {
			editor.takeSnapshot();			
		}
	};
	
	private final SBOLEditorAction PREFERENCES = new SBOLEditorAction("Edit preferences and configuration options", "configure.gif") {		
		@Override
		protected void perform() {
			PreferencesDialog.showPreferences(SBOLDesigner.this);
		}
	};
	
	private final SBOLEditorAction INFO = new SBOLEditorAction("About SBOL Designer", "info.gif") {		
		@Override
		protected void perform() {
		    AboutDialog.show(SBOLDesigner.this);
		}
	};
	
	private final SBOLEditorAction NEW_VERSION = new SBOLEditorAction("New", "Create a new versioned design", "newRepository.png") {		
		@Override
		protected void perform() {
			DocumentIO rvtIO = new CreateVersionDialog(SBOLDesigner.this).getInput();
			if (rvtIO != null) {
				setCurrentFile(rvtIO);
			}
		}
	}.precondition(CONFIRM_SAVE);
	private final SBOLEditorAction CHECKOUT = new SBOLEditorAction("Checkout", "Checks out a version", "checkout.gif") {		
		@Override
		protected void perform() {
				CheckoutResult result = new CheckoutDialog(SBOLDesigner.this).getInput();
				if (result != null) {
					DocumentIO newIO = result.getDocumentIO();
					if (result.isInsert()) {
						try {
							DnaComponent newComponent = SBOLUtils.getRootComponent(newIO.read());
							design.addComponent(newComponent);
						}
						catch (Throwable ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(SBOLDesigner.this, "Error checking out: " + ex.getMessage());
						}
					}
					else {
						openDesign(newIO);
					}
				}				
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction COMMIT = new SBOLEditorAction("Commit", "Commits the current design as a new version",
	                "commit.gif") {		
		@Override
		protected void perform() {
			if (documentIO == null || !(documentIO instanceof RVTDocumentIO)) {
				DocumentIO rvtIO = new CreateVersionDialog(SBOLDesigner.this).getInput();
				if (rvtIO == null) {
					return;
				}
				setCurrentFile(rvtIO);
			}	
			
			save();			
		}
	};
	
	private final SBOLEditorAction BRANCH = new SBOLEditorAction("Branch", "Commits the current design as a new version",
	                "newBranch.gif") {		
		@Override
		protected void perform() {
			
				RVTDocumentIO rvtIO = ((RVTDocumentIO) documentIO);
				CreateBranchDialog dialog = new CreateBranchDialog(SBOLDesigner.this);
				String branchName = dialog.getInput();
				if (branchName != null) {
					DocumentIO newIO = rvtIO.createBranch(branchName, dialog.getBranchMessage());
					setCurrentFile(newIO);
				}		
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction MERGE = new SBOLEditorAction("Merge", "Commits the current design as a new version",
	                "merge.gif") {		
		@Override
		protected void perform() {
			
				RVTDocumentIO rvtIO = ((RVTDocumentIO) documentIO);
				MergeBranchDialog dialog = new MergeBranchDialog(SBOLDesigner.this, rvtIO.getBranch());
				Branch branch = dialog.getInput();
				if (branch != null) {
					DocumentIO newIO = rvtIO.mergeBranch(branch, dialog.getMergeMessage());
					openDesign(newIO);
				}
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction SWITCH = new SBOLEditorAction("Switch", "Commits the current design as a new version",
	                "switchBranch.gif") {		
		@Override
		protected void perform() {
			
				RVTDocumentIO rvtIO = ((RVTDocumentIO) documentIO);
				Branch branch = new SwitchBranchDialog(SBOLDesigner.this, rvtIO.getBranch()).getInput();
				if (branch != null) {
					DocumentIO newIO = rvtIO.switchBranch(branch);
					openDesign(newIO);
				}
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction TAG = new SBOLEditorAction("Tag", "Tag the current version", "tag.png") {		
		@Override
		protected void perform() {			
			RVTDocumentIO rvtIO = ((RVTDocumentIO) documentIO);
			CreateTagDialog dialog = new CreateTagDialog(SBOLDesigner.this);
			String tagName = dialog.getInput();
			if (tagName != null) {
				rvtIO.createTag(tagName, dialog.getTagMessage());
			}
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction VALIDATE = new SBOLEditorAction("Validate", "Validate the current design", "validate.gif") {		
		@Override
		protected void perform() {			
			RVTDocumentIO rvtIO = ((RVTDocumentIO) documentIO);
			SPARQLEndpoint endpoint = rvtIO.getBranch().getEndpoint();
			try {
	            endpoint.validate(RDFInput.forURL(SBOLUtils.class.getResource("constraints.ttl")), rvtIO.getBranch().getHead().getURI().toString());
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
			
		}
	}.precondition(CONFIRM_SAVE);
	
	private final SBOLEditorAction QUERY_VERSION = new SBOLEditorAction("Query", "Query the version repository",
	                "queryVersion.png") {		
		@Override
		protected void perform() {
			new QueryVersionsDialog(SBOLDesigner.this).getInput();
		}
	};
	
	private final SBOLEditorAction HISTORY = new SBOLEditorAction("History", "Commits the current design as a new version",
	                "history.gif") {		
		@Override
		protected void perform() {
			DocumentIO docIO = HistoryDialog.show(SBOLDesigner.this, (RVTDocumentIO) documentIO);
			if (docIO != null) {
				if (docIO instanceof ReadOnlyDocumentIO) {
					try {
	                    SBOLDocument doc = docIO.read();
	                    DnaComponent comp = SBOLUtils.getRootComponent(doc);
	                    design.addComponent(comp);
                    }
                    catch (Exception e) {
                    	JOptionPane.showMessageDialog(null, "ERROR: " + e.getMessage());
                    	e.printStackTrace();
                    }
				}
				else if (confirmSave()) {
					openDesign(docIO);
				}				
			}
		}
	};

	private final SBOLEditor editor = new SBOLEditor(true);
	private final SBOLDesign design = editor.getDesign();
	
	private final SBOLEditorActions TOOLBAR_ACTIONS = new SBOLEditorActions()
					.add(NEW, OPEN, SAVE, DIVIDER)
					.addIf(SBOLEditorPreferences.INSTANCE.isVersioningEnabled(), VERSION, DIVIDER)
					.add(design.EDIT_ROOT, design.EDIT, design.FIND, design.DELETE, design.FLIP, DIVIDER)
					.add(design.HIDE_SCARS, design.ADD_SCARS, DIVIDER)
					.add(design.FOCUS_IN, design.FOCUS_OUT, DIVIDER, SNAPSHOT)
					.add(PREFERENCES)
					.add(SPACER, INFO);
	
	private final SBOLEditorActions VERSION_ACTIONS = new SBOLEditorActions()
					.add(NEW_VERSION, DIVIDER)
					.add(CHECKOUT, COMMIT, TAG, VALIDATE, DIVIDER)
					.addIf(SBOLEditorPreferences.INSTANCE.isBranchingEnabled(), BRANCH, SWITCH, MERGE, DIVIDER)
					.add(QUERY_VERSION, HISTORY);
	
	private final JFileChooser fc;
	
	private DocumentIO documentIO;

	public SBOLDesigner() {
		fc = new JFileChooser(new File("."));
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(new FileNameExtensionFilter("SBOL file (*.xml, *.rdf)", "xml", "rdf"));
		
		initGUI();
		
		editor.getEventBus().subscribe(this);
		
		newDesign();
	}
	
	private void initGUI() {
		Box topPanel = Box.createVerticalBox();
		topPanel.add(createActionBar());
		topPanel.add(createFocusBar());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(editor, BorderLayout.CENTER);
		
		setContentPane(panel);
		setLocationRelativeTo(null);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private JToolBar createFocusBar() {
		AddressBar focusbar = new AddressBar(editor);
		focusbar.setFloatable(false);
		focusbar.setAlignmentX(LEFT_ALIGNMENT);
		return focusbar;
	}
	
	private JToolBar createActionBar() {
		final JPopupMenu versionMenu = createVersionMenu();
		
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setAlignmentX(LEFT_ALIGNMENT);
		
		for (SBOLEditorAction action : TOOLBAR_ACTIONS) {
			if (action == DIVIDER) {
				toolbar.addSeparator();
			}
			else if (action == SPACER) {
				toolbar.add(Box.createHorizontalGlue());
			}
			else{
		        AbstractButton button = action.createButton();
		        toolbar.add(button);
		        if (action == VERSION) {
		    		button.addMouseListener(new MouseAdapter() {
			            public void mousePressed(MouseEvent e) {
			            	Component c = e.getComponent();
			            	versionMenu.show(c, 0, c.getY() + c.getHeight());
			            }
			        });
		        }
	        }
        }	
		
//		toolbar.add(Box.createHorizontalGlue());
		
		return toolbar;
	}

	private JPopupMenu createVersionMenu() {
		final JPopupMenu popup = new JPopupMenu();

		for (SBOLEditorAction action : VERSION_ACTIONS) {
			if (action == DIVIDER) {
				popup.addSeparator();
			}
			else {
				popup.add(action.createMenuItem());
			}
		}

		return popup;
	}
	
	private void newDesign() {
		editor.getDesign().load(SBOLFactory.createDocument());
		setCurrentFile(null);
	}
	
	private void openDesign(DocumentIO documentIO) {
		try {
			SBOLDocument doc = documentIO.read();
			editor.getDesign().load(doc);
			setCurrentFile(documentIO);
		}
		catch (Throwable ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
		}
	}
	
	private boolean confirmSave() {
		if (isModified()) {
			int confirmation = JOptionPane.showConfirmDialog(this, 
							"Current design has been modified. If you don't save\n" +
							"the current design, all your changes will be lost.\n\n" +
							"Do you want to save your changes?", "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (confirmation == JOptionPane.CANCEL_OPTION) {
				return false;
			}
			else if (confirmation == JOptionPane.OK_OPTION) {
				save();
			}
		}
		
		return true;
	}
	
	private boolean save() {
		if (documentIO == null) {
			if (!selectCurrentFile()) {
				return false;
			}
		}
		saveCurrentFile();
		return true;
	}
	
	private boolean selectCurrentFile() {
		String name = design.getRootComponent().getDisplayId();
		if (!Strings.isNullOrEmpty(name)) {
			File currentDirectory = fc.getCurrentDirectory();
			fc.setSelectedFile(new File(currentDirectory, name));
		}
		
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String fileName = file.getName();
			if (!fileName.contains(".")) {
				file = new File(file + ".rdf");
			}
			setCurrentFile(new FileDocumentIO(file, false));
			return true;
		}
		
		return false;
	}
	
	private void saveCurrentFile() {
		try {
			SBOLDocument doc = editor.getDesign().createDocument();
			
			documentIO.write(doc);
			
			updateEnabledButtons(false);
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
		}
	}
	
	private void setCurrentFile(DocumentIO documentIO) {
		this.documentIO = documentIO;
		
		String title = SBOLDesignerMetadata.NAME + " v" + SBOLDesignerMetadata.VERSION + " - " + (documentIO == null ? "New design" : documentIO);
		setTitle(title);
		
		updateEnabledButtons(false);
	}
	
	private void updateEnabledButtons(boolean designChanged) {			
		// enable version actions only if this is a versioned design
		boolean versionedDesign = (documentIO instanceof RVTDocumentIO);
		for (SBOLEditorAction action : VERSION_ACTIONS) {
			action.setEnabled(versionedDesign);
        }
		// new and checkout actions are always enabled
		NEW_VERSION.setEnabled(true);
		CHECKOUT.setEnabled(true);
		QUERY_VERSION.setEnabled(true);
		// commit enabled is design changed or it is not versioned so you can 
		// load a design from a file and save it in the version registry
		COMMIT.setEnabled(designChanged || !versionedDesign);
		// save is enabled only if the design changed
		SAVE.setEnabled(designChanged);
	}
	
	public boolean isModified() {
		return SAVE.isEnabled();
	}	

	@EventHandler
	public void designChanged(DesignChangedEvent e) {
		updateEnabledButtons(true);
	}

	public static void main(String[] args) {
        setup();
		
		final SBOLDesigner frame = new SBOLDesigner();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		

		if (args.length > 0) {
			try {
				File file = new File(args[0]);
				frame.openDesign(new FileDocumentIO(file, false));
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
		}
	}
	
	private static void setup() {
		setupLogging();
		setupLookAndFeel();
	}
	
	private static void setupLookAndFeel() {
		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
	}
	
	private static void setupLogging() {
		final InputStream inputStream = SBOLDesigner.class.getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		}
		catch (final Exception e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
}
