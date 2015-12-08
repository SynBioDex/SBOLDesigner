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

package com.clarkparsia.geneious;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.sbolstandard.core.SBOLDocument;

import com.adamtaft.eb.EventHandler;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.EditableSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.ActionProvider;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.geneious.publicapi.plugin.GeneiousAction;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.clarkparsia.sbol.editor.AddressBar;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.SBOLDesign;
import com.clarkparsia.sbol.editor.SBOLEditor;
import com.clarkparsia.sbol.editor.event.DesignChangedEvent;
import com.clarkparsia.sbol.editor.event.PartVisibilityChangedEvent;
import com.clarkparsia.sbol.editor.event.SelectionChangedEvent;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLViewer extends DocumentViewer {
	public static class Factory extends DocumentViewerFactory {
		public DocumentSelectionSignature[] getSelectionSignatures() {
			return new DocumentSelectionSignature[] { DocumentSelectionSignature.forNucleotideSequences(1, 1) };
		}

		public String getHelp() {
			return getDescription()
			                + ".<p>For more information, see <a href='http://www.sbolstandard.org/specification/extensions/visual'>SBOL visual</a>.";
		}

		public String getDescription() {
			return "Visualizes DNA components using the corresponding SBOL visual icons";
		}

		public String getName() {
			return "SBOL Visual";
		}

		public DocumentViewer createViewer(AnnotatedPluginDocument[] annotatedDocuments) {
			return new SBOLViewer(annotatedDocuments[0]);
		}
	}
	
	private final JPanel viewer;
	
	private final SBOLEditor editor;

	private final GeneiousAction saveAction, editAction, findAction, deleteAction, flipAction, scarAddAction, focusInAction, focusOutAction, snapshotAction;
	private final GeneiousAction.ToggleAction scarHideAction;
	
	private static boolean scarVisible = true;	

	private SBOLViewer(final AnnotatedPluginDocument annotatedDoc) {
		final SequenceDocument sequenceDoc = (SequenceDocument) annotatedDoc.getDocumentOrCrash();

		boolean isEditable = (sequenceDoc instanceof EditableSequenceDocument);
		final SBOLDocument sbolDoc = new SBOLExporter().processDocument(sequenceDoc);

		editor = new SBOLEditor(isEditable);

		AddressBar focusbar = new AddressBar(editor);
		focusbar.setFloatable(false);
		focusbar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		
		viewer = new JPanel(new BorderLayout());
		viewer.add(focusbar, BorderLayout.NORTH);
		viewer.add(editor, BorderLayout.CENTER);
		
		saveAction = new GeneiousAction(new GeneiousActionOptions("Save", "Save your SBOL design",
		                IconUtilities.getIcons("save16.png"))) {
			public void actionPerformed(ActionEvent e) {
				SBOLDesign design = editor.getDesign();
				EditableSequenceDocument editableDoc = (EditableSequenceDocument) sequenceDoc;
				editableDoc.setCircular(design.isCircular());
				SBOLImporter.Visitor importer = new SBOLImporter.Visitor();
				importer.importContents(design.createDocument(), editableDoc);
				annotatedDoc.saveDocument();
				setEnabled(false);
			}
		};
		saveAction.setEnabled(false);

		editAction = GeneiousActions.action(editor.getDesign().EDIT, IconUtilities.getIcons("edit16.png"));

		findAction = GeneiousActions.action(editor.getDesign().FIND, IconUtilities.getIcons("find16.png"));

		deleteAction = GeneiousActions.action(editor.getDesign().DELETE, IconUtilities.getIcons("delete16.png"));

		flipAction = GeneiousActions.action(editor.getDesign().FLIP);

		scarHideAction = new GeneiousAction.ToggleAction(GeneiousActions.options("Hide Scars",
		                "Hide Scars in your SBOL design", "hideScars.png")) {
			@Override
			public void actionToggled(ActionEvent e, boolean isSelected) {
				boolean isVisible = editor.getDesign().isPartVisible(Parts.SCAR);
				editor.getDesign().setPartVisible(Parts.SCAR, !isVisible);
				scarVisible = !scarVisible;
			}
		};
		scarHideAction.setSelected(!scarVisible);

		scarAddAction = GeneiousActions.action(editor.getDesign().ADD_SCARS);
		
		focusInAction = GeneiousActions.action(editor.getDesign().FOCUS_IN);
		
		focusOutAction = GeneiousActions.action(editor.getDesign().FOCUS_OUT);

		snapshotAction = new GeneiousAction(GeneiousActions.options("Snapshot",
		                "Takes a snapshot of the design and copies to the clipboard", "snapshot.png")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				editor.takeSnapshot();
			}
		};

		editor.getEventBus().subscribe(this);
		
		editor.getDesign().load(sbolDoc);
		editor.getDesign().setPartVisible(Parts.SCAR, scarVisible);
	}
	
	@Override
	public JComponent getComponent() {
		return viewer;
	}

	@Override
	public ActionProvider getActionProvider() {
		return new ActionProvider() {
			@Override
			public GeneiousAction getSaveAction() {
				return saveAction;
			}

			@Override
			public List<GeneiousAction> getOtherActions() {
				return Lists.newArrayList(findAction, editAction, deleteAction, flipAction,
				                new GeneiousAction.Divider(), scarHideAction, scarAddAction,
				                new GeneiousAction.Divider(), focusInAction, focusOutAction,
				                new GeneiousAction.Divider(), snapshotAction,
				                new GeneiousAction.Divider());
			}
		};
	}

	@EventHandler
	public void designChanged(DesignChangedEvent e) {
		saveAction.setEnabled(true);
	}

	@EventHandler
	public void visibilityChanged(PartVisibilityChangedEvent e) {
		if (e.getPart().equals(Parts.SCAR)) {
			scarHideAction.setSelected(!e.isVisible());
			scarVisible = e.isVisible();
		}
	}

	@EventHandler
	public void selectionChanged(SelectionChangedEvent e) {
		boolean isEnabled = (e.getSelected() != null);
		editAction.setEnabled(isEnabled);
		findAction.setEnabled(isEnabled);
		deleteAction.setEnabled(isEnabled);
		flipAction.setEnabled(isEnabled);
		focusInAction.setEnabled(editor.getDesign().canFocusIn());
		focusOutAction.setEnabled(editor.getDesign().canFocusOut());
	}
}
