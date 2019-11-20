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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.apache.commons.httpclient.URIException;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Cut;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;
import org.sbolstandard.core2.VariableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import edu.utah.ece.async.sboldesigner.sbol.CombinatorialExpansionUtil;
import edu.utah.ece.async.sboldesigner.sbol.ProvenanceUtil;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.ComponentDefinitionBox;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.MessageDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PartEditDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryInputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryLoginDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RootInputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.UploadExistingDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.UploadNewDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.VariantEditor;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.DesignChangedEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.DesignLoadedEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.FocusInEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.FocusOutEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.PartVisibilityChangedEvent;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.SelectionChangedEvent;

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

	public final SBOLEditorAction EDIT_CANVAS = new SBOLEditorAction("Edit canvas part", "Edit canvas part information",
			"edit_root.png") {
		@Override
		protected void perform() {
			try {
				editCanvasCD();
			} catch (SBOLValidationException e) {
				MessageDialog.showMessage(panel, "There was an error applying the edits: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction FIND = new SBOLEditorAction("Find parts", "Find parts in the part registry",
			"find.png") {
		@Override
		protected void perform() {
			try {
				findPartForSelectedCD();
			} catch (Exception e) {
				MessageDialog.showMessage(panel, "There was a problem finding a part: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction VARIANTS = new SBOLEditorAction("Edit variants",
			"Edit combinatorial design variants of selected part", "dna edit.png") {
		@Override
		protected void perform() {
			try {
				editVariants();
			} catch (Exception e) {
				MessageDialog.showMessage(panel, "There was a problem finding a part: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction UPLOAD = new SBOLEditorAction("Upload design",
			"Upload the current design into a SynBioHub instance", "upload.png") {
		@Override
		protected void perform() {
			try {
				if (!designerPanel.confirmSave()) {
					return;
				}

				String[] options = { "Current design", "Working document" };
				int choice = JOptionPane.showOptionDialog(panel, "What would you like to upload?", "Upload",
						JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				if (choice == 0) {
					ComponentDefinitionBox root = new ComponentDefinitionBox();
					SBOLDocument uploadDoc = createDocument(root);
					uploadDesign(panel, uploadDoc, null);
				} else if (choice == 1) {
					if (designerPanel.documentIO == null) {
						if (!designerPanel.selectCurrentFile()) {
							return;
						}
					}

					if (!SBOLUtils.setupFile().exists()) {
						JOptionPane.showMessageDialog(panel, "The working document does not exist.");
						return;
					}

					SBOLDocument uploadDoc = designerPanel.documentIO.read();
					uploadDesign(panel, uploadDoc, null);
				} else {
					return;
				}
			} catch (Exception e) {
				MessageDialog.showMessage(panel, "There was a problem uploading the design: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction COMBINATORIAL = new SBOLEditorAction("Expand combinatorial design",
			"Expand the combinatorial design", "combinatorial expansion.png") {
		@Override
		protected void perform() {
			try {
				expandCombinatorial();
			} catch (SBOLValidationException | FileNotFoundException | SBOLConversionException e) {
				MessageDialog.showMessage(panel, "There was a problem performing the combinatorial design expansion: ",
						e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction EDIT = new SBOLEditorAction("Edit part", "Edit selected part information",
			"edit.png") {
		@Override
		protected void perform() {
			try {
				editSelectedCD();
			} catch (SBOLValidationException | URISyntaxException e) {
				MessageDialog.showMessage(panel, "There was an error applying the edits: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};
	public final SBOLEditorAction DELETE = new SBOLEditorAction("Delete part", "Delete the selected part",
			"delete.png") {
		@Override
		protected void perform() {
			try {
				if(features.isEmpty()) {
					ComponentDefinition comp = getSelectedCD();
					deleteCD(comp);
				}
			} catch (SBOLValidationException | URISyntaxException e) {
				MessageDialog.showMessage(panel, "There was an error deleting the part: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction FLIP = new SBOLEditorAction("Flip Orientation",
			"Flip the Orientation for the selected part", "flipOrientation.png") {
		@Override
		protected void perform() {
			try {
				if(features.isEmpty()) {
					ComponentDefinition comp = getSelectedCD();
					flipOrientation(comp);
				}
			} catch (SBOLValidationException | URISyntaxException e) {
				MessageDialog.showMessage(panel, "There was an error flipping the orientation: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction HIDE_SCARS = new SBOLEditorAction("Hide scars", "Hide scars in the design",
			"hideScars.png") {
		@Override
		protected void perform() {
			if(features.isEmpty()) {
				boolean isVisible = isPartVisible(Parts.SCAR);
				setPartVisible(Parts.SCAR, !isVisible);
			}
		}
	}.toggle();

	public final SBOLEditorAction ADD_SCARS = new SBOLEditorAction("Add scars",
			"Add a scar between every two non-scar part in the design", "addScars.png") {
		@Override
		protected void perform() {
			try {
				if(features.isEmpty()) {
					addScars();
				}
			} catch (SBOLValidationException | URISyntaxException e) {
				MessageDialog.showMessage(panel, "There was a problem adding scars: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction FOCUS_IN = new SBOLEditorAction("Focus in",
			"Focus in the part to view and edit its subparts", "go_down.png") {
		@Override
		protected void perform() {
			try {
				focusIn();
			} catch (SBOLValidationException e) {
				MessageDialog.showMessage(panel, "There was a problem focussing in: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	public final SBOLEditorAction FOCUS_OUT = new SBOLEditorAction("Focus out", "Focus out to the parent part",
			"go_up.png") {
		@Override
		protected void perform() {
			try {
				focusOut();
			} catch (SBOLValidationException e) {
				MessageDialog.showMessage(panel, "There was a problem focussing out: ", e.getMessage());
				e.printStackTrace();
			}
		}
	};

	private final EventBus eventBus;

	/**
	 * The DesignElements displayed on the canvasCD.
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
	private final JPopupMenu noSelectionPopupMenu = createPopupMenu(EDIT_CANVAS, FOCUS_OUT);

	/**
	 * The SBOLDocument containing our design.
	 */
	private SBOLDocument design;

	/**
	 * The current CD displayed in the canvas.
	 */
	private ComponentDefinition canvasCD;
	
	/**
	 * Features and featureStack is responsible for displaying the desired 
	 * range of features on the canvas. 
	 * */
	public final static ArrayList<Feature> features = new ArrayList<Feature>();
	private final Stack<Feature> featureRange = new Stack<Feature>();
	private final Stack<Integer> zoomStack = new Stack<Integer>();

	private final Deque<ComponentDefinition> parentCDs = new ArrayDeque<ComponentDefinition>();

	private SBOLDesignerPanel designerPanel;

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
					try {
						deleteCD(getSelectedCD());
					} catch (SBOLValidationException | URISyntaxException e) {
						MessageDialog.showMessage(panel, "There was an problem deleting this part: ", e.getMessage());
						e.printStackTrace();
					}
				}
			}
		};
		KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		KeyStroke backspaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		panel.registerKeyboardAction(deleteAction, deleteKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
		panel.registerKeyboardAction(deleteAction, backspaceKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public SBOLDocument getDesign() {
		return design;
	}

	private static JPopupMenu createPopupMenu(SBOLEditorAction... actions) {
		final JPopupMenu popup = new JPopupMenu();

		for (SBOLEditorAction action : actions) {
			popup.add(action.createMenuItem());
		}

		return popup;
	}

	public boolean canFocusIn() {
		ComponentDefinition comp = getSelectedCD();
		boolean feature = false;
		if(selectedElement != null) {
			feature = selectedElement.isFeature();
			if(feature) {
				feature = feature && isElementCompositeFeature(selectedElement);
			}
		}
		return comp != null || feature;
	}
	
	private boolean isElementCompositeFeature(DesignElement e) {
		boolean iscomposite = false;
		if(!features.isEmpty()) {
			for(Feature f: features) {
				if(f.element.equals(e)) {
					iscomposite = false;
					for(int j = 0; j < features.size(); j++) {
						if(features.get(j).start >= f.start && features.get(j).end <= f.end && !features.get(j).element.equals(e)) {
							if(features.get(j).start > f.start || features.get(j).end < f.end) {
								iscomposite = true;
								break;
							}
						}
					}
					break;
				}
			}
		}
		return iscomposite;
	}

	public void focusIn() throws SBOLValidationException {
		Preconditions.checkState(canFocusIn(), "No selection to focus in");
		
		if(selectedElement.isFeature()) {
			zoomStack.push(1);
			SequenceAnnotation sa = selectedElement.getSeqAnn();
			for(Feature f: features) {
				if(f.element.equals(selectedElement)) {
					featureRange.push(f);
					displayFeatures();
					break;
				}
			}
			setSelectedElement(null);
			System.out.print("");
		}else {
			features.clear();
			featureRange.clear();
			
			zoomStack.push(0);
			ComponentDefinition comp = getSelectedCD();
	
			BufferedImage snapshot = getSnapshot();
	
			//updateCanvasCD();
			parentCDs.push(canvasCD);
	
			load(comp);
	
			eventBus.post(new FocusInEvent(this, comp, snapshot));
		}
		updateEnabledActions();
	}
	
	private void displayFeatures() throws SBOLValidationException {
		int start = featureRange.peek().start;
		int end = featureRange.peek().end;
		setElementVisible(featureRange.peek().element, false);
		features.remove(featureRange.peek());
		ArrayList<Feature> currentlyDisplayedFeatures = new ArrayList<Feature>();
		//ArrayList<DesignElement> currentlyDisplayedElements = new ArrayList<DesignElement>();
		for(int i = 0; i < features.size(); i++) {
			Feature f = features.get(i);
			if(f.start >= start && f.end <= end) {
				setElementVisible(f.element, true);
				currentlyDisplayedFeatures.add(f);
				for(int j = 0; j < features.size(); j++) {
					if(features.get(j).start > start && features.get(j).end < end) {
						if(features.get(j).start <= f.start && features.get(j).end >= f.end && i != j) {
							if(features.get(j).start < f.start || features.get(j).end > f.end) {
								setElementVisible(f.element, false);
								currentlyDisplayedFeatures.remove(f);
								break;
							}
							
						}
					}
				}
			}else {
				setElementVisible(f.element, false);
			}
		}
		for(Feature f : currentlyDisplayedFeatures){
			setElementVisible(f.element, true);
			JLabel button = buttons.get(f.element);
			setupIcons(button, f.element);
		}
		return;
	}
	private void displayFeatures(Feature parent) throws SBOLValidationException {
		features.add(parent);
		ArrayList<Feature> currentlyDisplayedFeatures = new ArrayList<Feature>();
		if(!featureRange.isEmpty()) {
			int start = featureRange.peek().start;
			int end = featureRange.peek().end;
			for(int i = 0; i < features.size(); i++) {
				Feature f = features.get(i);
				if(f.start >= featureRange.peek().start && f.end <= featureRange.peek().end) {
					setElementVisible(f.element, true);
					currentlyDisplayedFeatures.add(f);
					for(int j = 0; j < features.size(); j++) {
						if(features.get(j).start > start && features.get(j).end < end) {
							if(features.get(j).start <= f.start && features.get(j).end >= f.end && i != j) {
								if(features.get(j).start < f.start || features.get(j).end > f.end) {
									setElementVisible(f.element, false);
									currentlyDisplayedFeatures.remove(f);
									break;
								}
							}
						}
					}
				}
			}
		}else {
			for(int i = 0; i < features.size(); i++) {
				Feature f = features.get(i);
				setElementVisible(f.element, true);
				currentlyDisplayedFeatures.add(f);
				for(int j = 0; j < features.size(); j++) {
					if(features.get(j).start <= f.start && features.get(j).end >= f.end && i != j) {
						if(features.get(j).start < f.start || features.get(j).end > f.end) {
							setElementVisible(f.element, false);
							currentlyDisplayedFeatures.remove(f);
							break;
						}
					}
				}
			}
		}
		for(Feature f : currentlyDisplayedFeatures){
			setElementVisible(f.element, true);
			JLabel button = buttons.get(f.element);
			setupIcons(button, f.element);
		}

	} 

	public boolean canFocusOut() {
		boolean zoom = false;
		if(zoomStack != null) {
			if(!zoomStack.empty()) {
				if(zoomStack.peek() == 1) {
					zoom = true;
				}
			}
		}
		return !parentCDs.isEmpty() || zoom;
	}

	public void focusOut() throws SBOLValidationException {
		Preconditions.checkState(canFocusOut(), "No parent design to focus out");

		if(zoomStack != null) {
			if(zoomStack.pop() == 1)
				displayFeatures(featureRange.pop());
			else {
				features.clear();
				featureRange.clear();
				focusOut(getParentCD());
			}
		}else {
			features.clear();
			featureRange.clear();
			focusOut(getParentCD());
		}
		updateEnabledActions();
	}

	public void focusOut(ComponentDefinition comp) throws SBOLValidationException {
		if (canvasCD == comp) {
			return;
		}

		//updateCanvasCD();

		ComponentDefinition parentComponent = parentCDs.pop();
		while (!parentComponent.equals(comp)) {
			parentComponent = parentCDs.pop();
		}

		load(parentComponent);

		eventBus.post(new FocusOutEvent(this, parentComponent));
	}

	/**
	 * Loads the given SBOLDocument. Returns true if the design was successfully
	 * loaded. If rootUri is not null, use that rootUri as the root part.
	 */
	public boolean load(SBOLDocument doc, URI rootUri) throws SBOLValidationException {
		if (doc == null) {
			JOptionPane.showMessageDialog(panel, "No document to load.", "Load error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		SBOLUtils.populateRegistries(doc);
		SBOLValidate.validateSBOL(doc, false, false, false);
		List<String> errors = SBOLValidate.getErrors();
		if (!errors.isEmpty()) {
			MessageDialog.showMessage(panel, "Beware, this file isn't following best practice", errors);
		}
		design = doc;

		ComponentDefinition[] CDs = doc.getComponentDefinitions().toArray(new ComponentDefinition[0]);
		ComponentDefinition rootCD = null;

		switch (CDs.length) {
		case 0:
			// There isn't a rootCD
			rootCD = design.createComponentDefinition("UnnamedPart", "1", ComponentDefinition.DNA);
			rootCD.addRole(SequenceOntology.ENGINEERED_REGION);
			break;
		case 1:
			// There is a single root CD
			rootCD = CDs[0];
			break;
		default:
			// There are multiple root CDs
			if (rootUri == null) {
				ComponentDefinitionBox root = new ComponentDefinitionBox();
				doc = new RootInputDialog(panel, doc, root).getInput();
				rootCD = root.cd;
				if (doc == null || rootCD == null) {
					return false;
				}
			} else {
				rootCD = doc.getComponentDefinition(rootUri);
			}
			doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			design = doc;
			break;
		}

		parentCDs.clear();
		features.clear();
		featureRange.clear();
		zoomStack.clear();
		load(rootCD);

		eventBus.post(new DesignLoadedEvent(this));
		return true;
	}

	private void load(ComponentDefinition newRoot) throws SBOLValidationException {
		loading = true;

		elementBox.removeAll();
		backboneBox.removeAll();
		elements.clear();
		buttons.clear();
		isCircular = false;
		readOnly.clear();

		canvasCD = newRoot;
		populateComponents(canvasCD);

		detectReadOnly();

		selectedElement = null;

		loading = false;

		refreshUI();
		fireSelectionChangedEvent();
	}

	private void detectReadOnly() {
		if (SBOLUtils.notInNamespace(canvasCD)) {
			readOnly.add(ReadOnly.REGISTRY_COMPONENT);
		}

		Map<Integer, Sequence> uncoveredSequences = findUncoveredSequences();
		if (uncoveredSequences == null) {
			readOnly.add(ReadOnly.MISSING_START_END);
		} else if (!uncoveredSequences.isEmpty()) {
			readOnly.add(ReadOnly.UNCOVERED_SEQUENCE);
		}
	}

	private boolean confirmEditable() throws SBOLValidationException {
		if (readOnly.contains(ReadOnly.REGISTRY_COMPONENT)) {
			//readOnlyError();
			return false;
		}

		/*
		 * if (readOnly.contains(ReadOnly.MISSING_START_END)) { int result =
		 * JOptionPane.showConfirmDialog(panel, "The component '" +
		 * canvasCD.getDisplayId() + "' has a DNA sequence but the\n" +
		 * "subcomponents don't have start or end\n" +
		 * "coordinates. If you edit the design you will\n" +
		 * "lose the DNA sequence.\n\n" + "Do you want to continue with editing?",
		 * "Uncovered sequence", JOptionPane.YES_NO_OPTION,
		 * JOptionPane.QUESTION_MESSAGE);
		 * 
		 * if (result == JOptionPane.NO_OPTION) { return false; }
		 * readOnly.remove(ReadOnly.REGISTRY_COMPONENT); } else if
		 * (readOnly.contains(ReadOnly.UNCOVERED_SEQUENCE)) { String msg =
		 * "The sub components do not cover the DNA sequence\n" + "of the component '" +
		 * canvasCD.getDisplayId() + "' completely.\n" +
		 * "You need to add SCAR components to cover the missing\n" +
		 * "parts or you will lose the uncovered DNA sequence.\n\n" +
		 * "How do you want to continue?";
		 * 
		 * JRadioButton[] buttons = { new JRadioButton(
		 * "Add SCAR Parts to handle uncovered sequences"), new JRadioButton(
		 * "Continue with editing and lose the root DNA sequence"), new
		 * JRadioButton("Cancel the operation and do not edit the component") };
		 * 
		 * JTextArea textArea = new JTextArea(msg); textArea.setEditable(false);
		 * textArea.setLineWrap(true); textArea.setOpaque(false);
		 * textArea.setBorder(BorderFactory.createEmptyBorder());
		 * textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * 
		 * Box box = Box.createVerticalBox(); box.add(textArea);
		 * 
		 * ButtonGroup group = new ButtonGroup(); for (JRadioButton button : buttons) {
		 * button.setSelected(true); button.setAlignmentX(Component.LEFT_ALIGNMENT);
		 * group.add(button); box.add(button); }
		 * 
		 * int result = JOptionPane.showConfirmDialog(panel, box, "Uncovered sequence",
		 * JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		 * 
		 * if (result == JOptionPane.CANCEL_OPTION || buttons[2].isSelected()) { return
		 * false; }
		 * 
		 * readOnly.remove(ReadOnly.UNCOVERED_SEQUENCE);
		 * 
		 * if (buttons[0].isSelected()) { addScarsForUncoveredSequences(); } }
		 */

		return true;
	}

	private void readOnlyError() {
		MessageDialog.showMessage(panel, "This part is read only", Arrays.asList(canvasCD.getDisplayId()
				+ " is not owned by you.  Please edit it and/or its parents and choose \"yes\" to creating an editable copy while re-saving it."));
	}

	private Map<Integer, Sequence> findUncoveredSequences() {
		return SBOLUtils.findUncoveredSequences(canvasCD,
				Lists.transform(elements, new Function<DesignElement, SequenceAnnotation>() {
					@Override
					public SequenceAnnotation apply(DesignElement e) {
						return e.getSeqAnn();
					}
				}), design);
	}

	/**
	 * Adds components in the order they appear in the sequence
	 */
	private void populateComponents(ComponentDefinition comp) throws SBOLValidationException {
		// Check if the design is completely annotated, this is true if all
		// Components have a precise location specified by a SequenceAnnotation
		// with a Range or Cut Location.
		boolean completelyAnnotated = true;
		for (org.sbolstandard.core2.Component component : comp.getComponents()) {
			SequenceAnnotation sa = comp.getSequenceAnnotation(component);
			if (sa == null) {
				completelyAnnotated = false;
				break;
			}
			boolean preciseLocation = false;
			for (Location location : sa.getLocations()) {
				if (location instanceof Range) {
					preciseLocation = true;
					break;
				} else if (location instanceof Cut) {
					preciseLocation = true;
					break;
				}
			}
			if (!preciseLocation) {
				completelyAnnotated = false;
				break;
			}
		}

		// If completely annotated, then sort by SequenceAnnotations
		// SequenceConstraints can be neglected
		if (completelyAnnotated) {
			Iterable<SequenceAnnotation> sortedSAs = comp.getSortedSequenceAnnotations();
			for (SequenceAnnotation sequenceAnnotation : sortedSAs) {
				if (sequenceAnnotation.isSetComponent()) {
					org.sbolstandard.core2.Component component = sequenceAnnotation.getComponent();
					ComponentDefinition refered = component.getDefinition();
					if (refered == null) {
						// component reference without a connected CD
						continue;
					}

					if (component.getRoles().isEmpty()) {
						addCD(component, refered, Parts.forIdentified(refered));
					} else {
						// If component has roles, then these should be used
						addCD(component, refered, Parts.forIdentified(component));
					}
				} else {
					addSA(sequenceAnnotation, Parts.forIdentified(sequenceAnnotation));
				}
			}
			if(!features.isEmpty()) {
				ArrayList<Feature> currentlyDisplayedFeatures = new ArrayList<Feature>();
				for(int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					currentlyDisplayedFeatures.add(f);
					for(int j = 0; j < features.size(); j++) {
						if(features.get(j).start <= f.start && features.get(j).end >= f.end && i != j) {
							if(features.get(j).start < f.start || features.get(j).end > f.end) {
								setElementVisible(f.element, false);
								currentlyDisplayedFeatures.remove(f);
								break;
							}
						}
					}
				}
				for(Feature f : currentlyDisplayedFeatures){
					setElementVisible(f.element, true);
					JLabel button = buttons.get(f.element);
					setupIcons(button, f.element);
				}
			}
			return;
		}

		// If not completely annotated, need to sort by Components
		// get sortedComponents and add them in order
		Iterable<org.sbolstandard.core2.Component> sortedComponents = comp.getSortedComponents();
		for (org.sbolstandard.core2.Component component : sortedComponents) {
			ComponentDefinition refered = component.getDefinition();
			if (refered == null) {
				// component reference without a connected CD
				continue;
			}

			if (component.getRoles().isEmpty()) {
				addCD(component, refered, Parts.forIdentified(refered));
			} else {
				// If component has roles, then these should be used
				addCD(component, refered, Parts.forIdentified(component));
			}
		}
	}

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
			if (e.getCD() == comp) {
				return i;
			}
		}
		return -1;
	}

	public ComponentDefinition getRootCD() {
		return parentCDs.isEmpty() ? canvasCD : parentCDs.getFirst();
	}

	public ComponentDefinition getCanvasCD() {
		return canvasCD;
	}

	public ComponentDefinition getParentCD() {
		return parentCDs.peek();
	}

	public ComponentDefinition getSelectedCD() {
		return selectedElement == null ? null : selectedElement.getCD();
	}

	public boolean setSelectedCD(ComponentDefinition comp) {
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

	public void addCD(ComponentDefinition comp) throws SBOLValidationException {
		addCD(null, comp, Parts.forIdentified(comp));
	}

	/**
	 * edit is whether or not you want to bring up PartEditDialog when part button
	 * is pressed.
	 * @throws URISyntaxException 
	 */
	public ComponentDefinition addCD(Part part, boolean edit) throws SBOLValidationException, URISyntaxException {
		ComponentDefinition comp = part.createComponentDefinition(design);
		boolean autoUpdate = false; 
		if (!confirmEditable()) {
			int result = JOptionPane.showConfirmDialog(null,
					"The part '" + getCanvasCD().getDisplayId() + "' is not owned by you \n" + "and cannot be edited.\n\n"
							+ "Do you want to create an editable copy of\n" + "this part and save your changes?",
					"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.NO_OPTION) {
				return null;
			}else {
				autoUpdate = true;
			}
		}
		
		if(!features.isEmpty()) {
			return null;
		}

		if (edit || part.getDisplayId() == "NGA") {
			comp = PartEditDialog.editPart(panel.getParent(), getCanvasCD(), comp, edit, true, design, false);
			if (comp == null) {
				return null;
			}
		}
		
		if(autoUpdate)
		{
			parentCDs.push(canvasCD);
			autoUpdateComponentReferences(panel.getParent(), getCanvasCD(), comp, false);
			parentCDs.pop();
		}
		
		part = Parts.forIdentified(comp);
		addCD(null, comp, part);

		return comp;
	}

	/**
	 * Adds the part to elements. Takes in a component if one already exists, the
	 * CD, and the part.
	 * 
	 * @throws SBOLValidationException
	 */
	private void addCD(org.sbolstandard.core2.Component component, ComponentDefinition comp, Part part)
			throws SBOLValidationException {
		boolean backbone = (part == Parts.CIRCULAR);
		DesignElement e = new DesignElement(component, canvasCD, comp, part, design);
//		if(comp.getDisplayId() == "NGA") {
//			try {
//				editSelectedCD();
//			} catch (SBOLValidationException err) {
//				MessageDialog.showMessage(panel, "There was a problem editing: ", err.getMessage());
//				err.printStackTrace();
//			}
//		}
		JLabel button = createComponentButton(e);

		if (backbone) {
			if (isCircular) {
				throw new IllegalArgumentException("Cannot add multiple backbone parts");
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
			fireDesignChangedEvent(true);
		}else {
			updateCanvasCD(true);
		}
	}
	
	static class Feature{
		public ArrayList<Feature> children;
		DesignElement element;
		public int start;
		public int end;
		public int size; 
		
		public Feature(int start, int end, DesignElement element) {
			this.start = start;
			this.end = end;
			this.size = end-start;
			this.element = element;
			children = new ArrayList<Feature>();
		}
		
		public void addChild(Feature child) {
			this.children.add(child);
		}
		
	}

	private void addSA(SequenceAnnotation sequenceAnnotation, Part part) throws SBOLValidationException {
		DesignElement e = new DesignElement(sequenceAnnotation, canvasCD, part, design);
		JLabel button = createComponentButton(e);

		elements.add(e);
		elementBox.add(button);
		buttons.put(e, button);

		if (!isPartVisible(part)) {
			setPartVisible(part, true);
		}
		
		int start = -1;
		int end = -1;
		for (Location location : sequenceAnnotation.getLocations()) {
			if (location instanceof Range) {
				Range range = (Range) location;
				if(start == -1 || range.getStart() < start) {
					start = range.getStart(); 
				}
				if(end  == -1 || range.getEnd() > end) {
					end = range.getEnd(); 
				}
			}
		} 
		Feature f = new Feature(start, end, e);
		features.add(f);
		if (!loading) {
			fireDesignChangedEvent(true);
		}
	}

	public void moveElement(int source, int target) throws SBOLValidationException, URISyntaxException {
	
		DesignElement element = elements.remove(source);
		elements.add(element);

		JLabel button = buttons.get(element);
		elementBox.remove(button);
		elementBox.add(button, target);

		fireDesignChangedEvent(true);
	}

	private void setupIcons(final JLabel button, final DesignElement e) throws SBOLValidationException {
		final ComponentDefinition comp = e.getCD();
		boolean hasSequence = getAllSequences(comp);
		updateCanvasCD(false);
		boolean composite = e.isComposite();
		if(e.isFeature()) {
			composite = isElementCompositeFeature(e);
		}
		Image image = e.getPart().getImage(e.getOrientation(), composite, e.hasVariants(design, canvasCD),
				hasSequence);
		Image selectedImage = Images.createBorderedImage(image, Color.LIGHT_GRAY);
		button.setIcon(new ImageIcon(image));
		button.setDisabledIcon(new ImageIcon(selectedImage));
	}
	
	private boolean getAllSequences(final ComponentDefinition comp) {
		if (comp==null) return true;
		Set<org.sbolstandard.core2.Component> comps;
		comps = comp.getComponents();
		Iterator<org.sbolstandard.core2.Component> it = comps.iterator();
		if(comps.size() == 0) {
			if(comp.getSequences().size() > 0) {
				return true;
			}else {
				return false;
			}
		}else {
			while(it.hasNext()) {
				if(!getAllSequences(it.next().getDefinition())) {
					return false;
				}
			}
		}
		return true;
	}

	private String getButtonText(final DesignElement e) {
		Identified i = e.getCD() != null ? e.getCD() : e.getSeqAnn();

		int prefs = SBOLEditorPreferences.INSTANCE.getNameDisplayIdBehavior();

		if (prefs == 0 && i.isSetName() && i.getName().length() != 0) {
			return i.getName();
		} else {
			return i.getDisplayId();
		}
	}

	private JLabel createComponentButton(final DesignElement e) throws SBOLValidationException {
		final JLabel button = new JLabel();
		setupIcons(button, e);
		button.setVerticalAlignment(JLabel.TOP);
		button.setVerticalTextPosition(JLabel.TOP);
		button.setIconTextGap(2);
		button.setText(getButtonText(e));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setToolTipText(getTooltipText(e));
		button.setMaximumSize(new Dimension(IMG_WIDTH + 1, IMG_HEIGHT + 20));
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
					try {
						editSelectedCD();
					} catch (SBOLValidationException | URISyntaxException e) {
						MessageDialog.showMessage(panel, "There was a problem editing: ", e.getMessage());
						e.printStackTrace();
					}
				}
			}
		});
		// button.setComponentPopupMenu(popupMenu);

		boolean isDraggable = (e.getPart() != Parts.CIRCULAR);
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
				boolean completed = true;
				if (index >= 0) {
					Point loc = event.getLocation();
					if (loc.getX() > button.getWidth() * 0.75 && index < elements.size() - 1) {
						index++;
					}
					try {
						completed = moveSelectedElement(index);
					} catch (SBOLValidationException | URISyntaxException e) {
						MessageDialog.showMessage(panel, "There was an error deleting the part: ", e.getMessage());
						e.printStackTrace();
					}
				}
				event.dropComplete(completed);
			}
		});
	}

	private String getTooltipText(DesignElement e) {
		SequenceOntology so = new SequenceOntology();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		final ComponentDefinition comp = e.getCD();
		SequenceAnnotation sa = e.getSeqAnn();
		if (comp != null) {
			sb.append("<b>Component</b><br>");
			sb.append("<b>Display ID:</b> ").append(comp.getDisplayId()).append("<br>");
			sb.append("<b>Name:</b> ").append(Strings.nullToEmpty(comp.getName())).append("<br>");
			sb.append("<b>Description:</b> ").append(Strings.nullToEmpty(comp.getDescription())).append("<br>");
			for (URI role : comp.getRoles()) {
				String roleStr = so.getName(role);
				if (roleStr != null)
					sb.append("<b>Role:</b> ").append(roleStr).append("<br>");
			}
			/*
			 * if (e.getOrientation() != null) { sb.append( "<b>Orientation:</b> "
			 * ).append(e.getOrientation()).append("<br>"); }
			 */
			// Not sure sequence very useful on tooltip - CJM
			/*
			 * if (!comp.getSequences().isEmpty() &&
			 * comp.getSequences().iterator().next().getElements() != null) { // String
			 * sequence = comp.getSequence().getNucleotides(); String sequence =
			 * comp.getSequences().iterator().next().getElements();
			 * sb.append("<b>Sequence Length:</b> "
			 * ).append(sequence.length()).append("<br>"); sb.append(
			 * "<b>Sequence:</b> ").append(CharSequenceUtil.shorten(sequence, 25));
			 * sb.append("<br>"); }
			 */
			if (sa != null) {
				sb = appendOrientation(sa, sb);
			}
			if (e.isComposite()) {
				sb.append("<b>Composite</b><br>");
			}
			boolean comb = false;
			try {
				if(e.hasVariants(design, canvasCD)) {
					sb.append("<b>Combinatorial</b><br>");
					comb = true;
				}
			} catch (SBOLValidationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(!comb) {
				if (!getAllSequences(comp) || comp.getSequenceByEncoding(Sequence.IUPAC_DNA) == null
						|| comp.getSequenceByEncoding(Sequence.IUPAC_DNA).getElements().equals("")) {
					sb.append("<b>Sequence incomplete</b><br>");
				}
			}
		} else {
			sb.append("<b>Feature</b><br>");
			sb.append("<b>Display ID:</b> ").append(sa.getDisplayId()).append("<br>");
			sb.append("<b>Name:</b> ").append(Strings.nullToEmpty(sa.getName())).append("<br>");
			sb.append("<b>Description:</b> ").append(Strings.nullToEmpty(sa.getDescription())).append("<br>");
			for (URI role : sa.getRoles()) {
				String roleStr = so.getName(role);
				if (roleStr != null)
					sb.append("<b>Role:</b> ").append(roleStr).append("<br>");
			}
			if (sa != null) {
				sb = appendOrientation(sa, sb);
			}
		}
		sb.append("</html>");
		return sb.toString();
	}
	
	private StringBuilder appendOrientation(SequenceAnnotation sa, StringBuilder input) {
		StringBuilder sb = input;
		for (Location location : sa.getLocations()) {
			if (location instanceof Range) {
				Range range = (Range) location;
				if (range.isSetOrientation()) {
					sb.append("<b>Orientation:</b> ").append(range.getOrientation().toString()).append("<br>");
				}
				sb.append(range.getStart() + ".." + range.getEnd() + "<br>");
			} else if (location instanceof Cut) {
				Cut cut = (Cut) location;
				if (cut.isSetOrientation()) {
					sb.append("<b>Orientation:</b> ").append(cut.getOrientation().toString()).append("<br>");
				}
				sb.append(cut.getAt() + "^" + cut.getAt() + "<br>");
			} else {
				if (location.isSetOrientation()) {
					sb.append("<b>Orientation:</b> ").append(location.getOrientation().toString()).append("<br>");
				}
			}
		}
		return sb;
	}

	private boolean moveSelectedElement(int index) throws SBOLValidationException, URISyntaxException {
		if (SBOLUtils.notInNamespace(canvasCD)) {
			editCanvasCD();
			return false;
		}

		if (selectedElement != null) {
			int selectedIndex = elements.indexOf(selectedElement);
			if (selectedIndex >= 0 && selectedIndex != index) {
				elements.remove(selectedIndex);
				elements.add(index, selectedElement);

				int indexAdjustment = isCircular ? -1 : 0;
				JLabel button = buttons.get(selectedElement);
				elementBox.remove(selectedIndex + indexAdjustment);
				elementBox.add(button, index + indexAdjustment);

				fireDesignChangedEvent(true);
			}
		}
		return true;
	} 

	public void flipOrientation(ComponentDefinition comp) throws SBOLValidationException, URISyntaxException {
		int index = getElementIndex(comp);
		boolean designChanged = false;
		if (!confirmEditable()) {
			int result = JOptionPane.showConfirmDialog(null,
					"The part '" + getCanvasCD().getDisplayId() + "' is not owned by you \n" + "and cannot be edited.\n\n"
							+ "Do you want to create an editable copy of\n" + "this part and save your changes?",
					"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			parentCDs.push(canvasCD);
			updateComponentReferences(null, null, null);
			load(getParentCD());
			fireDesignChangedEvent(false);
			designChanged = true;
		}
		
		DesignElement e;
		if(designChanged) {
			e = elements.get(index); 
		}else {
			e = getElement(comp);
		}
		e.flipOrientation();

		JLabel button = buttons.get(e);
		setupIcons(button, e);
		button.setToolTipText(getTooltipText(e));

		fireDesignChangedEvent(true);
	}

	public void deleteCD(ComponentDefinition component) throws SBOLValidationException, URISyntaxException {
		int index = getElementIndex(component);
		boolean designChanged = false;
		if (!confirmEditable()) {
			int result = JOptionPane.showConfirmDialog(null,
					"The part '" + getCanvasCD().getDisplayId() + "' is not owned by you \n" + "and cannot be edited.\n\n"
							+ "Do you want to create an editable copy of\n" + "this part and save your changes?",
					"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.NO_OPTION) {
				return;
			}
			parentCDs.push(canvasCD);
			updateComponentReferences(null, null, null);
			load(getParentCD());
			fireDesignChangedEvent(false);
			designChanged = true;
		}
		if (!features.isEmpty()) {
			return;
		}

		if (index >= 0) {
			DesignElement e = elements.get(index);

			if (e == selectedElement || designChanged) {
				setSelectedElement(null);
				deleteCombinatorialDesign(canvasCD, e.component);
				design.removeComponentDefinition(e.component.getDefinition());
				canvasCD.removeSequenceAnnotation(e.seqAnn);
				canvasCD.clearSequenceConstraints();
				canvasCD.removeComponent(e.component);
			}

			JLabel button = buttons.remove(e);
			elements.remove(index);
			if (isCircular && index == 0) {
				backboneBox.remove(button);
				isCircular = false;
			} else {
				elementBox.remove(button);
			}
			fireDesignChangedEvent(true);
		}
	}

	private void deleteCombinatorialDesign(ComponentDefinition cd, org.sbolstandard.core2.Component component)
			throws SBOLValidationException {
		CombinatorialDerivation derivationToRemoveFrom = null;
		VariableComponent variableToBeRemoved = null;

		for (CombinatorialDerivation derivation : design.getCombinatorialDerivations()) {
			for (VariableComponent variable : derivation.getVariableComponents()) {
				if (variable.getVariable().equals(component)) {
					derivationToRemoveFrom = derivation;
					variableToBeRemoved = variable;
					break;
				}
			}

			if (derivationToRemoveFrom != null && variableToBeRemoved != null) {
				break;
			}
		}

		if (derivationToRemoveFrom == null || variableToBeRemoved == null) {
			return;
		}

		derivationToRemoveFrom.removeVariableComponent(variableToBeRemoved);

		if (derivationToRemoveFrom.getVariableComponents().isEmpty()) {
			design.removeCombinatorialDerivation(derivationToRemoveFrom);
		}
	}

	private void replaceCD(ComponentDefinition oldCD, ComponentDefinition newCD) throws SBOLValidationException {
		int index = getElementIndex(oldCD);
		if (index == -1) { // sometimes oldCD gets replaced by newCD
			index = getElementIndex(newCD);
		}

		if (index >= 0) {
			DesignElement e = elements.get(index);
			JLabel button = buttons.get(e);
			e.setCD(newCD);
			if (!newCD.getRoles().contains(e.getPart().getRole())) {
				Part newPart = Parts.forIdentified(newCD);
				if (newPart == null) {
					newCD.addRole(e.getPart().getRole());
				} else {
					e.setPart(newPart);
				}
			}
			setupIcons(button, e);
			button.setText(getButtonText(e));
			button.setToolTipText(getTooltipText(e));

			fireDesignChangedEvent(true);
		}
	}

	private void refreshUI() {
		panel.revalidate();
		panel.repaint();
	}

	private void fireDesignChangedEvent(boolean updateSequence) {
		updateCanvasCD(updateSequence);
		refreshUI();
		eventBus.post(new DesignChangedEvent(this));
	}

	private void fireSelectionChangedEvent() {
		updateEnabledActions();
		eventBus.post(new SelectionChangedEvent(getSelectedCD()));
	}

	private void updateEnabledActions() {
		boolean isEnabled = (selectedElement != null);
		FIND.setEnabled(isEnabled);
		EDIT.setEnabled(isEnabled);
		VARIANTS.setEnabled(isEnabled);
		DELETE.setEnabled(isEnabled);
		FLIP.setEnabled(isEnabled);
		FOCUS_IN.setEnabled(canFocusIn());
		FOCUS_OUT.setEnabled(canFocusOut());
	}

	public boolean isPartVisible(Part part) {
		return !hiddenParts.contains(part);
	}
	
	public void setElementVisible(DesignElement e, boolean isVisible) {
		JLabel button = buttons.get(e);
		// TODO: new check
		if (button != null) {
			button.setVisible(isVisible);
		}
		refreshUI();
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

			eventBus.post(new PartVisibilityChangedEvent(part, isVisible));

			if (selectedElement != null && part.equals(selectedElement.getPart())) {
				setSelectedElement(null);
			}
		}
	}

	public void addScars() throws SBOLValidationException, URISyntaxException {
		if (!confirmEditable()) {
			int result = JOptionPane.showConfirmDialog(null,
					"The part '" + getCanvasCD().getDisplayId() + "' is not owned by you \n" + "and cannot be edited.\n\n"
							+ "Do you want to create an editable copy of\n" + "this part and save your changes?",
					"Edit registry part", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.NO_OPTION) {
				return;
			}else {
				ComponentDefinition comp = getCanvasCD();
				URI originalIdentity = comp.getIdentity();
				comp = (ComponentDefinition) design.createCopy(comp,
						SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString(), comp.getDisplayId(),
						comp.getVersion());
				if (comp != null) {
					if (!originalIdentity.equals(comp.getIdentity())) {
						try {
							updateComponentReferences(originalIdentity, comp.getIdentity(), null);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					load(comp);
					fireDesignChangedEvent(false);
				}
			}
		}
		int size = elements.size();
		int start = isCircular ? 1 : 0;
		int end = size - 1;
		DesignElement curr = (size == 0) ? null : elements.get(start);
		for (int i = start; i < end; i++) {
			DesignElement next = elements.get(i + 1);

			if (curr.getPart() != Parts.SCAR && next.getPart() != Parts.SCAR) {
				DesignElement scar = new DesignElement(null, canvasCD, Parts.SCAR.createComponentDefinition(design),
						Parts.SCAR, design);
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
			fireDesignChangedEvent(true);
		}

		setPartVisible(Parts.SCAR, true);
	}

	public void editCanvasCD() throws SBOLValidationException {
		confirmEditable();
		if(!features.isEmpty()) {
			PartEditDialog.editPart(panel.getParent(), parentCDs.peekFirst(), getCanvasCD(), false, false, design, false);
		}else {
			ComponentDefinition comp = getCanvasCD();
			URI originalIdentity = comp.getIdentity();
			comp = PartEditDialog.editPart(panel.getParent(), parentCDs.peekFirst(), comp, false, true, design, false);
			if (comp != null) {
				if (!originalIdentity.equals(comp.getIdentity())) {
					try {
						updateComponentReferences(originalIdentity, comp.getIdentity(), null);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				load(comp);
				fireDesignChangedEvent(false);
			}
		}
	}

	/**
	 * Looks through all the components and updates all references from
	 * originalIdentity to identity
	 * @throws URISyntaxException 
	 */
	private void updateComponentReferences(URI originalIdentity, URI newIdentity, ComponentDefinition original) throws SBOLValidationException, URISyntaxException {
		ComponentDefinition CD = getParentCD();
		if(CD == null) {
			return;
		}
		ComponentDefinition newCD = CD;
		if(SBOLUtils.notInNamespace(CD)) {
			newCD = (ComponentDefinition)design.createCopy(CD, SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString(),
					CD.getDisplayId(), CD.getVersion());
		}
		for (org.sbolstandard.core2.Component comp : newCD.getComponents()) {
			if (comp.getDefinitionIdentity().equals(originalIdentity)) {
				comp.setDefinition(newIdentity);
			}
		}
		if(SBOLUtils.notInNamespace(CD)) {
			parentCDs.pop();
			updateComponentReferences(CD.getIdentity(), newCD.getIdentity(), null);
			parentCDs.push(newCD);
		}
		
	}
	
	/**
	 * This Is the method that should be called to auto update the component references for the user when editing a part that is not owned. 
	 * @param originalIdentity
	 * @param newIdentity
	 * @throws SBOLValidationException
	 * @throws URISyntaxException
	 */
	private URI autoUpdateComponentReferences(Component parent, ComponentDefinition parentCD, 
			ComponentDefinition CD, boolean openEditor) throws SBOLValidationException, URISyntaxException{
		URI originalIdentity = CD.getIdentity();
		URI newIdentity;
		if (openEditor) {
			newIdentity = PartEditDialog.editPart(parent, parentCD, CD, false, true, design, false).getIdentity();
		}else {
			ComponentDefinition cd = (ComponentDefinition) design.createCopy(CD,
					SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString(), CD.getDisplayId(),
					CD.getVersion());
			newIdentity = cd.getIdentity();
		}
		if (CD != null) {
			try {
				updateComponentReferences(originalIdentity, newIdentity, CD);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ComponentDefinition newParentCD = getParentCD();
			load(newParentCD);
			fireDesignChangedEvent(false);
			return newIdentity;
		}
		return null;
	}

	public void editSelectedCD() throws SBOLValidationException, URISyntaxException {
		if(selectedElement.isFeature()) {
			PartEditDialog.editPart(panel.getParent(), parentCDs.peekFirst(), getCanvasCD(), false, false, design, false);
		}else {
			focusIn();
			editCanvasCD();
			focusOut();
		}
	}

	public void findPartForSelectedCD() throws Exception {
		Part part = selectedElement.getPart();
		ComponentDefinition selectedCD = selectedElement.getCD();
		URI role = selectedCD.getRoles().iterator().next();
		Types type = SBOLUtils
				.convertURIsToType(new HashSet<URI>(Arrays.asList(selectedCD.getTypes().iterator().next())));

		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument selection = new RegistryInputDialog(panel.getParent(), root, part, type, role).getInput();

		if (selection != null) {
			SBOLUtils.insertTopLevels(selection, design);
			if (!confirmEditable()) {
				return;
			}
			replaceCD(selectedElement.getCD(), root.cd);
		}
	}

	private void editVariants() throws SBOLValidationException {
		int index = getElementIndex(getSelectedCD());
		new VariantEditor(panel, getCanvasCD(), getSelectedCD(), design);
		DesignElement e = elements.get(index);
		JLabel button = buttons.get(e);
		setupIcons(button, e);
	}

	private void expandCombinatorial() throws SBOLValidationException, SBOLConversionException, FileNotFoundException {
		ComponentDefinitionBox root = new ComponentDefinitionBox();
		SBOLDocument doc = createDocument(root);

		if (SBOLUtils.rootCalledUnamedPart(root.cd, panel)) {
			editCanvasCD();
			doc = createDocument(root);
		}

		File file = SBOLUtils.selectFile(getPanel(), SBOLUtils.setupFC());
		if (file == null) {
			return;
		}
		if (file.exists()) {
			JOptionPane.showMessageDialog(panel, "This file already exists.");
			return;
		}

		doc = CombinatorialExpansionUtil.createCombinatorialDesign((java.awt.Component)panel, doc);
		
		if (doc != null) {
			for(ComponentDefinition c : doc.getRootComponentDefinitions()) {
				rebuildSequences(c, doc);
			}
			if (!file.getName().contains(".")) {
				file = new File(file + ".xml");
			}
			SBOLWriter.write(doc, new FileOutputStream(file));
		}
	}
	
	private void rebuildSequences(ComponentDefinition comp, SBOLDocument doc) throws SBOLValidationException {
		Set<SequenceAnnotation> oldSequenceAnn = comp.getSequenceAnnotations();
		comp.clearSequenceAnnotations();
		Set<Sequence> currSequences = new HashSet<Sequence>();
		int start = 1;
		int length;
		int count = 0;
		String newSeq = "";
		ComponentDefinition curr;
		for(org.sbolstandard.core2.Component c : comp.getSortedComponents()) {
			curr = c.getDefinition();
			if(!curr.getComponents().isEmpty()) {
				rebuildSequences(curr, doc);
			}
			length = 0;
			//Append sequences to build newly constructed sequence
			for(Sequence s : curr.getSequences()) {
				currSequences.add(s);
				newSeq = newSeq.concat(s.getElements());
				length += s.getElements().length();
			}
			
			OrientationType o = OrientationType.INLINE;
			for(SequenceAnnotation sa : oldSequenceAnn) {
				if(sa.getComponent().getIdentity() == c.getIdentity()) {
					o = sa.getLocations().iterator().next().getOrientation();
				}
			}
			
			SequenceAnnotation seqAnn = comp.createSequenceAnnotation("SequenceAnnotation_"+count, "Range" , start, start+length, o);
			
			seqAnn.setComponent(c.getIdentity());
			
			start += length+1;
			count++;
		}
		if(newSeq != "") {
			if(comp.getSequences().isEmpty())
			{
				String uniqueId = SBOLUtils.getUniqueDisplayId(null, null,
						comp.getDisplayId() + "Sequence", comp.getVersion(), "Sequence", doc);
				comp.addSequence(doc.createSequence(uniqueId, comp.getVersion(), newSeq, Sequence.IUPAC_DNA));
			}else
			{
				comp.getSequences().iterator().next().setElements(newSeq);	
			}
		}
		
	}

	public static void uploadDesign(Component panel, SBOLDocument uploadDoc, File uploadFile)
			throws SynBioHubException, SBOLValidationException, URIException {
		// create a list of registries
		ArrayList<Registry> registryList = new ArrayList<Registry>();
		for (Registry r : Registries.get()) {
			if (!r.isPath()) {
				registryList.add(r);
			}
		}
		Object[] registryOptions = registryList.toArray();
		if (registryOptions.length == 0) {
			JOptionPane.showMessageDialog(panel, "There are no instances of SynBioHub in the registries list.");
			return;
		}

		// ask user to select a registry
		Registry registry = (Registry) JOptionPane.showInputDialog(panel,
				"Please select the SynBioHub instance you want to upload the current design to.", "Upload",
				JOptionPane.QUESTION_MESSAGE, null, registryOptions, registryOptions[0]);
		if (registry == null) {
			return;
		}

		// potentially login to this registry
		SynBioHubFrontends frontends = new SynBioHubFrontends();
		if (!frontends.hasFrontend(registry.getLocation())) {
			JOptionPane.showMessageDialog(panel, "You are not logged in to " + registry + ". Please log in.");
			RegistryLoginDialog loginDialog = new RegistryLoginDialog(panel, registry.getLocation(),
					registry.getUriPrefix());
			SynBioHubFrontend frontend = loginDialog.getSynBioHubFrontend();
			if (frontend == null) {
				return;
			}
			frontends.addFrontend(registry.getLocation(), frontend);
		}

		// upload to new collection or existing collection
		String[] uploadOptions = { "New Collection", "Existing Collection" };
		int uploadChoice = JOptionPane.showOptionDialog(panel,
				"Upload design to new collection or existing collection of " + registry + " ?", "Upload Design",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, uploadOptions, uploadOptions[0]);
		switch (uploadChoice) {
		case JOptionPane.CLOSED_OPTION:
			return;
		case 0:
			if (uploadDoc != null) {
				new UploadNewDialog(panel.getParent(), registry, uploadDoc);
			} else {
				new UploadNewDialog(panel.getParent(), registry, uploadFile);
			}
			return;
		case 1:
			if (uploadDoc != null) {
				new UploadExistingDialog(panel.getParent(), registry, uploadDoc);
			} else {
				new UploadExistingDialog(panel.getParent(), registry, uploadFile);
			}
			return;
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

	/**
	 * Creates a document based off of the root CD. The rootComp will be put in
	 * root.
	 */
	public SBOLDocument createDocument(ComponentDefinitionBox root) throws SBOLValidationException {
		ComponentDefinition rootComp = parentCDs.isEmpty() ? canvasCD : parentCDs.getLast();
		// updatecanvasCD on every level of the tree
		while (canvasCD != rootComp) {
			focusOut(parentCDs.getFirst());
			updateCanvasCD(false);
		}
		focusOut(rootComp);
		updateCanvasCD(false);

		SBOLDocument doc = new SBOLDocument();
		doc = design.createRecursiveCopy(rootComp);
		SBOLUtils.copyReferencedCombinatorialDerivations(doc, design);

		rootComp = doc.getComponentDefinition(rootComp.getIdentity());
		if (root != null) {
			root.cd = rootComp;
		}
		doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());

		ProvenanceUtil.createProvenance(doc, rootComp);

		return doc;
	}

	/**
	 * Updates the canvasCD's Sequences, SequenceConstraints, and
	 * SequenceAnnotations.
	 */
	private void updateCanvasCD(boolean updateSequence) {
		// should not allow updating of CDs outside our namespace
		if (SBOLUtils.notInNamespace(canvasCD)) {
			return;
		}

		try {
			// check circular
			if (isCircular) {
				canvasCD.addType(SequenceOntology.CIRCULAR);
			} else {
				canvasCD.removeType(SequenceOntology.CIRCULAR);
			}

			updateSequenceAnnotations();
			updateSequenceConstraints();

			if ((canvasCD.getComponents().isEmpty() && !canvasCD.getSequenceAnnotations().isEmpty())||!updateSequence) {
				return;
			}

			Sequence oldSeq = canvasCD.getSequenceByEncoding(Sequence.IUPAC_DNA);
			String oldElements = oldSeq == null ? "" : oldSeq.getElements();
			// remove all current Sequences
			for (Sequence s : canvasCD.getSequences()) {
				canvasCD.removeSequence(s.getIdentity());
				design.removeSequence(s);
			}
			String nucleotides = canvasCD.getImpliedNucleicAcidSequence();

			if(nucleotides != null)
				nucleotides = nucleotides.replace("N", "");
			if(oldElements != null)
				oldElements = oldElements.replace("N", "");
			if (nucleotides != null && nucleotides.length() > 0) {
				if (!nucleotides.equals(oldElements)) {
					// report to the user if the updated sequence is shorter
					int option = 0;
					// check preferences
					// askUser is 0, overwrite is 1, and keep is 2
					int seqBehavior = SBOLEditorPreferences.INSTANCE.getSeqBehavior();
					switch (seqBehavior) {
					case 0:
						// askUser
						Object[] options = { "Keep", "Overwrite" };
						do {
							option = JOptionPane.showOptionDialog(panel, "The implied sequence for "
									+ canvasCD.getDisplayId()
									+ " is shorter than the original sequence.  Would you like to overwrite or keep the original sequence? \n(The default behavior can be changed in settings)",
									"Implied sequece", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
									options, options[0]);
						} while (option == JOptionPane.CLOSED_OPTION);
						break;
					case 1:
						// overwrite
						option = 1;
						break;
					case 2:
						// keep
						option = 0;
						break;
					}

					if (option == 0) {
						// use the old sequence provided it was there
						if (oldSeq != null) {
							String uniqueId = SBOLUtils.getUniqueDisplayId(null, null,
									canvasCD.getDisplayId() + "Sequence", canvasCD.getVersion(), "Sequence", design);
							oldSeq = design.createSequence(uniqueId, canvasCD.getVersion(), oldSeq.getElements(),
									Sequence.IUPAC_DNA);
							canvasCD.addSequence(oldSeq);
						}
						return;
					}
				}
				// use the implied sequence
				String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, canvasCD.getDisplayId() + "Sequence", "1",
						"Sequence", design);
				Sequence newSequence = design.createSequence(uniqueId, "1", nucleotides, Sequence.IUPAC_DNA);
				canvasCD.addSequence(newSequence);
			}
			// TODO: removed, not sure what this is for and it is preventing a sequence from being deleted
			else if (nucleotides==null){
				// use the old sequence provided it was there
				if (oldSeq != null) {
					// only recreate it if it isn't in design
					if (!design.getSequences().contains(oldSeq)) {
						String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, canvasCD.getDisplayId() + "Sequence",
								canvasCD.getVersion(), "Sequence", design);
						oldSeq = design.createSequence(uniqueId, canvasCD.getVersion(), oldSeq.getElements(),
								Sequence.IUPAC_DNA);
					}
					canvasCD.addSequence(oldSeq);
				}
			} 
			
			LOGGER.debug("Updated root:\n{}", canvasCD.toString());
		} catch (SBOLValidationException e) {
			MessageDialog.showMessage(null, "Error in updating root component: ", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Updates all the seqAnns of the DesignElements in elements
	 */
	private void updateSequenceAnnotations() throws SBOLValidationException {
		int position = 1;
		for (DesignElement e : elements) {
			if (e.getCD() == null)
				continue;
			Location loc = e.seqAnn.getLocations().iterator().next();

			// We no longer need this seqAnn
			canvasCD.removeSequenceAnnotation(e.seqAnn);

			e.seqAnn = DesignElement.createSeqAnn(canvasCD, design);

			// if a sequence exists, give seqAnn a Range
			Sequence seq = e.getCD().getSequenceByEncoding(Sequence.IUPAC_DNA);
			if (seq != null) {
				String uniqueId = SBOLUtils.getUniqueDisplayId(canvasCD, null, e.seqAnn.getDisplayId() + "_Range", null,
						"Range", design);
				int start = position;
				int end = seq.getElements().length() + start - 1;
				position = end + 1;
				Range range = e.seqAnn.addRange(uniqueId, start, end, OrientationType.INLINE);
				// remove all other locations
				for (Location toBeRemoved : e.seqAnn.getLocations()) {
					if (!toBeRemoved.equals(range)) {
						e.seqAnn.removeLocation(toBeRemoved);
					}
				}
			}
			// maintain the orientation
			if (loc.getOrientation() == OrientationType.REVERSECOMPLEMENT) {
				e.flipOrientation();
			}

			e.seqAnn.setComponent(e.component.getIdentity());
			JLabel button = buttons.get(e);
			button.setToolTipText(getTooltipText(e));
		}
	}

	/**
	 * Generates canvasCD's SequenceConstraints based on ordering in elements.
	 */
	private void updateSequenceConstraints() throws SBOLValidationException {
		// only makes sense to have SCs if there are 2 or more components
		if (elements.size() < 2) {
			return;
		}

		canvasCD.clearSequenceConstraints();

		// create a precedes relationship for all the elements except the last
		for (int i = 0; i < (elements.size() - 1); i++) {
			org.sbolstandard.core2.Component subject = elements.get(i).component;
			org.sbolstandard.core2.Component object = elements.get((i + 1)).component;

			if (subject == null || object == null)
				continue;
			String uniqueId = SBOLUtils.getUniqueDisplayId(canvasCD, null,
					canvasCD.getDisplayId() + "_SequenceConstraint", null, "SequenceConstraint", design);
			canvasCD.createSequenceConstraint(uniqueId, RestrictionType.PRECEDES, subject.getIdentity(),
					object.getIdentity());
		}
	}

	private static class DesignElement {
		private org.sbolstandard.core2.Component component;
		private SequenceAnnotation seqAnn;
		private Part part;

		/**
		 * The component we are making into a design element, the canvas CD, the CD
		 * refered to by the component, and the part.
		 */
		public DesignElement(org.sbolstandard.core2.Component component, ComponentDefinition parentCD,
				ComponentDefinition childCD, Part part, SBOLDocument design) throws SBOLValidationException {
			// Only create a new component if one does not already exist
			if (component == null) {
				this.component = createComponent(parentCD, childCD, design);
			} else {
				this.component = component;
			}

			// Returns the SA that should be set to this.seqAnn
			SequenceAnnotation tempAnn = seqAnnRefersToComponent(this.component, parentCD);
			if (tempAnn == null) {
				// There isn't a SA already, we need to create one
				this.seqAnn = createSeqAnn(parentCD, design);
				// Set seqAnn to refer to this component
				this.seqAnn.setComponent(this.component.getIdentity());
			} else {
				this.seqAnn = tempAnn;
			}

			this.part = part;
		}

		public DesignElement(SequenceAnnotation sequenceAnnotation, ComponentDefinition parentCD, Part part,
				SBOLDocument design) throws SBOLValidationException {

			this.seqAnn = sequenceAnnotation;
			this.part = part;
		}

		/**
		 * Returns null if there isn't a SA belonging to parentCD that refers to
		 * component. Otherwise, returns that SA.
		 */
		private SequenceAnnotation seqAnnRefersToComponent(org.sbolstandard.core2.Component component,
				ComponentDefinition parentCD) {
			SequenceAnnotation result = null;
			for (SequenceAnnotation sa : parentCD.getSequenceAnnotations()) {
				if (sa.getComponentURI() != null && sa.getComponentURI().equals(component.getIdentity())) {
					result = sa;
					break;
				}
			}
			return result;
		}

		private static org.sbolstandard.core2.Component createComponent(ComponentDefinition parentCD,
				ComponentDefinition childCD, SBOLDocument design) throws SBOLValidationException {
			String uniqueId = SBOLUtils.getUniqueDisplayId(parentCD, null, childCD.getDisplayId() + "_Component", "1",
					"Component", design);
			return parentCD.createComponent(uniqueId, AccessType.PUBLIC, childCD.getIdentity());
		}

		private static SequenceAnnotation createSeqAnn(ComponentDefinition parentCD, SBOLDocument design)
				throws SBOLValidationException {
			String uniqueId = SBOLUtils.getUniqueDisplayId(parentCD, null,
					parentCD.getDisplayId() + "_SequenceAnnotation", "1", "SequenceAnnotation", design);
			return parentCD.createSequenceAnnotation(uniqueId, "GenericLocation", OrientationType.INLINE);
		}

		SequenceAnnotation getSeqAnn() {
			return seqAnn;
		}
		
		
		boolean isFeature() {
			return !seqAnn.isSetComponent();
		}

		void setCD(ComponentDefinition CD) throws SBOLValidationException {
			this.component.setDefinition(CD.getIdentity());
		}

		ComponentDefinition getCD() {
			if (component == null) {
				return null;
			}
			return component.getDefinition();
		}

		void setPart(Part part) {
			this.part = part;
		}

		Part getPart() {
			return part;
		}

		public boolean isComposite() {
			ComponentDefinition cd = getCD();

			if (cd == null) {
				return false;
			}

			return !cd.getComponents().isEmpty();
		}

		public boolean hasVariants(SBOLDocument design, ComponentDefinition canvasCD) throws SBOLValidationException {
			ComponentDefinition cd = getCD();

			if (cd == null) {
				return false;
			}

			for (CombinatorialDerivation derivation : design.getCombinatorialDerivations()) {
				if (derivation.getTemplate().equals(canvasCD)) {
					for (VariableComponent vc : derivation.getVariableComponents()) {
						if (vc.getVariable().equals(component)) {
							return true;
						}
					}
				}
			}

			return false;
		}

		public boolean hasSequence() {
			ComponentDefinition cd = getCD();

			if (cd == null) {
				return false;
			}

			return cd.getSequences() != null && cd.getSequences().stream()
					.filter(s -> s.getElements() != null && !s.getElements().equals("")).count() != 0;
		}

		/**
		 * Returns the first location's orientation
		 */
		public OrientationType getOrientation() {
			// returns the first location's orientation
			OrientationType orientation = seqAnn.getLocations().iterator().next().getOrientation();
			if (orientation == null) {
				orientation = OrientationType.INLINE;
			}
			return orientation;
		}

		void flipOrientation() {
			OrientationType orientation = this.getOrientation();
			for (Location loc : seqAnn.getLocations()) {
				loc.setOrientation(orientation == OrientationType.INLINE ? OrientationType.REVERSECOMPLEMENT
						: OrientationType.INLINE);
			}
		}

		public String toString() {
			return getCD().getDisplayId()
					+ (seqAnn.getLocations().iterator().next().getOrientation() == OrientationType.REVERSECOMPLEMENT
							? "-"
							: "");
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

	public void setPanel(SBOLDesignerPanel designerPanel) {
		this.designerPanel = designerPanel;
	}
}
