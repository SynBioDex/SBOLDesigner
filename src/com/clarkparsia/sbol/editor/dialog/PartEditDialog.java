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

package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.terms.SO;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import uk.ac.ncl.intbio.core.io.CoreIoException;

/**
 * 
 * @author Evren Sirin
 */
public class PartEditDialog extends JDialog implements ActionListener, DocumentListener {
	private static final String TITLE = "Component: ";

	private ComponentDefinition comp;

	private final JComboBox roleSelection = new JComboBox(Iterables.toArray(Parts.sorted(), Part.class));
	private final JComboBox roleRefinement;
	private final JButton saveButton;
	private final JButton cancelButton;
	private final JButton importSequence;
	private final JButton importCD;
	private final JTextField displayId = new JTextField();
	private final JTextField name = new JTextField();
	private final JTextField description = new JTextField();
	private final JTextArea sequenceField = new JTextArea(10, 80);

	/**
	 * Returns the ComponentDefinition edited by PartEditDialog. Null if the
	 * dialog throws an exception.
	 */
	public static ComponentDefinition editPart(Component parent, ComponentDefinition part, boolean enableSave) {
		try {
			PartEditDialog dialog = new PartEditDialog(parent, part);
			dialog.saveButton.setEnabled(enableSave);
			dialog.setVisible(true);
			return dialog.comp;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error editing component: " + e.getMessage());
			return null;
		}
	}

	private static String title(ComponentDefinition comp) {
		String title = comp.getDisplayId();
		if (title == null) {
			title = comp.getName();
		}
		if (title == null) {
			URI uri = comp.getIdentity();
			title = (uri == null) ? null : uri.toString();
		}

		return (title == null) ? "" : CharSequences.shorten(title, 20).toString();
	}

