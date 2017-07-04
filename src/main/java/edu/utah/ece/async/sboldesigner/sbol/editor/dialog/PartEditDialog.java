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

package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import edu.utah.ece.async.sboldesigner.sbol.CharSequences;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

/**
 * 
 * @author Evren Sirin
 */
public class PartEditDialog extends JDialog implements ActionListener, DocumentListener {
	private static final String TITLE = "Part: ";

	private ComponentDefinition CD;
	private SequenceAnnotation SA;
	private SBOLDocument design;
	private boolean canEdit;
	private Component parent;

	private final JComboBox<Part> roleSelection = new JComboBox<Part>(Iterables.toArray(Parts.sorted(), Part.class));
	private final JComboBox<Types> typeSelection = new JComboBox<Types>(Types.values());
	private final JComboBox<String> roleRefinement;
	private final JButton saveButton;
	private final JButton cancelButton;
	private final JButton importSequence;
	private final JButton importCD;
	private final JButton importFromRegistry;
	private final JButton openAnnotations;
	private final JTextField displayId = new JTextField();
	private final JTextField name = new JTextField();
	private final JTextField version = new JTextField();
	private final JTextField description = new JTextField();
	private final JLabel URIlink = new JLabel();
	private final JLabel derivedFrom = new JLabel();
	private final JTextArea sequenceField = new JTextArea(10, 80);

