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
import java.net.URI;

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

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Evren Sirin
 */
public class PartEditDialog extends JDialog implements ActionListener, DocumentListener {
	private static final String TITLE = "Component: ";
	
	private DnaComponent comp;
	
	private final JComboBox typeSelection = new JComboBox(Iterables.toArray(Parts.sorted(), Part.class));
	private final JButton saveButton;
	private final JButton cancelButton;
	private final JTextField displayId = new JTextField();
	private final JTextField name = new JTextField();
	private final JTextField description = new JTextField();
	private final JTextArea sequence = new JTextArea(10, 80);

	public static boolean editPart(Component parent, DnaComponent part, boolean enableSave) {
		try {				
			PartEditDialog dialog = new PartEditDialog(parent, part);
			dialog.saveButton.setEnabled(enableSave);
			dialog.setVisible(true);
			return dialog.comp != null;
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error editing component");
			return false;
		}
	}
	
	private static String title(DnaComponent comp) {
		String title = comp.getDisplayId();
		if (title == null) {
			title = comp.getName();
		}
		if (title == null) {
			URI uri = comp.getURI();
			title = (uri == null) ? null : uri.toString();
		}
		
		return (title == null) ? "" : CharSequences.shorten(title, 20).toString();
	}

	private PartEditDialog(Component parent, DnaComponent comp) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(comp), true);
		
		this.comp = comp;        
		
		cancelButton = new JButton("Cancel");
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
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
        
		JScrollPane tableScroller = new JScrollPane(sequence);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("DNASequence");
		label.setLabelFor(sequence);
		tablePane.add(label);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);
		tablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
		sequence.setLineWrap(true);
		sequence.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		if (comp.getDnaSequence() != null && comp.getDnaSequence().getNucleotides() != null) {
			sequence.setText(comp.getDnaSequence().getNucleotides());
		}

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
		sequence.getDocument().addDocumentListener(this);

		pack();
		setLocationRelativeTo(parent);
		displayId.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(typeSelection)) {
			saveButton.setEnabled(true);
			return;
		}
		
		if (e.getSource().equals(saveButton)) {
			if (SBOLUtils.isRegistryComponent(comp)) {
				if (!confirmEditing(getParent(), comp)) {
					return;
				}
			}			
			
			comp.setDisplayId(displayId.getText());
			comp.setName(name.getText());
			comp.setDescription(description.getText());
			comp.getTypes().clear();
			
			Part part = (Part) typeSelection.getSelectedItem();
			if (part != null) {
				comp.addType(part.getType());
			}
			
			String seq = sequence.getText();
			if (seq == null || seq.isEmpty()) {
				comp.setDnaSequence(null);
			}
			else if (comp.getDnaSequence() == null || !Objects.equal(comp.getDnaSequence().getNucleotides(), seq)) {
				DnaSequence dnaSeq = SBOLUtils.createDnaSequence(seq);
				comp.setDnaSequence(dnaSeq);
			}
		}
		else {
			comp = null;
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
	
	public static boolean confirmEditing(Component parent, DnaComponent comp) {
		int result = JOptionPane.showConfirmDialog(parent, 
				"The component '" + comp.getDisplayId() + "' has been added from\n" +
				"a parts registry and cannot be edited.\n\n" +
				"Do you want to create an editable copy of\n" +
				"this DnaComponent and save your changes?", "Edit registry part", 
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if (result == JOptionPane.NO_OPTION) {
			return false;
		}
		
		SBOLUtils.rename(comp);
		
		return true;
	}
}
