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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.RestrictionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamtaft.eb.EventBus;
import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.dialog.PartEditDialog;
import com.clarkparsia.sbol.editor.dialog.SelectPartDialog;
import com.clarkparsia.sbol.editor.event.DesignChangedEvent;
import com.clarkparsia.sbol.editor.event.DesignLoadedEvent;
import com.clarkparsia.sbol.editor.event.FocusInEvent;
import com.clarkparsia.sbol.editor.event.FocusOutEvent;
import com.clarkparsia.sbol.editor.event.PartVisibilityChangedEvent;
import com.clarkparsia.sbol.editor.event.SelectionChangedEvent;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import uk.ac.ncl.intbio.core.io.CoreIoException;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLDesign {
	private static Logger LOGGER = LoggerFactory.getLogger(SBOLDesign.class.getName());

	private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	private static final int IMG_GAP = 10;
	private static final int IMG_HEIGHT = Part.IMG_HEIGHT;
	private static final int IMG_WIDTH = Part.IMG_WIDTH + IMG_GAP;
	private static final int IMG_PAD = 20;

	private static final boolean HEADLESS = GraphicsEnvironment.isHeadless();

	private enum ReadOnly {
		REGISTRY_COMPONENT, UNCOVERED_SEQUENCE, MISSING_START_END
	}

	public final SBOLEditorAction EDIT_ROOT = new SBOLEditorAction("Edit root component",
			"Edit root component information", "edit_root.gif") {
		@Override
		protected void perform() {
			editRootComponent();
		}
	};

	public final SBOLEditorAction FIND = new SBOLEditorAction("Find components", "Find components in the part registry",
			"find.gif") {
		@Override
		protected void perform() {
			findPartForSelectedComponent();
		}
	};

	public final SBOLEditorAction EDIT = new SBOLEditorAction("Edit component", "Edit selected component information",
			"edit.gif") {
		@Override
		protected void perform() {
			editSelectedComponent();

		}
	};
	public final SBOLEditorAction DELETE = new SBOLEditorAction("Delete component", "Delete the selected component",
			"delete.gif") {
		@Override
		protected void perform() {
			ComponentDefinition comp = getSelectedComponent();
			deleteComponent(comp);
		}
	};

	public final SBOLEditorAction FLIP = new SBOLEditorAction("Flip Orientation",
			"Flip the Orientation for the selected component", "flipStrand.png") {
		@Override
		protected void perform() {
			ComponentDefinition comp = getSelectedComponent();
			flipOrientation(comp);
		}
	};

	public final SBOLEditorAction HIDE_SCARS = new SBOLEditorAction("Hide scars", "Hide scars in the design",
			"hideScars.png") {
		@Override
		protected void perform() {
			boolean isVisible = isPartVisible(Parts.SCAR);
			setPartVisible(Parts.SCAR, !isVisible);
		}
	}.toggle();

	public final SBOLEditorAction ADD_SCARS = new SBOLEditorAction("Add scars",
			"Add a scar between every two non-scar component in the design", "addScars.png") {
		@Override
		protected void perform() {
			addScars();
		}
	};

	public final SBOLEditorAction FOCUS_IN = new SBOLEditorAction("Focus in",
			"Focus in the component to view and edit its subcomponents", "go_down.png") {
		@Override
		protected void perform() {
			focusIn();
		}
	};

	public final SBOLEditorAction FOCUS_OUT = new SBOLEditorAction("Focus out", "Focus out to the parent component",
			"go_up.png") {
		@Override
		protected void perform() {
			focusOut();
		}
	};

	private final EventBus eventBus;

	/**
	 * The DesignElements displayed on the currentComponent canvas.
	 */
	private final List<DesignElement> elements = Lists.newArrayList();
	private final Map<DesignElement, JLabel> buttons = Maps.newHashMap();
	private final Set<Part> hiddenParts = Sets.newHashSet();

	private final Set<ReadOnly> readOnly = EnumSet.noneOf(ReadOnly.class);

	private boolean loading = false;

	private boolean isCircular = false;
	private DesignElement selectedElement = null;

	private final Box elementBox;
	private final Box backboneBox;
	private final JPanel panel;

	private final JPopupMenu selectionPopupMenu = createPopupMenu(FIND, EDIT, FLIP, DELETE, FOCUS_IN);
	private final JPopupMenu noSelectionPopupMenu = createPopupMenu(EDIT_ROOT, FOCUS_OUT);

	/**
	 * The current CD displayed in the canvas.
	 */
	private ComponentDefinition currentComponent;

	private boolean hasSequence;

	private final Deque<ComponentDefinition> parentComponents = new ArrayDeque<ComponentDefinition>();

	public SBOLDesign(EventBus eventBus) {
		this.eventBus = eventBus;

		elementBox = Box.createHorizontalBox();
		elementBox.setBorder(BorderFactory.createEmptyBorder());
		elementBox.setOpaque(false);

		backboneBox = Box.createHorizontalBox();
		backboneBox.setBorder(BorderFactory.createEmptyBorder());
		backboneBox.setOpaque(false);

		JPanel contentPanel = new JPanel();
		contentPanel.setAlignmentX(0.5f);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder());
		contentPanel.setOpaque(false);
		contentPanel.setAlignmentY(0);
		contentPanel.add(elementBox);
		contentPanel.add(backboneBox);

		panel = new DesignPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(0.5f);
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.add(Box.createHorizontalGlue());
		Component leftStrut = Box.createHorizontalStrut(IMG_PAD + IMG_GAP);
		if (leftStrut instanceof JComponent) {
			((JComponent) leftStrut).setOpaque(false);
		}
		panel.add(leftStrut);
		panel.add(contentPanel);
		Component rightStrut = Box.createHorizontalStrut(IMG_PAD + IMG_GAP);
		if (rightStrut instanceof JComponent) {
			((JComponent) rightStrut).setOpaque(false);
		}
		panel.add(rightStrut);
		panel.add(Box.createHorizontalGlue());

		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				setSelectedElement(null);
				if (event.isPopupTrigger()) {
					noSelectionPopupMenu.show(panel, event.getX(), event.getY());
				}
			}
		});

		ActionListener deleteAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				if (selectedElement != null) {
					deleteComponent(getSelectedComponent());
				}
			}
		};
		KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		KeyStroke backspaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		panel.registerKeyboardAction(deleteAction, deleteKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
		panel.registerKeyboardAction(deleteAction, backspaceKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private static JPopupMenu createPopupMenu(SBOLEditorAction... actions) {
		final JPopupMenu popup = new JPopupMenu();

		for (SBOLEditorAction action : actions) {
			popup.add(action.createMenuItem());
		}

		return popup;
	}

	public boolean canFocusIn() {
		ComponentDefinition comp = getSelectedComponent();
		return comp != null;
	}

	public void focusIn() {
		Preconditions.checkState(canFocusIn(), "No selection to focus in");

		ComponentDefinition comp = getSelectedComponent();

		BufferedImage snapshot = getSnapshot();

		// TODO
		// updateRootComponent();
		parentComponents.push(currentComponent);

		load(comp);

		eventBus.publish(new FocusInEvent(this, comp, snapshot));
	}

	public boolean canFocusOut() {
		return !parentComponents.isEmpty();
	}

	public void focusOut() {
		Preconditions.checkState(canFocusOut(), "No parent design to focus out");

		focusOut(getParentComponent());
	}

	public void focusOut(ComponentDefinition comp) {
		if (currentComponent == comp) {
			return;
		}

		// TODO
		// updateRootComponent();

		ComponentDefinition parentComponent = parentComponents.pop();
		while (parentComponent != comp) {
			parentComponent = parentComponents.pop();
		}

		load(parentComponent);

		eventBus.publish(new FocusOutEvent(this, parentComponent));
	}

	public void load(SBOLDocument doc) {
		if (doc == null) {
			JOptionPane.showMessageDialog(panel, "No document to load.", "Load error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Importing the document
		// TODO Create preferences file
		try {
			doc.setDefaultURIprefix("http://fetchfrompreferences");
		} catch (SBOLValidationException e1) {
			JOptionPane.showMessageDialog(panel, "Preferences contains an invalid default URI prefix");
			e1.printStackTrace();
		}
		SBOLFactory.setSBOLDocument(doc);

		Iterator<ComponentDefinition> components = SBOLUtils.getRootComponentDefinitions(doc);
		ComponentDefinition newComponent = null;
		if (components.hasNext()) {
			// Sets newComponent to the root CD in the document
			newComponent = components.next();
			if (components.hasNext()) {
				// There is more than one root component
				JOptionPane.showMessageDialog(panel, "Cannot load documents with multiple root ComponentDefinitions.",
						"Load error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			try {
				newComponent = SBOLFactory.createComponentDefinition("RootComponent", ComponentDefinition.DNA);
			} catch (SBOLValidationException e) {
				JOptionPane.showMessageDialog(panel, "Error creating the root component");
				e.printStackTrace();
			}
			// newComponent.setURI(SBOLUtils.createURI());
			// newComponent.setDisplayId("Unnamed");
		}

		parentComponents.clear();
		load(newComponent);

		eventBus.publish(new DesignLoadedEvent(this));
	}

	private void load(ComponentDefinition newRoot) {
		loading = true;

		elementBox.removeAll();
		backboneBox.removeAll();
		elements.clear();
		buttons.clear();
		isCircular = false;
		readOnly.clear();

		currentComponent = newRoot;
		try {
			populateComponents(currentComponent);
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(panel, "Error loading the new root component definition");
			e.printStackTrace();
		}

		hasSequence = (currentComponent.getSequences() != null) && elements.isEmpty();

		detectReadOnly();

		selectedElement = null;

		loading = false;

		refreshUI();
		fireSelectionChangedEvent();
	}

	private void detectReadOnly() {
		if (SBOLUtils.isRegistryComponent(currentComponent)) {
			readOnly.add(ReadOnly.REGISTRY_COMPONENT);
		}

		Map<Integer, Sequence> uncoveredSequences = findUncoveredSequences();
		if (uncoveredSequences == null) {
			readOnly.add(ReadOnly.MISSING_START_END);
		} else if (!uncoveredSequences.isEmpty()) {
			readOnly.add(ReadOnly.UNCOVERED_SEQUENCE);
		}
	}

	private boolean confirmEditable() {
		if (readOnly.contains(ReadOnly.REGISTRY_COMPONENT)) {
			if (!PartEditDialog.confirmEditing(panel, currentComponent)) {
				return false;
			}
			readOnly.remove(ReadOnly.REGISTRY_COMPONENT);
		}

		if (readOnly.contains(ReadOnly.MISSING_START_END)) {
			int result = JOptionPane.showConfirmDialog(panel,
					"The component '" + currentComponent.getDisplayId() + "' has a DNA sequence but the\n"
							+ "subcomponents don't have start or end\n"
							+ "coordinates. If you edit the design you will\n" + "lose the DNA sequence.\n\n"
							+ "Do you want to continue with editing?",
					"Uncovered sequence", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.NO_OPTION) {
				return false;
			}
			readOnly.remove(ReadOnly.REGISTRY_COMPONENT);
		} else if (readOnly.contains(ReadOnly.UNCOVERED_SEQUENCE)) {
			String msg = "The sub components do not cover the DNA sequence\n" + "of the component '"
					+ currentComponent.getDisplayId() + "' completely.\n"
					+ "You need to add SCAR components to cover the missing\n"
					+ "parts or you will lose the uncovered DNA sequence.\n\n" + "How do you want to continue?";

			JRadioButton[] buttons = { new JRadioButton("Add SCAR Parts to handle uncovered sequences"),
					new JRadioButton("Continue with editing and lose the root DNA sequence"),
					new JRadioButton("Cancel the operation and do not edit the component") };

			JTextArea textArea = new JTextArea(msg);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setOpaque(false);
			textArea.setBorder(BorderFactory.createEmptyBorder());
			textArea.setAlignmentX(Component.LEFT_ALIGNMENT);

			Box box = Box.createVerticalBox();
			box.add(textArea);

			ButtonGroup group = new ButtonGroup();
			for (JRadioButton button : buttons) {
				button.setSelected(true);
				button.setAlignmentX(Component.LEFT_ALIGNMENT);
				group.add(button);
				box.add(button);
			}

			int result = JOptionPane.showConfirmDialog(panel, box, "Uncovered sequence", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.CANCEL_OPTION || buttons[2].isSelected()) {
				return false;
			}

			readOnly.remove(ReadOnly.UNCOVERED_SEQUENCE);

			if (buttons[0].isSelected()) {
				addScarsForUncoveredSequences();
			}
		}

		return true;
	}

	private void addScarsForUncoveredSequences() {
		Map<Integer, Sequence> uncoveredSequences = findUncoveredSequences();
		int insertCount = 0;
		int lastIndex = elements.size();
		for (Entry<Integer, Sequence> entry : uncoveredSequences.entrySet()) {
			int index = entry.getKey();
			Sequence seq = entry.getValue();
			try {
				if (index >= 0) {
					int updateIndex = index + insertCount;
					DesignElement e = elements.get(updateIndex);
					e.getComponentDefinition().clearSequences();
					e.getComponentDefinition().addSequence(seq);
				} else {
					int insertIndex = -index - 1 + insertCount++;

					addComponentDefinition(Parts.SCAR, false);

					DesignElement e = elements.get(lastIndex);
					e.getComponentDefinition().clearSequences();
					e.getComponentDefinition().addSequence(seq);

					moveComponent(lastIndex++, insertIndex);
				}
			} catch (SBOLValidationException e) {
				JOptionPane.showMessageDialog(panel, "Error in addScarsForUncoveredSequences");
				e.printStackTrace();
			}
		}

		createDocument();
	}

	private Map<Integer, Sequence> findUncoveredSequences() {
		return SBOLUtils.findUncoveredSequences(currentComponent,
				Lists.transform(elements, new Function<DesignElement, SequenceAnnotation>() {
					@Override
					public SequenceAnnotation apply(DesignElement e) {
						return e.getSeqAnn();
					}
				}));
	}

	/**
	 * Adds components in the order they appear in the sequence
	 */
	private void populateComponents(ComponentDefinition comp) throws SBOLValidationException {
		// If the rootComponent we passed in is empty, start with a blank
		// canvas.
		if (comp.getComponents().isEmpty()) {
			if (currentComponent != comp) {
				addComponentDefinition(comp);
			}
			return;
		}
		// get sortedComponents and add them in order
		Iterable<org.sbolstandard.core2.Component> sortedComponents = comp.getSortedComponents();
		for (org.sbolstandard.core2.Component component : sortedComponents) {
			ComponentDefinition refered = component.getDefinition();
			addComponentDefinition(component, refered, Parts.forComponent(refered));
		}
	}

	// My implementation of sorting componenets
	// // Returns a set of components in the order they are supposed to be added
	// private ArrayList<org.sbolstandard.core2.Component>
	// sortedComponents(ComponentDefinition comp) {
	// Multimap<org.sbolstandard.core2.Component,
	// org.sbolstandard.core2.Component> precedes = transitiveClosure(comp);
	// Map<org.sbolstandard.core2.Component,
	// Collection<org.sbolstandard.core2.Component>> precedesMap = precedes
	// .asMap();
	// ArrayList<org.sbolstandard.core2.Component> sortedComponents = new
	// ArrayList<org.sbolstandard.core2.Component>();
	// // iterate through precedes taking out components and storing them into
	// // sortedComponents until precedes is empty.
	// while (!precedesMap.isEmpty()) {
	// for (org.sbolstandard.core2.Component key : precedesMap.keySet()) {
	// if (precedesMap.get(key).isEmpty()) {
	// // add the key (Component) to sortedComponents and removes
	// // it from the map.
	// sortedComponents.add(key);
	// precedesMap.remove(key);
	// }
	// }
	// }
	// // the components in sortedCOmponents is in reverse order. Flip them
	// // around.
	// ArrayList<org.sbolstandard.core2.Component> temp = new
	// ArrayList<org.sbolstandard.core2.Component>();
	// for (int i = temp.size() - 1; i >= 0; i--) {
	// temp.add(sortedComponents.get(i));
	// }
	// sortedComponents = temp;
	//
	// return sortedComponents;
	// }
	//
	// // Creates a transitive closure based on the ComponentDefinition's
	// // SequenceConstraints and SequenceAnnotations.
	// private Multimap<org.sbolstandard.core2.Component,
	// org.sbolstandard.core2.Component> transitiveClosure(
	// ComponentDefinition comp) {
	// // precedes stores each component of this ComponentDefinition as keys
	// // and the components it precedes as values.
	// Multimap<org.sbolstandard.core2.Component,
	// org.sbolstandard.core2.Component> precedes = HashMultimap.create();
	//
	// Set<org.sbolstandard.core2.Component> components = comp.getComponents();
	// // put all of the components into precedes as keys
	// for (org.sbolstandard.core2.Component component : components) {
	// fillInPrecedes(comp, precedes, component);
	// }
	//
	// // put all of the values in as keys as well
	// for (org.sbolstandard.core2.Component value : precedes.values()) {
	// fillInPrecedes(comp, precedes, value);
	// }
	// // may have to do above step a couple of times if there are new
	// // values/precedes still changes?
	//
	// return precedes;
	// }
	//
	// // put all of the components this component precedes as values in
	// precedes
	// // if the component is already in precedes as a key, don't do anything
	// private void fillInPrecedes(ComponentDefinition comp,
	// Multimap<org.sbolstandard.core2.Component,
	// org.sbolstandard.core2.Component> precedes,
	// org.sbolstandard.core2.Component component) {
	// if (precedes.containsKey(component)) {
	// return;
	// } else {
	// // deal with the constraints
	// Set<SequenceConstraint> constraints = comp.getSequenceConstraints();
	// for (SequenceConstraint constraint : constraints) {
	// if (constraint.getRestriction() == RestrictionType.PRECEDES
	// && constraint.getSubject().equals(constraint)) {
	// precedes.put(component, constraint.getObject());
	// }
	// }
	// // deal with the annotations
	// Set<SequenceAnnotation> annotations = comp.getSequenceAnnotations();
	// for (SequenceAnnotation annotation : annotations) {
	// if (annotation.getComponent().equals(component)) {
	// addAllPrecedes(precedes, component, annotation, annotations);
	// }
	// }
	// }
	// }
	//
	// // put into precedes all of the components whose annotations follow
	// // component's annotation
	// private void addAllPrecedes(Multimap<org.sbolstandard.core2.Component,
	// org.sbolstandard.core2.Component> precedes,
	// org.sbolstandard.core2.Component component, SequenceAnnotation
	// annotation,
	// Set<SequenceAnnotation> annotations) {
	// // Only looks at the first location in the set
	// Location componentLocation = annotation.getLocations().iterator().next();
	// // Treats componentLocation as either a range or cut. If the location is
	// // a GenericLocation, it doesn't precede anything
	// // (Integer.MAX_VALUE)
	// int componentStart = Integer.MAX_VALUE;
	// if (componentLocation instanceof Range) {
	// componentStart = ((Range) componentLocation).getStart();
	// }
	// if (componentLocation instanceof Cut) {
	// componentStart = ((Cut) componentLocation).getAt();
	// }
	// // iterate through all the annotations,
	// for (SequenceAnnotation ann : annotations) {
	// // Only looks at the first location
	// Location loc = ann.getLocations().iterator().next();
	// if (loc instanceof Range) {
	// if (((Range) loc).getStart() > componentStart) {
	// precedes.put(component, ann.getComponent());
	// }
	// }
	// if (loc instanceof Cut) {
	// if (((Cut) loc).getAt() > componentStart) {
	// precedes.put(component, ann.getComponent());
	// }
	// }
	// }
	// }

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// the original implementation of sorting components

	// private void populateComponents(ComponentDefinition comp) {
	// if (comp.getAnnotations().isEmpty()) {
	// if (currentComponent != comp) {
	// addComponent(comp);
	// }
	// return;
	// }
	//
	// Iterable<SequenceAnnotation> sortedAnnotations =
	// sortedAnnotations(comp.getAnnotations());
	// //
	// System.out.println(Iterators.toString(Iterators.transform(sortedAnnotations.iterator(),
	// // new Function<SequenceAnnotation,String>() {
	// // public String apply(SequenceAnnotation ann) {
	// // return "\n" + ann.getURI().toString() + " " +
	// // ann.getSubComponent().getURI() + " " + ann.getBioStart() + " " +
	// // ann.getBioEnd();
	// // }
	// // })));
	// int lastStart = -1;
	// int lastEnd = -1;
	// for (SequenceAnnotation ann : sortedAnnotations) {
	// // even though we are using SequenceConstraints, we still use the
	// // annotation to get the range?
	// Range range = (Range) ann.getLocations().iterator().next();
	// if (range.getStart() >= lastStart && range.getEnd() <= lastEnd) {
	// continue;
	// }
	// // this is the case where we have a sequence within another
	// // sequence?
	// lastStart = range.getStart();
	// lastEnd = range.getEnd();
	//
	// ComponentDefinition subComp = ann.getComponentDefinition();
	// if (subComp != null) {
	// // There isn't an add component for sequenceConstraints.
	// addComponent(ann, subComp, Parts.forComponent(subComp));
	// }
	// }
	// }
	//
	// private Multimap<SequenceAnnotation, SequenceAnnotation>
	// computePrecedesTransitive(
	// Iterable<SequenceAnnotation> annotations) {
	// // google what HashMultimap is
	// Multimap<SequenceAnnotation, SequenceAnnotation> precedes =
	// HashMultimap.create();
	// // google what .newLinkedHashSet is
	// Set<SequenceAnnotation> visited = Sets.newLinkedHashSet();
	// for (SequenceAnnotation ann : annotations) {
	// computePrecedesTransitive(ann, precedes, visited);
	// }
	// return precedes;
	// }
	//
	// // What is the point of transitive?
	// // Example, if A is larger than B, and B is larger than C, then A is
	// larger
	// // than C.
	// private void computePrecedesTransitive(SequenceAnnotation ann,
	// Multimap<SequenceAnnotation, SequenceAnnotation> precedes,
	// Set<SequenceAnnotation> visited) {
	// if (!visited.add(ann)) {
	// LOGGER.warn("Circular precedes relation: " + Iterators
	// .toString(Iterators.transform(visited.iterator(), new
	// Function<SequenceAnnotation, String>() {
	// public String apply(SequenceAnnotation ann) {
	// return ann.getIdentity().toString();
	// }
	// })));
	// return;
	// }
	//
	// if (!precedes.containsKey(ann)) {
	// for (SequenceAnnotation nextAnn : ann.getPrecedes()) {
	// computePrecedesTransitive(nextAnn, precedes, visited);
	// precedes.put(ann, nextAnn);
	// precedes.putAll(ann, precedes.get(nextAnn));
	// }
	// }
	//
	// visited.remove(ann);
	// }
	//
	// private Iterable<SequenceAnnotation>
	// sortedAnnotations(Set<SequenceAnnotation> annotations) {
	// final Multimap<SequenceAnnotation, SequenceAnnotation> precedesTransitive
	// = computePrecedesTransitive(
	// annotations); // what is a multimap, reminder to google
	// return new PartialOrder<SequenceAnnotation>(new
	// PartialOrderComparator<SequenceAnnotation>() {
	// // Comparator used to compare SequenceAnnotations
	// @Override
	// public PartialOrderRelation compare(SequenceAnnotation a,
	// SequenceAnnotation b) {
	// if (precedesTransitive.containsEntry(a, b)) {
	// return PartialOrderRelation.LESS;
	// }
	//
	// if (precedesTransitive.containsEntry(b, a)) {
	// return PartialOrderRelation.GREATER;
	// }
	//
	// try {
	// Range rangeA = (Range) a.getLocations().iterator().next();
	// Range rangeB = (Range) b.getLocations().iterator().next();
	// // Ints.compare returns the smallest or biggest? Reminder to
	// // google
	// int cmpStart = Ints.compare(rangeA.getStart(), rangeB.getStart());
	// int cmpEnd = Ints.compare(rangeA.getEnd(), rangeB.getEnd());
	// if (cmpStart < 0) {
	// return PartialOrderRelation.LESS;
	// } else if (cmpStart > 0) {
	// return PartialOrderRelation.GREATER;
	// } else if (cmpEnd < 0) {
	// return PartialOrderRelation.GREATER;
	// } else if (cmpEnd > 0) {
	// return PartialOrderRelation.LESS;
	// } else {
	// return PartialOrderRelation.EQUAL;
	// }
	// } catch (Exception e) {
	// return PartialOrderRelation.INCOMPARABLE;
	// }
	//
	// }
	// });
	// }
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isCircular() {
		return isCircular;
	}

	public JPanel getPanel() {
		return panel;
	}

	public Part getPart(ComponentDefinition comp) {
		DesignElement e = getElement(comp);
		return e == null ? null : e.part;
	}

	private DesignElement getElement(ComponentDefinition comp) {
		int index = getElementIndex(comp);
		return index < 0 ? null : elements.get(index);
	}

	private int getElementIndex(ComponentDefinition comp) {
		for (int i = 0, n = elements.size(); i < n; i++) {
			DesignElement e = elements.get(i);
			if (e.getComponentDefinition() == comp) {
				return i;
			}
		}
		return -1;
	}

	public ComponentDefinition getRootComponent() {
		return parentComponents.isEmpty() ? currentComponent : parentComponents.getFirst();
	}

	public ComponentDefinition getCurrentComponent() {
		return currentComponent;
	}

	public ComponentDefinition getParentComponent() {
		return parentComponents.peek();
	}

	public ComponentDefinition getSelectedComponent() {
		return selectedElement == null ? null : selectedElement.getComponentDefinition();
	}

	public boolean setSelectedComponent(ComponentDefinition comp) {
		DesignElement e = (comp == null) ? null : getElement(comp);
		setSelectedElement(e);
		return (e != null);
	}

	private void setSelectedElement(DesignElement element) {
		if (selectedElement != null) {
			buttons.get(selectedElement).setEnabled(true);
		}

		selectedElement = element;

		if (selectedElement != null) {
			buttons.get(selectedElement).setEnabled(false);
		}

		fireSelectionChangedEvent();
	}

	public void addComponentDefinition(ComponentDefinition comp) {
		addComponentDefinition(null, comp, Parts.forComponent(comp));
	}

	public ComponentDefinition addComponentDefinition(Part part, boolean edit) {
		if (!confirmEditable()) {
			return null;
		}

		ComponentDefinition comp = part.createComponentDefinition();
		if (edit && PartEditDialog.editPart(panel.getParent(), comp, edit) == null) {
			return null;
		}

		addComponentDefinition(null, comp, part);

		return comp;
	}

	/**
	 * Adds the part to elements. Takes in a component if one already exists,
	 * the CD, and the part.
	 */
	private void addComponentDefinition(org.sbolstandard.core2.Component component, ComponentDefinition comp,
			Part part) {
		boolean backbone = (part == Parts.ORI);
		DesignElement e = new DesignElement(component, currentComponent, comp, part);
		JLabel button = createComponentButton(e);

		// TODO debugging
		try {
			SBOLFactory.write(System.out);
		} catch (XMLStreamException | FactoryConfigurationError | CoreIoException | IOException e1) {
			e1.printStackTrace();
		}

		if (backbone) {
			if (isCircular) {
				throw new IllegalArgumentException("Cannot add multiple origin of replication parts");
			}
			elements.add(0, e);
			backboneBox.add(button);
			isCircular = true;
		} else {
			elements.add(e);
			elementBox.add(button);
		}
		buttons.put(e, button);

		if (!isPartVisible(part)) {
			setPartVisible(part, true);
		}

		if (!loading) {
			fireDesignChangedEvent();
		}
	}

	public void moveComponent(int source, int target) {
		if (!confirmEditable()) {
			return;
		}

		DesignElement element = elements.remove(source);
		elements.add(element);

		JLabel button = buttons.get(element);
		elementBox.remove(button);
		elementBox.add(button, target);

		fireDesignChangedEvent();
	}

	private void setupIcons(final JLabel button, final DesignElement e) {
		Image image = e.getPart().getImage(e.getOrientation());
		Image selectedImage = Images.createBorderedImage(image, Color.LIGHT_GRAY);
		button.setIcon(new ImageIcon(image));
		button.setDisabledIcon(new ImageIcon(selectedImage));
	}

	private JLabel createComponentButton(final DesignElement e) {
		final JLabel button = new JLabel();
		setupIcons(button, e);
		button.setVerticalAlignment(JLabel.TOP);
		button.setVerticalTextPosition(JLabel.TOP);
		button.setIconTextGap(2);
		button.setText(e.getComponentDefinition().getDisplayId());
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setToolTipText(getTooltipText(e));
		button.setMaximumSize(new Dimension(IMG_WIDTH, IMG_HEIGHT + 20));
		button.setPreferredSize(new Dimension(IMG_WIDTH, IMG_HEIGHT + 20));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setFont(LABEL_FONT);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				setSelectedElement(e);
				if (event.isPopupTrigger()) {
					selectionPopupMenu.show(button, event.getX(), event.getY());
				}
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					focusIn();
				}
			}
		});
		// button.setComponentPopupMenu(popupMenu);

		boolean isDraggable = (e.getPart() != Parts.ORI);
		if (isDraggable) {
			setupDragActions(button, e);
		}

		return button;
	}

	private void setupDragActions(final JLabel button, final DesignElement e) {
		if (HEADLESS) {
			return;
		}
		final DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(button, DnDConstants.ACTION_COPY_OR_MOVE,
				new DragGestureListener() {
					@Override
					public void dragGestureRecognized(DragGestureEvent event) {
						Transferable transferable = new JLabelTransferable(button);
						dragSource.startDrag(event, DragSource.DefaultMoveDrop, transferable, new DragSourceAdapter() {
						});
					}
				});

		new DropTarget(button, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent event) {
				int index = elements.indexOf(e);
				if (index >= 0) {
					Point loc = event.getLocation();
					if (loc.getX() > button.getWidth() * 0.75 && index < elements.size() - 1) {
						index++;
					}
					moveSelectedElement(index);
				}
				event.dropComplete(true);
			}
		});
	}

	private String getTooltipText(DesignElement e) {
		final ComponentDefinition comp = e.getComponentDefinition();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<b>Display ID:</b> ").append(comp.getDisplayId()).append("<br>");
		sb.append("<b>Name:</b> ").append(Strings.nullToEmpty(comp.getName())).append("<br>");
		sb.append("<b>Description:</b> ").append(Strings.nullToEmpty(comp.getDescription())).append("<br>");
		if (e.getOrientation() != null) {
			sb.append("<b>Orientation:</b> ").append(e.getOrientation()).append("<br>");
		}
		if (!comp.getSequences().isEmpty() && comp.getSequences().iterator().next().getElements() != null) {
			// String sequence = comp.getSequence().getNucleotides();
			String sequence = comp.getSequences().iterator().next().getElements();
			sb.append("<b>Sequence Length:</b> ").append(sequence.length()).append("<br>");
			sb.append("<b>Sequence:</b> ").append(CharSequences.shorten(sequence, 25));
			sb.append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}

	private void moveSelectedElement(int index) {
		if (selectedElement != null) {
			int selectedIndex = elements.indexOf(selectedElement);
			if (selectedIndex >= 0 && selectedIndex != index) {
				elements.remove(selectedIndex);
				elements.add(index, selectedElement);

				int indexAdjustment = isCircular ? -1 : 0;
				JLabel button = buttons.get(selectedElement);
				elementBox.remove(selectedIndex + indexAdjustment);
				elementBox.add(button, index + indexAdjustment);

				fireDesignChangedEvent();
			}
		}
	}

	public void flipOrientation(ComponentDefinition comp) {
		if (!confirmEditable()) {
			return;
		}

		DesignElement e = getElement(comp);
		e.flipOrientation();

		JLabel button = buttons.get(e);
		setupIcons(button, e);
		button.setToolTipText(getTooltipText(e));

		fireDesignChangedEvent();
	}

	public void deleteComponent(ComponentDefinition component) {
		if (!confirmEditable()) {
			return;
		}

		int index = getElementIndex(component);
		if (index >= 0) {
			DesignElement e = elements.get(index);

			if (e == selectedElement) {
				// TODO Delete this CD and all its Sequences from SBOLFactory
				setSelectedElement(null);
			}

			JLabel button = buttons.remove(e);
			elements.remove(index);
			if (isCircular && index == 0) {
				backboneBox.remove(button);
				isCircular = false;
			} else {
				elementBox.remove(button);
			}

			// TODO debugging
			try {
				SBOLFactory.write(System.out);
			} catch (XMLStreamException | FactoryConfigurationError | CoreIoException | IOException e1) {
				e1.printStackTrace();
			}

			fireDesignChangedEvent();
		}
	}

	private void replaceComponent(ComponentDefinition component, ComponentDefinition newComponent) {
		int index = getElementIndex(component);
		if (index >= 0) {
			DesignElement e = elements.get(index);
			JLabel button = buttons.get(e);
			try {
				e.setComponentDefinition(newComponent);
			} catch (SBOLValidationException e1) {
				JOptionPane.showMessageDialog(panel, "There was an error replacing the component");
				e1.printStackTrace();
			}
			if (!newComponent.getRoles().contains(e.getPart().getRole())) {
				Part newPart = Parts.forComponent(newComponent);
				if (newPart == null) {
					try {
						newComponent.addRole(e.getPart().getRole());
					} catch (SBOLValidationException e1) {
						JOptionPane.showMessageDialog(panel, "There was an error replacing the component");
						e1.printStackTrace();
					}
				} else {
					e.setPart(newPart);
					setupIcons(button, e);
				}
			}
			button.setText(newComponent.getDisplayId());
			button.setToolTipText(getTooltipText(e));

			fireDesignChangedEvent();
		}
	}

	private void refreshUI() {
		panel.revalidate();
		panel.repaint();
	}

	private void fireDesignChangedEvent() {
		refreshUI();
		eventBus.publish(new DesignChangedEvent(this));
	}

	private void fireSelectionChangedEvent() {
		updateEnabledActions();
		eventBus.publish(new SelectionChangedEvent(getSelectedComponent()));
	}

	private void updateEnabledActions() {
		boolean isEnabled = (selectedElement != null);
		FIND.setEnabled(isEnabled);
		EDIT.setEnabled(isEnabled);
		DELETE.setEnabled(isEnabled);
		FLIP.setEnabled(isEnabled);
		FOCUS_IN.setEnabled(canFocusIn());
		FOCUS_OUT.setEnabled(canFocusOut());
	}

	public boolean isPartVisible(Part part) {
		return !hiddenParts.contains(part);
	}

	public void setPartVisible(Part part, boolean isVisible) {
		boolean visibilityChanged = isVisible ? hiddenParts.remove(part) : hiddenParts.add(part);

		if (visibilityChanged) {
			for (DesignElement e : elements) {
				if (e.getPart().equals(part)) {
					JLabel button = buttons.get(e);
					button.setVisible(isVisible);
				}
			}

			if (part.equals(Parts.SCAR)) {
				HIDE_SCARS.putValue(Action.SELECTED_KEY, !isVisible);
			}

			refreshUI();

			eventBus.publish(new PartVisibilityChangedEvent(part, isVisible));

			if (selectedElement != null && part.equals(selectedElement.getPart())) {
				setSelectedElement(null);
			}
		}
	}

	public void addScars() {
		if (!confirmEditable()) {
			return;
		}

		int size = elements.size();
		int start = isCircular ? 1 : 0;
		int end = size - 1;
		DesignElement curr = (size == 0) ? null : elements.get(start);
		for (int i = start; i < end; i++) {
			DesignElement next = elements.get(i + 1);

			if (curr.getPart() != Parts.SCAR && next.getPart() != Parts.SCAR) {
				DesignElement scar = new DesignElement(null, currentComponent, Parts.SCAR.createComponentDefinition(),
						Parts.SCAR);
				JLabel button = createComponentButton(scar);

				elements.add(i + 1, scar);
				elementBox.add(button, i + 1 - start);
				buttons.put(scar, button);
				end++;
				i++;
			}
			curr = next;
		}

		if (size != elements.size()) {
			fireDesignChangedEvent();
		}

		setPartVisible(Parts.SCAR, true);
	}

	public void editRootComponent() {
		if (!confirmEditable()) {
			return;
		}

		ComponentDefinition comp = getCurrentComponent();

		boolean edited = PartEditDialog.editPart(panel.getParent(), comp, false) != null;

		if (edited) {
			fireDesignChangedEvent();
		}
	}

	public void editSelectedComponent() {
		if (!confirmEditable()) {
			return;
		}

		ComponentDefinition comp = getSelectedComponent();
		ComponentDefinition newComp = PartEditDialog.editPart(panel.getParent(), comp, false);
		boolean edited = newComp != null;
		if (edited) {
			try {
				// if the CD type or the displyId has been edited we need to
				// update the component view so we'll replace it with the new CD
				replaceComponent(comp, newComp);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(panel, "There was an error applying the edits");
			}
		}
	}

	public void findPartForSelectedComponent() {
		Part part = selectedElement.getPart();
		ComponentDefinition newComponent = new SelectPartDialog(panel.getParent(), part).getInput();

		if (newComponent != null) {
			if (!confirmEditable()) {
				return;
			}

			try {
				replaceComponent(selectedElement.getComponentDefinition(), newComponent);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(panel, "There was an error adding the selected part to the design");
			}
		}
	}

	public BufferedImage getSnapshot() {
		BufferedImage image = Images.createImage(panel);

		int totalWidth = panel.getWidth();
		int designWidth = elementBox.getWidth();
		int designHeight = elementBox.getHeight();

		int x = (totalWidth - designWidth) / 2;
		if (isCircular) {
			x -= IMG_PAD;
			designWidth += (2 * IMG_PAD);
			designHeight += backboneBox.getHeight();
		}

		return image.getSubimage(Math.max(0, x - IMG_PAD), 0, Math.min(designWidth + 2 * IMG_PAD, totalWidth),
				designHeight);
	}

	public SBOLDocument createDocument() {
		// TODO
		// updateRootComponent();

		ComponentDefinition comp = parentComponents.isEmpty() ? currentComponent : parentComponents.getFirst();
		SBOLDocument doc = new SBOLDocument();
		// doc.addContent(comp);
		try {
			doc.createCopy(comp);
			doc.setDefaultURIprefix("http://fetchfrompreferences");
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(panel, "Error creating document from the root component");
			e.printStackTrace();
		}
		SBOLFactory.setSBOLDocument(doc);

		return doc;
	}

	/**
	 * Updates the currentComponent's sequences and
	 * SequenceAnnotation/Constraints.
	 */
	private void updateRootComponent() {
		try {
			currentComponent.clearSequenceAnnotations();

			// TODO this is not correct
			StringBuilder rootSequence = new StringBuilder();
			int location = 1;
			SequenceAnnotation prev = null;
			for (DesignElement e : elements) {
				ComponentDefinition comp = e.getComponentDefinition();
				SequenceAnnotation ann = e.getSeqAnn();

				Iterator<Sequence> iter = comp.getSequences().iterator();
				if (location >= 0 && comp.getSequences() != null && iter.hasNext()) {
					Sequence seq = iter.next();
					String nucleotides = seq.getElements();
					rootSequence.append(nucleotides);
					// ann.setBioStart(location);
					int rangeStart = location;
					location += nucleotides.length();
					// ann.setBioEnd(location - 1);
					int rangeEnd = location;
					// TODO is ann.getDisplayId() the right displayId to pass to
					// this create range method?
					ann.addRange(ann.getDisplayId(), rangeStart, rangeEnd);
				} else {
					location = -1;
					// ann.setBioStart(null);
					// ann.setBioEnd(null);
					// TODO is ann.getDisplayId() the right displayId to pass to
					// this create range method?
					ann.removeLocation(ann.getLocation(ann.getDisplayId()));
				}

				if (prev != null) {
					// prev.getPrecedes().clear();
					// prev.addPrecede(ann);
					comp.createSequenceConstraint("temp", RestrictionType.PRECEDES, prev.getComponent().getDisplayId(),
							ann.getComponent().getDisplayId());
				}
				// TODO Only ever looks at the first location, there may be more
				// than one
				prev = ann;
			}

			if (location > 0 && !elements.isEmpty()) {
				// Sequence seq = SBOLFactory.createSequence();
				// seq.setURI(SBOLUtils.createURI());
				// seq.setNucleotides(rootSequence.toString());
				Sequence seq = SBOLFactory.createSequence("Root Sequence", rootSequence.toString(), Sequence.IUPAC_DNA);
				currentComponent.clearSequences();
				currentComponent.addSequence(seq);
			} else if (!hasSequence) {
				// currentComponent.setSequence(null);
				currentComponent.clearSequences();
			}

			LOGGER.debug("Updated root:\n{}", currentComponent.toString());
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(panel, "Error in updating root component");
			e.printStackTrace();
		}
	}

	private static class DesignElement {
		private final org.sbolstandard.core2.Component component;
		private final SequenceAnnotation seqAnn;
		private Part part;

		public DesignElement(org.sbolstandard.core2.Component component, ComponentDefinition currentComponent,
				ComponentDefinition comp, Part part) {
			// Only create a new component if one does not already exist
			if (component == null) {
				this.component = createComponent(currentComponent, comp);
			} else {
				this.component = component;
			}
			this.seqAnn = createSeqAnn(currentComponent);
			this.part = part;
		}

		private static org.sbolstandard.core2.Component createComponent(ComponentDefinition currentComponent,
				ComponentDefinition childComp) {
			// SequenceAnnotation seqAnn =
			// SublimeSBOLFactory.createSequenceAnnotation();
			// seqAnn.setURI(SBOLUtils.createURI());
			// seqAnn.setSubComponent(component);
			// seqAnn.setOrientation(OrientationType.INLINE);
			try {
				int unique = SBOLUtils.getUniqueNumber(currentComponent, childComp.getDisplayId() + "Component",
						"Component");
				return currentComponent.createComponent(childComp.getDisplayId() + "Component" + unique,
						AccessType.PUBLIC, childComp.getDisplayId());
			} catch (SBOLValidationException e) {
				// TODO Generate error
				e.printStackTrace();
				return null;
			}
		}

		private static SequenceAnnotation createSeqAnn(ComponentDefinition currentComponent) {
			try {
				int unique = SBOLUtils.getUniqueNumber(currentComponent,
						currentComponent.getDisplayId() + "SequenceAnnotation", "SequenceAnnotation");
				return currentComponent.createSequenceAnnotation(
						currentComponent.getDisplayId() + "SequenceAnnotation" + unique, "genericLocation",
						OrientationType.INLINE);
			} catch (SBOLValidationException e) {
				// TODO Generate error
				e.printStackTrace();
				return null;
			}
		}

		SequenceAnnotation getSeqAnn() {
			return seqAnn;
		}

		void setComponentDefinition(ComponentDefinition CD) throws SBOLValidationException {
			// seqAnn.setSubComponent(component);
			this.component.setDefinition(CD.getIdentity());
		}

		ComponentDefinition getComponentDefinition() {
			return component.getDefinition();
		}

		void setPart(Part part) {
			this.part = part;
		}

		Part getPart() {
			return part;
		}

		public OrientationType getOrientation() {
			return seqAnn.getLocations().iterator().next().getOrientation();
		}

		void flipOrientation() {
			OrientationType Orientation = seqAnn.getLocations().iterator().next().getOrientation();
			try {
				seqAnn.getLocations().iterator().next().setOrientation(Orientation == OrientationType.REVERSECOMPLEMENT
						? OrientationType.INLINE : OrientationType.REVERSECOMPLEMENT);
			} catch (SBOLValidationException e) {
				// TODO Generate error
				e.printStackTrace();
			}
		}

		public String toString() {
			return getComponentDefinition().getDisplayId()
					+ (seqAnn.getLocations().iterator().next().getOrientation() == OrientationType.REVERSECOMPLEMENT
							? "-" : "");
		}
	}

	private class DesignPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			// clear the background
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			// draw the line
			g2d.setColor(Color.black);
			g2d.setPaint(Color.black);
			g2d.setStroke(new BasicStroke(4.0f));

			if (!elements.isEmpty()) {
				int totalWidth = getWidth();
				int designWidth = Math.max(elementBox.getWidth(), backboneBox.getWidth());

				int x = (totalWidth - designWidth) / 2;
				int y = IMG_HEIGHT / 2;

				if (!isCircular) {
					g.drawLine(x, y, totalWidth - x, y);
				} else {
					g.drawRoundRect(x - IMG_PAD, y, designWidth + 2 * IMG_PAD, backboneBox.getHeight(), IMG_PAD,
							IMG_PAD);
				}
			}

			// draw the rest
			super.paintComponent(g);
		}
	}

	private static class JLabelTransferable implements Transferable {
		// A flavor that transfers a copy of the JLabel
		public static final DataFlavor FLAVOR = new DataFlavor(JButton.class, "JLabel");

		private static final DataFlavor[] FLAVORS = new DataFlavor[] { FLAVOR };

		private JLabel label; // The label being transferred

		public JLabelTransferable(JLabel label) {
			this.label = label;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return FLAVORS;
		}

		public boolean isDataFlavorSupported(DataFlavor fl) {
			return fl.equals(FLAVOR);
		}

		public Object getTransferData(DataFlavor fl) {
			if (!isDataFlavorSupported(fl)) {
				return null;
			}

			return label;
		}
	}
}
