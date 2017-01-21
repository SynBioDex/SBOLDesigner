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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.sbolstack.frontend.IdentifiedMetadata;
import org.sbolstack.frontend.StackFrontend;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.SBOLUtils.Types;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.Registries;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.swing.ComboBoxRenderer;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class RegistryInputDialog extends InputDialog<SBOLDocument> {

	private final ComboBoxRenderer<Registry> registryRenderer = new ComboBoxRenderer<Registry>() {
		@Override
		protected String getLabel(Registry registry) {
			StringBuilder sb = new StringBuilder();
			if (registry != null) {
				sb.append(registry.getName());
				if (!registry.getLocation().equals("N/A")) {
					sb.append(" (");
					sb.append(CharSequences.shorten(registry.getLocation(), 30));
					sb.append(")");
				}
			}
			return sb.toString();
		}

		@Override
		protected String getToolTip(Registry registry) {
			return registry == null ? "" : registry.getDescription();
		}
	};

	private final ComboBoxRenderer<IdentifiedMetadata> collectionsRenderer = new ComboBoxRenderer<IdentifiedMetadata>() {
		@Override
		protected String getLabel(IdentifiedMetadata collection) {
			if (collection != null) {
				return collection.getDisplayId();
			} else {
				return "Unknown";
			}
		}

		@Override
		protected String getToolTip(IdentifiedMetadata collection) {
			return collection == null ? "" : collection.getDescription();
		}
	};

	private static final String TITLE = "Select a part from registry";

	private static final Part ALL_PARTS = new Part("All parts", "All");
	// represents what part we should display in role selection
	private Part part;
	// represents the role of the template CD, could be used in roleRefinement
	private URI role;
	private JComboBox<Part> roleSelection;
	private JComboBox<String> roleRefinement;
	private ActionListener roleRefinementListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			updateTable();
		}
	};
	private Types type;
	private JComboBox<Types> typeSelection;
	private JComboBox<IdentifiedMetadata> collectionSelection;
	private ActionListener collectionSelectionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			updateCollectionSelection(false, null);
			updateTable();
		}
	};

	private JTable table;
	private JLabel tableLabel;
	private JScrollPane scroller;

	private JCheckBox importSubparts;

	private static StackFrontend stack;
	private SBOLDocument design;

	public RegistryInputDialog(final Component parent, final Part part, Types type, URI role, SBOLDocument design) {
		super(parent, TITLE);

		this.design = design;
		this.part = part;
		this.role = role;
		this.type = type;

		Registries registries = Registries.get();
		int selectedRegistry = registries.getVersionRegistryIndex();

		registrySelection = new JComboBox<Registry>(Iterables.toArray(registries, Registry.class));
		if (registries.size() > 0) {
			registrySelection.setSelectedIndex(selectedRegistry);
		}
		registrySelection.addActionListener(actionListener);
		registrySelection.setRenderer(registryRenderer);
		builder.add("Registry", registrySelection);

		if (registries.size() == 0) {
			JOptionPane.showMessageDialog(this,
					"No parts registries are defined.\nPlease click 'Options' and add a parts registry.");
			location = null;
		} else {
			location = registries.get(selectedRegistry).getLocation();
		}
	}

	@Override
	public void initFormPanel(FormBuilder builder) {
		// set up type selection
		typeSelection = new JComboBox<Types>(Types.values());
		typeSelection.setSelectedItem(type);
		typeSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTable();
			}
		});
		builder.add("Part type", typeSelection);

		// set up collection selection
		collectionSelection = new JComboBox<IdentifiedMetadata>();
		collectionSelection.setRenderer(collectionsRenderer);
		updateCollectionSelection(true, null);
		collectionSelection.addActionListener(collectionSelectionListener);
		builder.add("Collection", collectionSelection);

		// set up role selection
		List<Part> parts = Lists.newArrayList(Parts.sorted());
		parts.add(0, ALL_PARTS);
		roleSelection = new JComboBox<Part>(parts.toArray(new Part[0]));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.setSelectedItem(part);
		roleSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateRoleRefinement();
				updateTable();
			}
		});
		builder.add("Part role", roleSelection);

		// set up the JComboBox for role refinement
		roleRefinement = new JComboBox<String>();
		updateRoleRefinement();
		roleRefinement.removeActionListener(roleRefinementListener);
		if (role != null && role != part.getRole()) {
			String roleName = new SequenceOntology().getName(role);
			if (!comboBoxContains(roleRefinement, roleName)) {
				roleRefinement.addItem(roleName);
			}
			roleRefinement.setSelectedItem(roleName);
		}
		roleRefinement.addActionListener(roleRefinementListener);
		builder.add("Role refinement", roleRefinement);

		// set up the import subparts checkbox
		importSubparts = new JCheckBox("Import with subcomponents");
		importSubparts.setSelected(true);
		builder.add("", importSubparts);

		// set up the filter
		final JTextField filterSelection = new JTextField();
		filterSelection.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}
		});
		builder.add("Filter parts", filterSelection);
	}

	/**
	 * Returns whether box contains s
	 */
	private boolean comboBoxContains(JComboBox<String> box, String s) {
		for (int i = 0; i < box.getItemCount(); i++) {
			if (s.equals(roleRefinement.getItemAt(i))) {
				return true;
			}
		}
		return false;
	}

	private boolean isRoleSelection() {
		return roleSelection != null;
	}

	@Override
	protected JPanel initMainPanel() {
		JPanel panel;

		Part part;
		String roleName = (String) roleRefinement.getSelectedItem();
		if (roleName == null || roleName.equals("None")) {
			part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
		} else {
			SequenceOntology so = new SequenceOntology();
			URI role = so.getURIbyName(roleName);
			part = new Part(role, null, null);
		}

		if (isMetadata()) {
			searchParts(isRoleSelection() ? part : null, stack);
			TableMetadataTableModel tableModel = new TableMetadataTableModel(new ArrayList<TableMetadata>());
			panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");
		} else {
			List<ComponentDefinition> components = searchParts(isRoleSelection() ? part : null);
			ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);
			panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");
		}

		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");
		scroller = (JScrollPane) panel.getClientProperty("scroller");

		return panel;
	}

	/**
	 * Checks to see if the registry we are working on is represented by
	 * IdentifiedMetadata.
	 */
	private boolean isMetadata() {
		return location.startsWith("http://");
	}

	/**
	 * Gets the SBOLDocument from the path (file on disk) and returns all its
	 * CDs.
	 */
	private List<ComponentDefinition> searchParts(Part part) {
		try {
			if (isMetadata()) {
				throw new Exception("Incorrect state.  url isn't a path");
			}
			if (part.equals(ALL_PARTS)) {
				part = null;
			}

			SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			SBOLReader.setCompliant(true);
			SBOLDocument doc;
			Registry registry = (Registry) registrySelection.getSelectedItem();
			if (registry.equals(Registry.BUILT_IN)) {
				// read from BuiltInParts.xml
				doc = SBOLReader.read(Registry.class.getResourceAsStream("/BuiltInParts.xml"));
			} else if (registry.equals(Registry.WORKING_DOCUMENT)) {
				// read from SBOLUtils.setupFile();
				File file = SBOLUtils.setupFile();
				if (file.exists()) {
					doc = SBOLReader.read(file);
				} else {
					JOptionPane.showMessageDialog(null,
							"The working document could not be found on disk.  Try opening the file again.");
					return new ArrayList<ComponentDefinition>();
				}
			} else {
				// read from the location (path)
				doc = SBOLReader.read(location);
			}
			doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			return SBOLUtils.getCDOfRole(doc, part);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Getting the SBOLDocument from path failed: " + e.getMessage());
			Registries registries = Registries.get();
			registries.setVersionRegistryIndex(0);
			registries.save();
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Queries the stack provided for CDs matching the role(s) of the part
	 */
	private void searchParts(Part part, StackFrontend stack) {
		try {
			if (!isMetadata()) {
				throw new Exception("Incorrect state.  url is a path");
			}
			if (stack == null) {
				stack = new StackFrontend(location);
			}
			if (part != null) {
				// create the query
				IdentifiedMetadata selectedCollection = (IdentifiedMetadata) collectionSelection.getSelectedItem();
				if (selectedCollection == null || selectedCollection.getUri() == null) {
					return;
				}
				Set<URI> setCollections = new HashSet<URI>(Arrays.asList(URI.create(selectedCollection.getUri())));
				Set<URI> setRoles = new HashSet<URI>(part.getRoles());
				Set<URI> setTypes = SBOLUtils.convertTypesToSet((Types) typeSelection.getSelectedItem());
				SBOLStackQuery query = new SBOLStackQuery(stack, setRoles, setTypes, setCollections, new TableUpdater(),
						this);
				// non-blocking: will update using the TableUpdater
				query.execute();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Querying this repository failed: " + e.getMessage() + "\n"
					+ " Internet connection is required for importing from the SBOL Stack. Setting default registry to built-in parts, which doesn't require an internet connection.");
			Registries registries = Registries.get();
			registries.setVersionRegistryIndex(0);
			registries.save();
			e.printStackTrace();
		}
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			ComponentDefinition comp;
			int row = table.convertRowIndexToModel(table.getSelectedRow());

			if (isMetadata()) {
				TableMetadata compMeta = ((TableMetadataTableModel) table.getModel()).getElement(row);
				if (stack == null) {
					stack = new StackFrontend(location);
				}
				if (compMeta.isCollection) {
					return new SBOLDocument();
				}
				comp = stack.fetchComponentDefinition(URI.create(compMeta.identified.getUri()));
			} else {
				comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
			}

			SBOLDocument doc = new SBOLDocument();
			if (!importSubparts.isSelected()) {
				// remove all dependencies
				comp.clearSequenceConstraints();
				comp.clearSequenceAnnotations();
				comp.clearComponents();
				doc.createCopy(comp);
				if (comp.getSequenceByEncoding(Sequence.IUPAC_DNA) != null) {
					doc.createCopy(comp.getSequenceByEncoding(Sequence.IUPAC_DNA));
				}
			} else {
				doc = doc.createRecursiveCopy(comp);
			}
			return doc;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Getting this selection failed: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void registryChanged() {
		if (isMetadata()) {
			stack = new StackFrontend(location);
		}
		updateCollectionSelection(true, null);
		updateTable();
	}

	private void updateCollectionSelection(boolean registryChanged, IdentifiedMetadata newCollection) {
		collectionSelection.removeActionListener(collectionSelectionListener);
		collectionSelection.setEnabled(isMetadata());
		if (!isMetadata()) {
			collectionSelection.addActionListener(collectionSelectionListener);
			return;
		}
		if (stack == null) {
			stack = new StackFrontend(location);
		}

		if (registryChanged) {
			// display only "allCollections"
			IdentifiedMetadata allCollections = new IdentifiedMetadata();
			allCollections.setName("All Collections");
			allCollections.setDisplayId("All Collections");
			allCollections.setUri("");
			collectionSelection.removeAllItems();
			collectionSelection.addItem(allCollections);
			collectionSelection.setSelectedItem(allCollections);
			collectionSelection.addActionListener(collectionSelectionListener);
			return;
		}

		if (newCollection != null) {
			collectionSelection.addItem(newCollection);
			collectionSelection.setSelectedItem(newCollection);
		} else {
			while (collectionSelection.getSelectedIndex() + 1 < collectionSelection.getItemCount()) {
				collectionSelection.removeItemAt(collectionSelection.getSelectedIndex() + 1);
			}
		}

		collectionSelection.addActionListener(collectionSelectionListener);
	}

	private void updateRoleRefinement() {
		roleRefinement.removeActionListener(roleRefinementListener);
		roleRefinement.removeAllItems();
		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
			roleRefinement.addItem(s);
		}
		roleRefinement.addActionListener(roleRefinementListener);
	}

	public void updateTable() {
		// create the part criteria
		Part part;
		String roleName = (String) roleRefinement.getSelectedItem();
		if (roleName == null || roleName.equals("None")) {
			part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
		} else {
			SequenceOntology so = new SequenceOntology();
			URI role = so.getURIbyName(roleName);
			part = new Part(role, null, null);
		}

		if (isMetadata()) {
			searchParts(part, stack);
		} else {
			List<ComponentDefinition> components = searchParts(part);
			components = SBOLUtils.getCDOfType(components, (Types) typeSelection.getSelectedItem());
			ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);
			table = new JTable(tableModel);
			tableLabel.setText("Matching parts (" + components.size() + ")");
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			table.setRowSorter(sorter);
			setWidthAsPercentages(table, tableModel.getWidths());
		}
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				setSelectAllowed(table.getSelectedRow() >= 0);
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
					handleTableSelection();
				}
			}
		});
		scroller.setViewportView(table);
	}

	@Override
	protected void handleTableSelection() {
		// handle collection selected
		if (isMetadata()) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			TableMetadata meta = ((TableMetadataTableModel) table.getModel()).getElement(row);
			if (meta.isCollection) {
				updateCollectionSelection(false, meta.identified);
				updateTable();
				return;
			}
		}
		// otherwise a part was selected
		canceled = false;
		setVisible(false);
	}

	private void updateFilter(String filterText) {
		filterText = "(?i)" + filterText;
		if (isMetadata()) {
			TableRowSorter<TableMetadataTableModel> sorter = (TableRowSorter) table.getRowSorter();
			if (filterText.length() == 0) {
				sorter.setRowFilter(null);
			} else {
				try {
					RowFilter<TableMetadataTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1, 2, 4);
					sorter.setRowFilter(rf);
				} catch (PatternSyntaxException e) {
					sorter.setRowFilter(null);
				}
			}
			tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
		} else {
			TableRowSorter<ComponentDefinitionTableModel> sorter = (TableRowSorter) table.getRowSorter();
			if (filterText.length() == 0) {
				sorter.setRowFilter(null);
			} else {
				try {
					RowFilter<ComponentDefinitionTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1, 2, 4);
					sorter.setRowFilter(rf);
				} catch (PatternSyntaxException e) {
					sorter.setRowFilter(null);
				}
			}
			tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
		}
	}

	/**
	 * Updates the table using the provided components. This lets the
	 * SBOLStackQuery thread update the table.
	 */
	public class TableUpdater {
		public void updateTable(ArrayList<TableMetadata> identified) {
			TableMetadataTableModel tableModel = new TableMetadataTableModel(identified);
			table = new JTable(tableModel);
			tableLabel.setText("Matching parts (" + identified.size() + ")");
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			table.setRowSorter(sorter);
			setWidthAsPercentages(table, tableModel.getWidths());
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent event) {
					setSelectAllowed(table.getSelectedRow() >= 0);
				}
			});
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
						handleTableSelection();
					}
				}
			});
			scroller.setViewportView(table);
		}
	}
}
