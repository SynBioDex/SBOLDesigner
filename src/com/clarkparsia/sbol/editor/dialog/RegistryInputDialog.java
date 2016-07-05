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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
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

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstack.*;
import org.sbolstack.frontend.ComponentMetadata;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.Registries;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.swing.AbstractListTableModel;
import com.clarkparsia.swing.ComboBoxRenderer;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javassist.NotFoundException;

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
				if (!registry.isBuiltin()) {
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

	private static final String TITLE = "Select a part from registry";

	private static final Part ALL_PARTS = new Part("All parts", "All");
	private Part part;
	private JComboBox<Part> roleSelection;

	private JTable table;
	private JLabel tableLabel;
	private JScrollPane scroller;

	private JCheckBox importSubparts;

	private static StackFrontend stack;

	public RegistryInputDialog(final Component parent, final Part part) {
		super(parent, TITLE);

		this.part = part;

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
		if (part != null) {
			List<Part> parts = Lists.newArrayList(Parts.sorted());
			parts.add(0, ALL_PARTS);

			roleSelection = new JComboBox<Part>(parts.toArray(new Part[0]));
			roleSelection.setRenderer(new PartCellRenderer());
			roleSelection.setSelectedItem(part);
			roleSelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					updateTable();
				}
			});
			builder.add("Part role", roleSelection);
		} else {
			roleSelection = null;
		}

		importSubparts = new JCheckBox("Import with subcomponents");
		importSubparts.setSelected(true);
		builder.add("", importSubparts);

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

	private boolean isRoleSelection() {
		return roleSelection != null;
	}

	@Override
	protected JPanel initMainPanel() {
		JPanel panel;
		if (isMetadata()) {
			List<ComponentMetadata> components = searchParts(isRoleSelection() ? part : null, stack);
			ComponentMetadataTableModel tableModel = new ComponentMetadataTableModel(components);
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
	 * ComponentMetadata.
	 */
	private boolean isMetadata() {
		return !(location.startsWith("file:") || location.startsWith("jar:"))
				&& !((Registry) registrySelection.getSelectedItem()).isBuiltin();
	}

	/**
	 * Gets the SBOLDocument from the path(url) and returns all its CDs.
	 */
	private List<ComponentDefinition> searchParts(Part part) {
		try {
			if (isMetadata()) {
				throw new Exception("Incorrect state.  url isn't a path");
			}
			if (part.equals(ALL_PARTS)) {
				part = null;
			}
			String path = location;
			path = path.replaceAll("file:", "");
			path = path.replaceAll("jar:", "");
			SBOLReader.setURIPrefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			SBOLReader.setCompliant(true);
			FileInputStream stream = new FileInputStream(path);
			SBOLDocument doc = SBOLReader.read(stream);
			stream.close();
			doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			return SBOLUtils.getCDOfRole(doc, part);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Getting the SBOLDocument from path failed: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Queries the stack provided for CDs matching the role(s) of the part
	 */
	private List<ComponentMetadata> searchParts(Part part, StackFrontend stack) {
		try {
			if (!isMetadata()) {
				throw new Exception("Incorrect state.  url is a path");
			}
			if (stack == null) {
				stack = new StackFrontend(location);
			}
			if (part != null) {
				Set<URI> setRoles = new HashSet<URI>();
				setRoles.addAll(part.getRoles());
				ArrayList<ComponentMetadata> l = stack.searchComponentMetadata(null, setRoles, null, null);
				return l;
			} else {
				ArrayList<ComponentMetadata> l = stack.searchComponentMetadata(null, new HashSet<URI>(), 0, 99);
				return l;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Querying this repository failed: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			ComponentDefinition comp;
			int row = table.convertRowIndexToModel(table.getSelectedRow());

			if (isMetadata()) {
				ComponentMetadata compMeta = ((ComponentMetadataTableModel) table.getModel()).getElement(row);
				if (stack == null) {
					stack = new StackFrontend(location);
				}
				comp = stack.fetchComponent(URI.create(compMeta.uri));
			} else {
				comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
			}

			SBOLDocument doc = new SBOLDocument();
			if (!importSubparts.isSelected()) {
				doc.createCopy(comp);
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

	protected void registryChanged() {
		if (isMetadata()) {
			stack = new StackFrontend(location);
		}
		updateTable();
	}

	public void updateTable() {
		if (isMetadata()) {
			List<ComponentMetadata> components = searchParts(
					isRoleSelection() ? (Part) roleSelection.getSelectedItem() : null, stack);
			ComponentMetadataTableModel tableModel = new ComponentMetadataTableModel(components);
			table = new JTable(tableModel);
			tableLabel.setText("Matching parts (" + components.size() + ")");
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			table.setRowSorter(sorter);
			setWidthAsPercentages(table, tableModel.getWidths());
		} else {
			Part part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : null;
			List<ComponentDefinition> components = searchParts(part);
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
					canceled = false;
					setVisible(false);
				}
			}
		});
		scroller.setViewportView(table);
	}

	private void updateFilter(String filterText) {
		if (isMetadata()) {
			TableRowSorter<ComponentMetadataTableModel> sorter = (TableRowSorter) table.getRowSorter();
			if (filterText.length() == 0) {
				sorter.setRowFilter(null);
			} else {
				try {
					RowFilter<ComponentMetadataTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
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
					RowFilter<ComponentDefinitionTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
					sorter.setRowFilter(rf);
				} catch (PatternSyntaxException e) {
					sorter.setRowFilter(null);
				}
			}
			tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
		}
	}
}
