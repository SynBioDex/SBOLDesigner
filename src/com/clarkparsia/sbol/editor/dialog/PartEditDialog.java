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
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
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

	private final JComboBox typeSelection = new JComboBox(Iterables.toArray(Parts.sorted(), Part.class));
	private final JButton saveButton;
	private final JButton cancelButton;
	private final JTextField displayId = new JTextField();
	private final JTextField name = new JTextField();
	private final JTextField description = new JTextField();
	private final JTextArea sequenceField = new JTextArea(10, 80);

	public static boolean editPart(Component parent, ComponentDefinition part, boolean enableSave) {
		try {
			PartEditDialog dialog = new PartEditDialog(parent, part);
			dialog.saveButton.setEnabled(enableSave);
			dialog.setVisible(true);
			return dialog.comp != null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error editing component");
			return false;
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

		typeSelection.setSelectedItem(Parts.forComponent(comp));
		typeSelection.setRenderer(new PartCellRenderer());
		typeSelection.addActionListener(this);

		FormBuilder builder = new FormBuilder();
		builder.add("Part type", typeSelection);
		builder.add("Display ID", displayId, comp.getDisplayId());
		builder.add("Name", name, comp.getName());
		builder.add("Description", description, comp.getDescription());

		JPanel controlsPane = builder.build();

		JScrollPane tableScroller = new JScrollPane(sequenceField);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("DNASequence");
		label.setLabelFor(sequenceField);
		tablePane.add(label);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);
		tablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		sequenceField.setLineWrap(true);
		sequenceField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		// if (comp.getDnaSequence() != null &&
		// comp.getDnaSequence().getNucleotides() != null) {
		// sequence.setText(comp.getDnaSequence().getNucleotides());
		// }
		// Check if set has sequences and that the sequences's nucleotides
		// aren't null.
		Set<Sequence> sequences = comp.getSequences();
		if (sequences != null && !sequences.isEmpty()) {
			java.util.Iterator<Sequence> iter = sequences.iterator();
			while (iter.hasNext()) {
				Sequence seq = iter.next();
				if (seq.getElements() != null) {
					sequenceField.setText(seq.getElements());
				}
			}
		}
		//

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
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
		if (e.getSource().equals(typeSelection)) {
			saveButton.setEnabled(true);
			return;
		}

		try {
			if (e.getSource().equals(saveButton)) {
				if (SBOLUtils.isRegistryComponent(comp)) {
					if (!confirmEditing(getParent(), comp)) {
						return;
					}
				}

				// TODO
				try {
					System.out.println("Before");
					SBOLFactory.write(System.out);
				} catch (XMLStreamException | FactoryConfigurationError | CoreIoException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// when reassign displayId unless comp is null
				comp = SBOLFactory.getComponentDefinition(displayId.getText(), "");
				if (comp == null) {
					comp = SBOLFactory.createComponentDefinition(displayId.getText(), ComponentDefinition.DNA);
				}
				// comp.setDisplayId(displayId.getText());
				comp.setName(name.getText());
				comp.setDescription(description.getText());
				// comp.getTypes().clear();

				Part part = (Part) typeSelection.getSelectedItem();
				if (part != null) {
					comp.addRole(part.getRole());
				}

				String seq = sequenceField.getText();
				if (seq == null || seq.isEmpty()) {
					// comp.setDnaSequence(null);
					comp.clearSequences();
				} else if (comp.getSequences().isEmpty()
						|| !Objects.equal(comp.getSequences().iterator().next().getElements(), seq)) {
					// Sequence dnaSeq = SBOLUtils.createSequence(seq);
					// TODO Should sequence displayId be the same as the CDs?
					// Use sbolutils to find a unique addition
					Sequence dnaSeq = SBOLFactory.createSequence(comp.getDisplayId() + "_seq", seq, Sequence.IUPAC_DNA);
					comp.addSequence(dnaSeq);
				}

				// TODO
				try {
					System.out.println("After");
					SBOLFactory.write(System.out);
				} catch (XMLStreamException | FactoryConfigurationError | CoreIoException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// TODO
			} else {
				comp = null;
			}
		} catch (SBOLValidationException exception) {
			// TODO Generate error: if any of the fields are not valid SBOL,
			// need to report an
			// error message to the user.
			exception.printStackTrace();
		}
		setVisible(false);
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
}
