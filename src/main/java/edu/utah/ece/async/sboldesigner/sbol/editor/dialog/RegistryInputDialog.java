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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

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

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceOntology;
import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;
import org.synbiohub.frontend.WebOfRegistriesData;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.utah.ece.async.sboldesigner.sbol.CharSequenceUtil;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.SynBioHubFrontends;
import edu.utah.ece.async.sboldesigner.swing.ComboBoxRenderer;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

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
					sb.append(CharSequenceUtil.shorten(registry.getLocation(), 30));
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

	public static final Part ALL_PARTS = new Part("All parts", "All");
	// represents what part we should display in role selection
	private Part part;
	// represents the role of the template CD, could be used in roleRefinement
	private URI refinementRole;
	private JComboBox<Part> roleSelection;
	private JComboBox<String> roleRefinement;
	private ActionListener roleRefinementListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			updateTable();
		}
	};
	private Types type;
	private SBOLDocument workingDoc;
	private JComboBox<Types> typeSelection;
	private JComboBox<IdentifiedMetadata> collectionSelection;
	private boolean updateCollection = true;
	private ActionListener collectionSelectionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			// only update collectionSelection when we aren't programmatically
			// modifying it in collectionSelectionListener
			if (updateCollection) {
				updateCollectionSelection(false, null);
				updateTable();
			}
		}
	};
	private static HashMap<Registry, ArrayList<IdentifiedMetadata>> collectionPaths = new HashMap<>();

	private JTable table;
	private JLabel tableLabel;
	private JScrollPane scroller;

	final JTextField filterSelection = new JTextField();
	/*
	 * Determines whether the table should be refreshed when a user types in
	 * filter text. This is true when the results from SynBioHubQuery exceeds
	 * the QUERY_LIMIT.
	 */
	private boolean refreshSearch = false;
	/*
	 * Stores the filter text that caused the current ArrayList<TableMetadata>.
	 */
	private String cacheKey = "";

	private ComponentDefinitionBox root;

	private static SynBioHubFrontend synBioHub;

	private boolean allowCollectionSelection = false;

	private String objectType = "ComponentDefinition";

	/**
	 * Allows a collection to be selected.
	 */
	public void allowCollectionSelection() {
		allowCollectionSelection = true;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * For when the working document is known and the root is not needed.
	 */
	public RegistryInputDialog(final Component parent, final Part part, Types type, URI refinementRole,
			SBOLDocument workingDoc) {
		super(parent, TITLE);
		this.workingDoc = workingDoc;
		setup(null, part, type, refinementRole);
	}

	/**
	 * For when the working document is unknown and the root is not needed.
	 */
	public RegistryInputDialog(final Component parent, final Part part, Types type, URI refinementRole) {
		super(parent, TITLE);
		this.workingDoc = null;
		setup(null, part, type, refinementRole);
	}

	/**
	 * For when the working document is known and preferences node shouldn't be
	 * used
	 */
	public RegistryInputDialog(final Component parent, ComponentDefinitionBox root, final Part part, Types type,
			URI refinementRole, SBOLDocument workingDoc) {
		super(parent, TITLE);
		this.workingDoc = workingDoc;
		setup(root, part, type, refinementRole);
	}

	/**
	 * For when the working document is unknown and preferences node should be
	 * used
	 */
	public RegistryInputDialog(final Component parent, ComponentDefinitionBox root, final Part part, Types type,
			URI refinementRole) {
		super(parent, TITLE);
		this.workingDoc = null;
		setup(root, part, type, refinementRole);
	}

	/**
	 * root, if not null, will reference the root CD that was selected.
	 */
	private void setup(ComponentDefinitionBox root, final Part part, Types type, URI refinementRole) {
		this.root = root;
		this.part = part;
		this.refinementRole = refinementRole;
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
			uriPrefix = null;
		} else {
			location = registries.get(selectedRegistry).getLocation();
			uriPrefix = registries.get(selectedRegistry).getUriPrefix();
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
				updateContext();
			}
		});
		if (objectType == "ComponentDefinition") {
			builder.add("Part type", typeSelection);
		}

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
				part = (Part) roleSelection.getSelectedItem();
				updateRoleRefinement();
				updateTable();
			}
		});
		if (objectType == "ComponentDefinition") {
			builder.add("Part role", roleSelection);
		}

		// set up the JComboBox for role refinement
		roleRefinement = new JComboBox<String>();
		updateRoleRefinement();
		roleRefinement.removeActionListener(roleRefinementListener);
		if (refinementRole != null && refinementRole != part.getRole()) {
			String roleName = new SequenceOntology().getName(refinementRole);
			if (!comboBoxContains(roleRefinement, roleName)) {
				roleRefinement.addItem(roleName);
			}
			roleRefinement.setSelectedItem(roleName);
		}
		roleRefinement.addActionListener(roleRefinementListener);
		if (objectType == "ComponentDefinition") {
			builder.add("Role refinement", roleRefinement);
		}
		updateContext();

		// set up the filter
		filterSelection.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent paramDocumentEvent) {
				searchOrFilterTable();
			}

			@Override
			public void insertUpdate(DocumentEvent paramDocumentEvent) {
				searchOrFilterTable();
			}

			@Override
			public void changedUpdate(DocumentEvent paramDocumentEvent) {
				searchOrFilterTable();
			}

			private void searchOrFilterTable() {
				/*
				 * System.out.println();
				 * System.out.println("searchOrFilterTable");
				 * System.out.println("refreshSearch: " + refreshSearch);
				 * System.out.println("cacheKey: " + cacheKey);
				 * System.out.println("filter: " + filterSelection.getText());
				 */
				if ((refreshSearch || filterSelection.getText().equals("")
						|| !filterSelection.getText().contains(cacheKey)) && isMetadata()) {
					searchParts(part, synBioHub, filterSelection.getText());
				} else {
					updateFilter(filterSelection.getText());
				}
			}
		});
		builder.add("Filter parts", filterSelection);
	}

	/**
	 * Returns whether box contains s
	 */
	private boolean comboBoxContains(JComboBox<String> box, String s) {
		for (int i = 0; i < box.getItemCount(); i++) {
			if (s != null && s.equals(roleRefinement.getItemAt(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected JPanel initMainPanel() {
		JPanel panel;

		Part part = null;
		if (roleSelection.isEnabled() && roleRefinement.isEnabled()) {
			String roleName = (String) roleRefinement.getSelectedItem();
			if (roleName == null || roleName.equals("None")) {
				part = (Part) roleSelection.getSelectedItem();
			} else {
				SequenceOntology so = new SequenceOntology();
				URI role = so.getURIbyName(roleName);
				part = new Part(role, null, null);
			}
		} else {
			part = ALL_PARTS;
		}

		if (isMetadata()) {
			searchParts(part, synBioHub, filterSelection.getText());
			TableMetadataTableModel tableModel = new TableMetadataTableModel(new ArrayList<TableMetadata>());
			panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");
		} else {
			List<ComponentDefinition> components = searchParts(part);
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
		return location.startsWith("http://") || location.startsWith("https://");
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
				if (workingDoc != null) {
					// workingDoc is specified, so use that
					doc = workingDoc;
				} else {
					// read from SBOLUtils.setupFile();
					File file = SBOLUtils.setupFile();

					if (file.exists()) {
						doc = SBOLReader.read(file);
					} else {
						// JOptionPane.showMessageDialog(null, "The working
						// document could not be found on disk. Try opening the
						// file again.");
						return new ArrayList<ComponentDefinition>();
					}
				}

			} else {
				// read from the location (path)
				doc = SBOLReader.read(location);
			}

			doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			return SBOLUtils.getCDOfRole(doc, part);

		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.showMessage(null, "Getting the SBOLDocument from path failed: ", e.getMessage());
			Registries registries = Registries.get();
			registries.setVersionRegistryIndex(0);
			registries.save();
			return null;
		}
	}

	/**
	 * Queries SynBioHub for CDs matching the role(s), type(s), and
	 * collection(s) of the part. Also filters by the filterText.
	 */
	private void searchParts(Part part, SynBioHubFrontend synbiohub, String filterText) {
		try {
			if (!isMetadata()) {
				throw new Exception("Incorrect state.  url is a path");
			}

			if (synbiohub == null) {
				synbiohub = createSynBioHubFrontend(location, uriPrefix);
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
				SynBioHubQuery query = new SynBioHubQuery(synbiohub, setRoles, setTypes, setCollections, filterText,
						objectType, new TableUpdater(), this);
				// non-blocking: will update using the TableUpdater
				query.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Querying this repository failed: " + e.getMessage() + "\n"
					+ " Internet connection is required for importing from SynBioHub. Setting default registry to built-in parts, which doesn't require an internet connection.");
			Registries registries = Registries.get();
			registries.setVersionRegistryIndex(0);
			registries.save();
		}
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			SBOLDocument document = null;
			ComponentDefinition comp = null;
			int row = table.convertRowIndexToModel(table.getSelectedRow());

			if (isMetadata()) {
				TableMetadata compMeta = ((TableMetadataTableModel) table.getModel()).getElement(row);

				if (synBioHub == null) {
					synBioHub = createSynBioHubFrontend(location, uriPrefix);
				}

				if (compMeta.isCollection && !allowCollectionSelection) {
					JOptionPane.showMessageDialog(getParent(), "Selecting collections is not allowed");
					return new SBOLDocument();
				}

				document = synBioHub.getSBOL(URI.create(compMeta.identified.getUri()));
				comp = document.getComponentDefinition(URI.create(compMeta.identified.getUri()));

				if (comp == null) {
					// if cannot find it then return root component definition
					// from document
					for (ComponentDefinition cd : document.getRootComponentDefinitions()) {
						comp = cd;
					}
				}
			} else {
				document = new SBOLDocument();
				comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
				ArrayList<WebOfRegistriesData> webOfRegistries;
				try {
					webOfRegistries = SynBioHubFrontend.getRegistries(); // TODO: replace with preferences
					for (WebOfRegistriesData registry : webOfRegistries) {
						document.addRegistry(registry.getInstanceUrl(),registry.getUriPrefix());
					}
				}
				catch (SynBioHubException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				document = document.createRecursiveCopy(comp);
			}

			if (root != null) {
				root.cd = document.getComponentDefinition(comp.getIdentity());
			}

			return document;
		} catch (SBOLValidationException | SynBioHubException e) {
			e.printStackTrace();
			MessageDialog.showMessage(null, "Getting this selection failed: ", e.getMessage());
			return null;
		}
	}

	@Override
	protected void registryChanged() {
		if (isMetadata()) {
			synBioHub = createSynBioHubFrontend(location, uriPrefix);
		}
		loginButton.setEnabled(isMetadata());
		updateCollectionSelection(true, null);
		updateTable();
	}

	private void updateCollectionSelection(boolean registryChanged, IdentifiedMetadata newCollection) {
		collectionSelection.setEnabled(isMetadata());
		if (!isMetadata()) {
			return;
		}
		if (synBioHub == null) {
			synBioHub = createSynBioHubFrontend(location, uriPrefix);
		}
		Registry registry = (Registry) registrySelection.getSelectedItem();

		updateCollection = false;
		if (registryChanged) {
			// display only "rootCollections"
			IdentifiedMetadata rootCollections = new IdentifiedMetadata();
			rootCollections.setName("Root Collections");
			rootCollections.setDisplayId("Root Collections");
			rootCollections.setUri("");
			collectionSelection.removeAllItems();
			collectionSelection.addItem(rootCollections);
			collectionSelection.setSelectedItem(rootCollections);

			// restore/create cached collection path
			if (collectionPaths.containsKey(registry)) {
				for (IdentifiedMetadata collection : collectionPaths.get(registry)) {
					collectionSelection.addItem(collection);
					collectionSelection.setSelectedItem(collection);
				}
			} else {
				collectionPaths.put(registry, new ArrayList<>());
			}
		} else {
			// clicked on different collection
			if (newCollection != null) {
				collectionSelection.addItem(newCollection);
				collectionSelection.setSelectedItem(newCollection);
				collectionPaths.get(registry).add(newCollection);
			} else {
				while (collectionSelection.getSelectedIndex() + 1 < collectionSelection.getItemCount()) {
					collectionSelection.removeItemAt(collectionSelection.getSelectedIndex() + 1);
					collectionPaths.get(registry).remove(collectionSelection.getSelectedIndex());
				}
			}
		}

		updateCollection = true;
	}

	private void updateRoleRefinement() {
		roleRefinement.removeActionListener(roleRefinementListener);
		roleRefinement.removeAllItems();
		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
			roleRefinement.addItem(s);
		}
		roleRefinement.addActionListener(roleRefinementListener);
	}

	private void updateContext() {
		boolean enableRoles = typeSelection.getSelectedItem() == Types.DNA
				|| typeSelection.getSelectedItem() == Types.RNA;
		roleSelection.setEnabled(enableRoles);
		roleRefinement.setEnabled(enableRoles);
	}

	public void updateTable() {
		// create the part criteria
		Part part = null;
		if (roleSelection.isEnabled() && roleRefinement.isEnabled()) {
			String roleName = (String) roleRefinement.getSelectedItem();
			if (roleName == null || roleName.equals("None")) {
				part = (Part) roleSelection.getSelectedItem();
			} else {
				SequenceOntology so = new SequenceOntology();
				URI role = so.getURIbyName(roleName);
				part = new Part(role, null, null);
			}
		} else {
			part = ALL_PARTS;
		}

		if (isMetadata()) {
			searchParts(part, synBioHub, filterSelection.getText());
		} else {
			List<ComponentDefinition> components = searchParts(part);
			components = SBOLUtils.getCDOfType(components, (Types) typeSelection.getSelectedItem());
			ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);
			table = new JTable(tableModel);
			tableLabel.setText("Matching parts (" + components.size() + ")");
			refreshSearch = false;
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
					handleTableSelection(false);
				}
			}
		});
		scroller.setViewportView(table);
	}

	@Override
	protected void handleTableSelection(boolean select) {
		// handle collection selected
		if (isMetadata()) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			TableMetadata meta = ((TableMetadataTableModel) table.getModel()).getElement(row);
			if (meta.isCollection && (!select || !allowCollectionSelection)) {
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
		public void updateTable(ArrayList<TableMetadata> identified, String filterText) {
			if (!filterSelection.getText().equals(filterText)) {
				// don't update if the filterSelection text has changed.
				return;
			}

			TableMetadataTableModel tableModel = new TableMetadataTableModel(identified);
			table = new JTable(tableModel);
			tableLabel.setText("Matching parts (" + identified.size() + ")");

			refreshSearch = identified.size() >= SynBioHubQuery.QUERY_LIMIT;
			if (filterText != null && !refreshSearch) {
				cacheKey = filterText;
			}
			/*
			 * System.out.println(); System.out.println("TableUpdater");
			 * System.out.println("refreshSearch: " + refreshSearch);
			 * System.out.println("cacheKey: " + cacheKey); System.out.println(
			 * "filter: " + filterSelection.getText());
			 */

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
						handleTableSelection(false);
					}
				}
			});

			scroller.setViewportView(table);
		}
	}

	/**
	 * Wraps SynBioHubFrontend creation so legacy locations can be used.
	 */
	private SynBioHubFrontend createSynBioHubFrontend(String location, String uriPrefix) {
		// update location and SynBioHub location if not using https
		if (location == "http://synbiohub.org") {
			location = "https://synbiohub.org";

			// This isn't elegant, but should work
			ArrayList<Registry> oldRegistries = new ArrayList<Registry>();
			for (int i = 3; i < Registries.get().size(); i++) {
				oldRegistries.add(Registries.get().get(i));
			}

			Registries.get().restoreDefaults();

			for (Registry r : oldRegistries) {
				Registries.get().add(r);
			}

			Registries.get().save();
		}

		// get logged in SynBioHubFrontend if possible
		SynBioHubFrontends frontends = new SynBioHubFrontends();
		if (frontends.hasFrontend(location)) {
			return frontends.getFrontend(location);
		}

		return new SynBioHubFrontend(location, uriPrefix);
	}
}