	/**
	 * Returns the ComponentDefinition edited by PartEditDialog. Null if the
	 * dialog throws an exception. Also pass in the design.
	 */
	public static ComponentDefinition editPart(Component parent, ComponentDefinition CD, boolean enableSave,
			boolean canEdit, SBOLDocument design) {
		try {
			PartEditDialog dialog = new PartEditDialog(parent, CD, canEdit, design);
			dialog.saveButton.setEnabled(enableSave);
			dialog.setVisible(true);
			return dialog.CD;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error editing component: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Returns the SequenceAnnotation edited by PartEditDialog. Null if the
	 * dialog throws an exception. Also pass in the design.
	 */
	public static SequenceAnnotation editPart(Component parent, ComponentDefinition CD, SequenceAnnotation SA,
			boolean enableSave, boolean canEdit, SBOLDocument design) {
		try {
			PartEditDialog dialog = new PartEditDialog(parent, CD, SA, canEdit, design);
			dialog.saveButton.setEnabled(enableSave);
			dialog.setVisible(true);
			return dialog.SA;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Error editing sequenceAnnotation: " + e.getMessage());
			return null;
		}
	}

	private static String title(Identified comp) {
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

	private PartEditDialog(final Component parent, final ComponentDefinition CD, boolean canEdit, SBOLDocument design) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(CD), true);

		this.CD = CD;
		this.design = design;
		this.canEdit = canEdit;
		this.parent = parent;

		cancelButton = new JButton("Cancel");
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setEnabled(false);
		getRootPane().setDefaultButton(saveButton);

		importFromRegistry = new JButton("Import registry part");
		importFromRegistry.addActionListener(this);
		importSequence = new JButton("Import sequence");
		importSequence.addActionListener(this);
		importCD = new JButton("Import part");
		importCD.addActionListener(this);
		openAnnotations = new JButton("Open annotations");
		openAnnotations.addActionListener(this);

		typeSelection.setSelectedItem(SBOLUtils.convertURIsToType(CD.getTypes()));
		typeSelection.addActionListener(this);

		roleSelection.setSelectedItem(Parts.forIdentified(CD));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.addActionListener(this);

		// set up the JComboBox for role refinement
		Part selectedPart = (Part) roleSelection.getSelectedItem();
		roleRefinement = new JComboBox<String>();
		updateRoleRefinement();
		List<URI> refinementRoles = SBOLUtils.getRefinementRoles(CD, selectedPart);
		if (!refinementRoles.isEmpty()) {
			SequenceOntology so = new SequenceOntology();
			roleRefinement.setSelectedItem(so.getName(refinementRoles.get(0)));
		} else {
			roleRefinement.setSelectedItem("None");
		}
		roleRefinement.addActionListener(this);

		// set up clickable URIlink
		URIlink.setText(CD.getIdentity().toString());
		URIlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		URIlink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(CD.getIdentity());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent, "The URI could not be opened: " + e1.getMessage());
				}
			}
		});

		// put the controlsPane together
		// some fields are optional
		FormBuilder builder = new FormBuilder();
		builder.add("Part type", typeSelection);
		builder.add("Part role", roleSelection);
		builder.add("Role refinement", roleRefinement);
		builder.add("Display ID", displayId, CD.getDisplayId());
		builder.add("Name", name, CD.getName());
		builder.add("Description", description, CD.getDescription());
		if (CD.isSetVersion()) {
			version.setEditable(false);
			builder.add("Version", version, CD.getVersion());
		}
		builder.add("URI", URIlink);
		if (CD.isSetWasDerivedFrom()) {
			derivedFrom.setText(CD.getWasDerivedFrom().toString());
			derivedFrom.setCursor(new Cursor(Cursor.HAND_CURSOR));
			derivedFrom.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(CD.getWasDerivedFrom());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(parent, "The URI could not be opened: " + e1.getMessage());
					}
				}
			});
			builder.add("Derived from", derivedFrom);
		}
		JPanel controlsPane = builder.build();

		JScrollPane tableScroller = new JScrollPane(sequenceField);
		tableScroller.setPreferredSize(new Dimension(550, 200));
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
		Sequence seq = CD.getSequenceByEncoding(Sequence.IUPAC_DNA);
		if (seq != null && !seq.getElements().isEmpty()) {
			sequenceField.setText(seq.getElements());
		}

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(importFromRegistry);
		buttonPane.add(importCD);
		buttonPane.add(importSequence);
		buttonPane.add(openAnnotations);
		buttonPane.add(Box.createHorizontalStrut(100));
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
		version.getDocument().addDocumentListener(this);
		description.getDocument().addDocumentListener(this);
		sequenceField.getDocument().addDocumentListener(this);

		pack();

		setLocationRelativeTo(parent);
		displayId.requestFocusInWindow();
	}

	private PartEditDialog(final Component parent, final ComponentDefinition CD, final SequenceAnnotation SA,
			boolean canEdit, SBOLDocument design) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(SA), true);

		this.SA = SA;
		this.design = design;
		this.canEdit = canEdit;

		cancelButton = new JButton("Cancel");
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setEnabled(false);
		getRootPane().setDefaultButton(saveButton);

		importFromRegistry = new JButton("Import registry part");
		importFromRegistry.addActionListener(this);
		importSequence = new JButton("Import sequence");
		importSequence.addActionListener(this);
		importCD = new JButton("Import part");
		importCD.addActionListener(this);
		openAnnotations = new JButton("Open annotations");
		openAnnotations.addActionListener(this);

		typeSelection.setSelectedItem(SBOLUtils.convertURIsToType(CD.getTypes()));
		typeSelection.addActionListener(this);

		roleSelection.setSelectedItem(Parts.forIdentified(SA));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.addActionListener(this);

		// set up the JComboBox for role refinement
		Part selectedPart = (Part) roleSelection.getSelectedItem();
		roleRefinement = new JComboBox<String>();
		updateRoleRefinement();
		List<URI> refinementRoles = SBOLUtils.getRefinementRoles(SA, selectedPart);
		if (!refinementRoles.isEmpty()) {
			SequenceOntology so = new SequenceOntology();
			roleRefinement.setSelectedItem(so.getName(refinementRoles.get(0)));
		} else {
			roleRefinement.setSelectedItem("None");
		}
		roleRefinement.addActionListener(this);

		// set up clickable URIlink
		URIlink.setText(CD.getIdentity().toString());
		URIlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
		URIlink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(CD.getIdentity());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(parent, "The URI could not be opened: " + e1.getMessage());
				}
			}
		});

		// put the controlsPane together
		// some fields are optional
		FormBuilder builder = new FormBuilder();
		builder.add("Part type", typeSelection);
		builder.add("Part role", roleSelection);
		builder.add("Role refinement", roleRefinement);
		builder.add("Display ID", displayId, SA.getDisplayId());
		builder.add("Name", name, SA.getName());
		builder.add("Description", description, SA.getDescription());
		if (SA.isSetVersion()) {
			version.setEditable(false);
			builder.add("Version", version, SA.getVersion());
		}
		builder.add("URI", URIlink);
		if (SA.isSetWasDerivedFrom()) {
			derivedFrom.setText(SA.getWasDerivedFrom().toString());
			derivedFrom.setCursor(new Cursor(Cursor.HAND_CURSOR));
			derivedFrom.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(SA.getWasDerivedFrom());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(parent, "The URI could not be opened: " + e1.getMessage());
					}
				}
			});
			builder.add("Derived from", derivedFrom);
		}
		JPanel controlsPane = builder.build();

		// TODO: read only for now
		roleSelection.setEnabled(false);
		roleRefinement.setEnabled(false);
		displayId.setEditable(false);
		name.setEditable(false);
		description.setEditable(false);
		importFromRegistry.setEnabled(false);
		importSequence.setEnabled(false);
		importCD.setEnabled(false);

		JScrollPane tableScroller = new JScrollPane(sequenceField);
		tableScroller.setPreferredSize(new Dimension(550, 200));
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
		Sequence seq = CD.getSequenceByEncoding(Sequence.IUPAC_DNA);
		if (seq != null && !seq.getElements().isEmpty()) {
			if (SA.getLocations().size() == 1) {
				Location location = SA.getLocations().iterator().next();
				if (location instanceof Range) {
					Range range = (Range) location;
					sequenceField.setText(seq.getElements().substring(range.getStart() - 1, range.getEnd()));
				} else if (location instanceof Cut) {
					// TODO: need to consider how to display this
				}
			} else {
				// TODO: need to consider how to display multiple locations
			}
		}

		// TODO: read-only for now
		sequenceField.setEditable(false);

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(importFromRegistry);
		buttonPane.add(importCD);
		buttonPane.add(importSequence);
		buttonPane.add(openAnnotations);
		buttonPane.add(Box.createHorizontalStrut(100));
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
		version.getDocument().addDocumentListener(this);
		description.getDocument().addDocumentListener(this);
		sequenceField.getDocument().addDocumentListener(this);

		pack();

		setLocationRelativeTo(parent);
		displayId.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean keepVisible = false;
		if (e.getSource().equals(roleSelection) || e.getSource().equals(roleRefinement)
				|| e.getSource().equals(typeSelection)) {
			if (canEdit) {
				saveButton.setEnabled(true);
			}
			if (e.getSource().equals(roleSelection)) {
				updateRoleRefinement();
			}
			return;
		}

		if (e.getSource() == importSequence) {
			importSequenceHandler();
			return;
		}

		try {
			if (e.getSource() == importCD) {
				boolean isImported = false;
				isImported = importCDHandler();
				if (isImported) {
					setVisible(false);
				}
				return;
			}

			if (e.getSource() == importFromRegistry) {
				boolean isImported = false;
				isImported = importFromRegistryHandler();
				if (isImported) {
					setVisible(false);
				}
				return;
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "This part cannot be imported: " + e1.getMessage());
			e1.printStackTrace();
		}

		if (e.getSource() == openAnnotations) {
			openAnnotationHandler();
			return;
		}

		try {
			if (e.getSource().equals(saveButton)) {
				saveButtonHandler();
			} else {
				// Sets comp to null if things don't get edited/cancel is
				// pressed
				CD = null;
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

	private void updateRoleRefinement() {
		roleRefinement.removeAllItems();
		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
			roleRefinement.addItem(s);
		}
	}

	private void openAnnotationHandler() {
		new AnnotationEditor(parent, CD);
	}

	/**
	 * Handles importing of a CD and all its dependencies from a registry.
	 * Returns true if something was imported. False otherwise.
	 */
	private boolean importFromRegistryHandler() throws Exception {
		Part part = roleSelection.getSelectedItem().equals("None") ? PartInputDialog.ALL_PARTS
				: (Part) roleSelection.getSelectedItem();
		URI role = new SequenceOntology().getURIbyName((String) roleRefinement.getSelectedItem());
		Types type = (Types) typeSelection.getSelectedItem();

		// User selects the CD
		SBOLDocument selection = new RegistryInputDialog(this.getParent(), part, type, role, design).getInput();
		if (selection == null) {
			return false;
		} else {
			this.CD = selection.getRootComponentDefinitions().iterator().next();
			// copy the rest of the design into design
			SBOLUtils.insertTopLevels(selection, design);
			return true;
		}
	}

	/**
	 * Handles importing of a CD and all its dependencies. Returns true if
	 * something was imported. False otherwise.
	 */
	private boolean importCDHandler() throws Exception {
		SBOLDocument doc = SBOLUtils.importDoc();
		if (doc != null) {
			ComponentDefinition[] CDs = doc.getComponentDefinitions().toArray(new ComponentDefinition[0]);

			switch (CDs.length) {
			case 0:
				JOptionPane.showMessageDialog(getParent(), "There are no parts to import");
				return false;
			case 1:
				CD = CDs[0];
				SBOLUtils.insertTopLevels(doc.createRecursiveCopy(CD), design);
				return true;
			default:
				Part criteria = roleSelection.getSelectedItem().equals("None") ? PartInputDialog.ALL_PARTS
						: (Part) roleSelection.getSelectedItem();

				// User selects the CD
				SBOLDocument selection = new PartInputDialog(getParent(), doc, criteria).getInput();
				if (selection == null) {
					return false;
				} else {
					this.CD = selection.getRootComponentDefinitions().iterator().next();
					// copy the rest of the design into design
					SBOLUtils.insertTopLevels(selection, design);
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
		if (SBOLUtils.isRegistryComponent(CD)) {
			// Rename CD and use that
			CD = confirmEditing(getParent(), CD, design);
			if (CD == null) {
				return;
			}
		}

		// try to get CD if it exists. Otherwise, create it.
		if (design.getComponentDefinition(displayId.getText(), version.getText()) != null) {
			CD = design.getComponentDefinition(displayId.getText(), version.getText());
		} else {
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, displayId.getText(), version.getText(), "CD", design);
			CD = (ComponentDefinition) design.createCopy(CD, uniqueId, version.getText());
		}

		if (name.getText().length() == 0) {
			if (CD.isSetName()) {
				CD.unsetName();
			}
		} else {
			CD.setName(name.getText());
		}
		CD.setDescription(description.getText());

		Types type = (Types) typeSelection.getSelectedItem();
		if (type != null) {
			CD.setTypes(SBOLUtils.convertTypesToSet(type));
		}

		Part part = (Part) roleSelection.getSelectedItem();
		if (part != null) {
			// change parts list of roles to set of roles
			Set<URI> setRoles = new HashSet<URI>(part.getRoles());
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
			CD.setRoles(setRoles);
		}

		String seq = sequenceField.getText();
		if (seq == null || seq.isEmpty()) {
			CD.clearSequences();
		} else if (CD.getSequences().isEmpty()
				|| !Objects.equal(CD.getSequences().iterator().next().getElements(), seq)) {
			CD.clearSequences();
			String uniqueId = SBOLUtils.getUniqueDisplayId(null, CD.getDisplayId() + "Sequence", CD.getVersion(),
					"Sequence", design);
			Sequence dnaSeq = design.createSequence(uniqueId, CD.getVersion(), seq, Sequence.IUPAC_DNA);
			CD.addSequence(dnaSeq);
		}

		addSBOLDesignerAnnotation(CD);
	}

	private void addSBOLDesignerAnnotation(ComponentDefinition cd) throws SBOLValidationException {
		// get/create SBOLDesigner agent
		URI designerURI = URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/2.2");
		// unused because designerURI will be dereferenced on SynBioHub
		// designerURI = createSBOLDesignerAgent().getIdentity();

		// get/create the activity
		URI activityURI = URI
				.create(design.getDefaultURIprefix() + cd.getDisplayId() + "_SBOLDesigner" + "/" + cd.getVersion());
		GenericTopLevel oldActivity = design.getGenericTopLevel(activityURI);

		if (oldActivity != null) {
			design.removeGenericTopLevel(oldActivity);
		}

		GenericTopLevel partActivity = design.createGenericTopLevel(design.getDefaultURIprefix(),
				cd.getDisplayId() + "_SBOLDesigner", cd.getVersion(),
				new QName("http://www.w3.org/ns/prov#", "Activity", "prov"));

		String creator = SBOLEditorPreferences.INSTANCE.getUserInfo().getName();
		partActivity.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), creator);

		partActivity.createAnnotation(new QName("http://www.w3.org/ns/prov#", "endedAtTime", "prov"),
				ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

		// create the qualified usage annotation
		Annotation agentAnnotation = new Annotation(new QName("http://www.w3.org/ns/prov#", "agent", "prov"),
				designerURI);

		partActivity.createAnnotation(new QName("http://www.w3.org/ns/prov#", "qualifiedAssociation", "prov"),
				new QName("http://www.w3.org/ns/prov#", "Association", "prov"),
				URI.create(partActivity.getIdentity().toString() + "/association"),
				new ArrayList<Annotation>(Arrays.asList(agentAnnotation)));

		// link the cd/part to partActivity
		Annotation prev = null;
		for (Annotation a : cd.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("wasGeneratedBy") && a.isURIValue()
					&& a.getURIValue().equals(designerURI)) {
				prev = a;
			}
		}

		if (prev != null) {
			cd.removeAnnotation(prev);
		}

		cd.createAnnotation(new QName("http://www.w3.org/ns/prov#", "wasGeneratedBy", "prov"),
				partActivity.getIdentity());
	}

	private GenericTopLevel createSBOLDesignerAgent() throws SBOLValidationException {
		GenericTopLevel designerAgent = design
				.getGenericTopLevel(URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/2.2"));

		if (designerAgent == null) {
			designerAgent = design.createGenericTopLevel("http://www.async.ece.utah.edu", "SBOLDesigner", "2.2",
					new QName("http://www.w3.org/ns/prov#", "Agent", "prov"));
			designerAgent.setName("SBOLDesigner CAD Tool");
			designerAgent.setDescription(
					"SBOLDesigner is a simple, biologist-friendly CAD software tool for creating and manipulating the sequences of genetic constructs using the Synthetic Biology Open Language (SBOL) 2.0 data model. Throughout the design process, SBOL Visual symbols, a system of schematic glyphs, provide standardized visualizations of individual parts. SBOLDesigner completes a workflow for users of genetic design automation tools. It combines a simple user interface with the power of the SBOL standard and serves as a launchpad for more detailed designs involving simulations and experiments. Some new features in SBOLDesigner are SynBioHub integration, local repositories, importing of parts/sequences from existing files, import and export of GenBank and FASTA files, extended role ontology support, the ability to partially open designs with multiple root ComponentDefinitions, backward compatibility with SBOL 1.1, and versioning.");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Michael Zhang");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Chris Myers");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Michal Galdzicki");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Bryan Bartley");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Sean Sleight");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "Evren Sirin");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), "John Gennari");
		}

		return designerAgent;
	}

	/**
	 * Handles when the import sequence button is clicked.
	 */
	private void importSequenceHandler() {
		SBOLDocument doc = SBOLUtils.importDoc();
		if (doc != null) {
			Set<Sequence> seqSet = doc.getSequences();
			String importedNucleotides = "";
			if (seqSet.size() == 1) {
				// only one Sequence
				importedNucleotides = seqSet.iterator().next().getElements();
			} else {
				// multiple Sequences
				importedNucleotides = new SequenceInputDialog(getParent(), seqSet).getInput();
			}
			sequenceField.setText(importedNucleotides);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent paramDocumentEvent) {
		if (canEdit) {
			saveButton.setEnabled(true);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent paramDocumentEvent) {
		if (canEdit) {
			saveButton.setEnabled(true);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent paramDocumentEvent) {
		if (canEdit) {
			saveButton.setEnabled(true);
		}
	}

	/**
	 * Returns the renamed CD or null if the user chooses "no". Also pass in the
	 * SBOLDocument containing the design.
	 */
	public static ComponentDefinition confirmEditing(Component parent, ComponentDefinition comp, SBOLDocument design)
			throws SBOLValidationException {
		int result = JOptionPane.showConfirmDialog(parent,
				"The part '" + comp.getDisplayId() + "' doesn't belong to your\n"
						+ "namespace and cannot be edited.\n\n" + "Do you want to create an editable copy of\n"
						+ "this part and save your changes?",
				"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.NO_OPTION) {
			return null;
		}

		return (ComponentDefinition) design.createCopy(comp,
				SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString(), comp.getDisplayId(),
				comp.getVersion());
	}
}