	private PartEditDialog(Component parent, ComponentDefinition comp) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(comp), true);

		this.comp = comp;

		cancelButton = new JButton("Cancel");
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setEnabled(false);
		getRootPane().setDefaultButton(saveButton);

		importSequence = new JButton("Import sequence");
		importSequence.addActionListener(this);
		importCD = new JButton("Import part");
		importCD.addActionListener(this);

		roleSelection.setSelectedItem(Parts.forComponent(comp));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.addActionListener(this);

		// set up the JComboBox for role refinement
		Part selectedPart = (Part) roleSelection.getSelectedItem();
		SequenceOntology so = new SequenceOntology();
		Object[] refinements = so.getDescendantsOf(selectedPart.getRole()).toArray();
		Object[] refine = new Object[refinements.length + 1];
		refine[0] = "None";
		for (int i = 1; i < refinements.length + 1; i++) {
			refine[i] = so.getName((String) refinements[i - 1]);
		}
		roleRefinement = new JComboBox(refine);
		List<URI> refinementRoles = getRefinementRoles(comp, selectedPart);
		if (!refinementRoles.isEmpty()) {
			roleRefinement.setSelectedItem(so.getName(refinementRoles.get(0)));
		} else {
			roleRefinement.setSelectedItem("None");
		}
		roleRefinement.addActionListener(this);

		// put the controlsPane together
		FormBuilder builder = new FormBuilder();
		builder.add("Part role", roleSelection);
		builder.add("Role refinement", roleRefinement);
		builder.add("Display ID", displayId, comp.getDisplayId());
		builder.add("Name", name, comp.getName());
		builder.add("Description", description, comp.getDescription());
		JPanel controlsPane = builder.build();

		JScrollPane tableScroller = new JScrollPane(sequenceField);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("DNA sequence");
		label.setLabelFor(sequenceField);
		tablePane.add(label);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);
		tablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		sequenceField.setLineWrap(true);
		sequenceField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		Sequence seq = comp.getSequenceByEncoding(Sequence.IUPAC_DNA);
		if (seq != null && !seq.getElements().isEmpty()) {
			sequenceField.setText(seq.getElements());
		}

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(importCD);
		buttonPane.add(importSequence);
		buttonPane.add(cancelButton);
		buttonPane.add(saveButton);

		// Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(controlsPane, BorderLayout.PAGE_START);
		contentPane.add(tablePane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);

		displayId.getDocument().addDocumentListener(this);
		name.getDocument().addDocumentListener(this);
		description.getDocument().addDocumentListener(this);
		sequenceField.getDocument().addDocumentListener(this);

		pack();

		setLocationRelativeTo(parent);
		displayId.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e) {
		boolean keepVisible = false;
		if (e.getSource().equals(roleSelection) || e.getSource().equals(roleRefinement)) {
			saveButton.setEnabled(true);
			return;
		}

		if (e.getSource() == importSequence) {
			importSequenceHandler();
			return;
		}

		if (e.getSource() == importCD) {
			boolean isImported = false;
			try {
				isImported = importCDHandler();
			} catch (SBOLValidationException e1) {
				e1.printStackTrace();
			}
			if (isImported) {
				setVisible(false);
			}
			return;
		}

		try {
			if (e.getSource().equals(saveButton)) {
				saveButtonHandler();
			} else {
				// Sets comp to null if things don't get edited/cancel is
				// pressed
				comp = null;
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(getParent(), "What you have entered is invalid. " + e1.getMessage());
			e1.printStackTrace();
			keepVisible = true;
		}

		if (!keepVisible) {
			setVisible(false);
		} else {
			keepVisible = false;
		}
	}

	/**
	 * Handles importing of a CD and all its dependencies. Returns true if
	 * something was imported. False otherwise.
	 */
	private boolean importCDHandler() throws SBOLValidationException {
		SBOLDocument doc = importDoc();
		if (doc != null) {
			Object[] CDs = doc.getComponentDefinitions().toArray();

			switch (CDs.length) {
			case 0:
				JOptionPane.showMessageDialog(getParent(), "There are no parts to import");
				return false;
			case 1:
				comp = (ComponentDefinition) CDs[0];
				SBOLFactory.createCopy(doc.createRecursiveCopy(comp));
				return true;
			default:
				// create an Object[] of Strings from the displayIds of the CDs
				// in CDs
				Object[] cdDisplayIds = new Object[CDs.length];
				for (int i = 0; i < CDs.length; i++) {
					cdDisplayIds[i] = ((ComponentDefinition) CDs[i]).getDisplayId();
				}

				// TODO replace with something more scalable
				int selection = JOptionPane.showOptionDialog(null, "Please select a part", "Part selector",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, cdDisplayIds, cdDisplayIds[0]);
				if (selection == -1) {
					return false;
				} else {
					this.comp = (ComponentDefinition) CDs[selection];
					SBOLFactory.createCopy(doc.createRecursiveCopy(comp));
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * Fills in a CD and Sequence based on this dialog's state.
	 */
	private void saveButtonHandler() throws SBOLValidationException {
		// if (SBOLUtils.isRegistryComponent(comp)) {
		// if (!confirmEditing(getParent(), comp)) {
		// return;
		// }
		// }

		comp = SBOLFactory.getComponentDefinition(displayId.getText(), "");
		if (comp == null) {
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, displayId.getText(), "CD");
			comp = SBOLFactory.createComponentDefinition(uniqueId, ComponentDefinition.DNA);
		}
		comp.setName(name.getText());
		comp.setDescription(description.getText());
		// comp.setDisplayId(displayId.getText());
		// comp.getTypes().clear();

		Part part = (Part) roleSelection.getSelectedItem();
		if (part != null) {
			// change parts list of roles to set of roles
			Set<URI> setRoles = new HashSet<URI>();
			for (URI role : part.getRoles()) {
				setRoles.add(role);
			}
			// use the role from roleRefinement if not "None"
			if (!roleRefinement.getSelectedItem().equals("None")) {
				SequenceOntology so = new SequenceOntology();
				setRoles.clear();
				URI roleURI = so.getURIbyName((String) roleRefinement.getSelectedItem());
				if (!so.isDescendantOf(roleURI, part.getRole())) {
					throw new IllegalArgumentException(roleRefinement.getSelectedItem() + " isn't applicable for "
							+ roleSelection.getSelectedItem());
				}
				setRoles.add(roleURI);
			}
			comp.setRoles(setRoles);
		}

		String seq = sequenceField.getText();
		if (seq == null || seq.isEmpty()) {
			// comp.setDnaSequence(null);
			comp.clearSequences();
		} else if (comp.getSequences().isEmpty()
				|| !Objects.equal(comp.getSequences().iterator().next().getElements(), seq)) {
			// Sequence dnaSeq = SBOLUtils.createSequence(seq);
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, comp.getDisplayId() + "Sequence", "Sequence");
			Sequence dnaSeq = SBOLFactory.createSequence(uniqueId, seq, Sequence.IUPAC_DNA);
			comp.addSequence(dnaSeq);
		}

		// TODO debugging
		try {
			SBOLFactory.write(System.out);
		} catch (SBOLConversionException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Handles when the import sequence button is clicked.
	 */
	private void importSequenceHandler() {
		SBOLDocument doc = importDoc();
		if (doc != null) {
			Set<Sequence> seqSet = doc.getSequences();
			String importedNucleotides = "";
			if (seqSet.size() == 1) {
				// only one Sequence
				importedNucleotides = seqSet.iterator().next().getElements();
			} else {
				// multiple Sequences
				importedNucleotides = sequenceSelector(seqSet);
			}
			sequenceField.setText(importedNucleotides);
		}
	}

	/**
	 * Displays to the user all the sequences and returns the elements of the
	 * sequence the user chooses. sequences should contain at leasts one
	 * Sequence object.
	 */
	private String sequenceSelector(Set<Sequence> sequences) {
		Object[] sList = sequences.toArray();

		// create an Object[] of all the displayIds
		Object[] sDisplayId = new Object[sList.length];
		for (int i = 0; i < sList.length; i++) {
			sDisplayId[i] = ((Sequence) sList[i]).getDisplayId();
		}

		// TODO replace with something more scalable
		int selection = JOptionPane.showOptionDialog(null, "Please select a sequence", "Sequence selector",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, sDisplayId, sDisplayId[0]);
		return ((Sequence) sList[selection]).getElements();
	}

	/**
	 * Prompts the user to choose a file and reads it, returning the output
	 * SBOLDocument. If the user cancels or the file in unable to be imported,
	 * returns null.
	 */
	private SBOLDocument importDoc() {
		JFileChooser fc = new JFileChooser(new File("."));
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(
				new FileNameExtensionFilter("SBOL file (*.xml, *.rdf, *.sbol), GenBank (*.gb, *.gbk), FASTA (*.fasta)",
						"xml", "rdf", "sbol", "gb", "gbk", "fasta"));

		int returnVal = fc.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			SBOLDocument doc = null;
			try {
				SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
				SBOLReader.setCompliant(true);
				doc = SBOLReader.read(file);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(getParent(), "This file is unable to be imported: " + e1.getMessage());
				e1.printStackTrace();
			}
			return doc;
		}
		return null;
	}

	@Override
	public void removeUpdate(DocumentEvent paramDocumentEvent) {
		saveButton.setEnabled(true);
	}

	@Override
	public void insertUpdate(DocumentEvent paramDocumentEvent) {
		saveButton.setEnabled(true);
	}

	@Override
	public void changedUpdate(DocumentEvent paramDocumentEvent) {
		saveButton.setEnabled(true);
	}

	public static boolean confirmEditing(Component parent, ComponentDefinition comp) {
		int result = JOptionPane.showConfirmDialog(parent,
				"The component '" + comp.getDisplayId() + "' has been added from\n"
						+ "a parts registry and cannot be edited.\n\n" + "Do you want to create an editable copy of\n"
						+ "this ComponentDefinition and save your changes?",
				"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.NO_OPTION) {
			return false;
		}

		SBOLUtils.rename(comp);

		return true;
	}

	/**
	 * Returns a list of all roles of a CD that are descendants of the part's
	 * role.
	 */
	public List<URI> getRefinementRoles(ComponentDefinition comp, Part part) {
		ArrayList<URI> list = new ArrayList<URI>();
		SequenceOntology so = new SequenceOntology();
		for (URI r : comp.getRoles()) {
			// assumes the part role is always the first role in the list
			if (so.isDescendantOf(r, part.getRole())) {
				list.add(r);
			}
		}
		return list;
	}
}
