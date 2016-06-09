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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLFactory;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstack.*;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.Registries;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.swing.ComboBoxRenderer;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class StackInputDialog extends InputDialog<SBOLDocument> {

	private final ComboBoxRenderer<Registry> registryRenderer = new ComboBoxRenderer<Registry>() {
		@Override
		protected String getLabel(Registry registry) {
			StringBuilder sb = new StringBuilder();
			if (registry != null) {
				sb.append(registry.getName());
				if (!registry.isBuiltin()) {
					sb.append(" (");
					sb.append(CharSequences.shorten(registry.getURL(), 30));
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

	private JComboBox typeSelection;

	private JTable table;
	private JLabel tableLabel;

	private JCheckBox importSubparts;

	public StackInputDialog(final Component parent, final Part part) {
		super(parent, TITLE);

		this.part = part;

		Registries registries = Registries.get();
		int selectedRegistry = registries.getVersionRegistryIndex();

		if (registries.size() == 0) {
			JOptionPane.showMessageDialog(this,
					"No parts registries are defined.\nPlease click 'Options' and add a parts registry.");
			url = null;
		} else {
			url = registries.get(selectedRegistry).getURL();
		}

		registrySelection = new JComboBox(Iterables.toArray(registries, Registry.class));
		if (registries.size() > 0) {
			registrySelection.setSelectedIndex(selectedRegistry);
		}
		registrySelection.addActionListener(actionListener);
		registrySelection.setRenderer(registryRenderer);

		builder.add("Registry", registrySelection);
	}

	@Override
	public void initFormPanel(FormBuilder builder) {
		if (part != null) {
			List<Part> parts = Lists.newArrayList(Parts.sorted());
			parts.add(0, ALL_PARTS);

			typeSelection = new JComboBox(parts.toArray());
			typeSelection.setRenderer(new PartCellRenderer());
			typeSelection.setSelectedItem(part);
			typeSelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					partTypeChanged();
				}
			});
			builder.add("Part type", typeSelection);
		} else {
			typeSelection = null;
		}

		importSubparts = new JCheckBox("Import with subcomponents");
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

	private boolean isTypeSelection() {
		return typeSelection != null;
	}

	@Override
	protected JPanel initMainPanel() {
		List<ComponentDefinition> components = searchParts(isTypeSelection() ? part : null, url);
		ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);

		JPanel panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");

		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");

		return panel;
	}

	/**
	 * Queries the stack url provided for CDs matching the role(s) of the part
	 */
	private List<ComponentDefinition> searchParts(Part part, String url) {
		try {
			StackFrontend sf = new StackFrontend(url);
			if (part != null) {
				Set<URI> setRoles = new HashSet<URI>();
				setRoles.addAll(part.getRoles());
				Set<ComponentDefinition> setCD = sf.searchComponents(null, setRoles, null, null)
						.getComponentDefinitions();
				List<ComponentDefinition> listCD = new ArrayList<ComponentDefinition>();
				listCD.addAll(setCD);
				return listCD;
			} else {
				Set<ComponentDefinition> setCD = sf.searchComponents(null, new HashSet<URI>(), null, null)
						.getComponentDefinitions();
				List<ComponentDefinition> listCD = new ArrayList<ComponentDefinition>();
				listCD.addAll(setCD);
				return listCD;
			}
		} catch (StackException e) {
			JOptionPane.showMessageDialog(null, "Querying this repository failed");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			ComponentDefinition comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);

			StackFrontend sf = new StackFrontend(url);
			// TODO This may not return the correct CD
			SBOLDocument doc = sf.searchComponents(comp.getName(), comp.getRoles(), null, 1);
			if (!importSubparts.isSelected()) {
				doc = new SBOLDocument();
				doc.createCopy(comp);
			}
			return doc;
		} catch (StackException | SBOLValidationException e) {
			JOptionPane.showMessageDialog(null, "Getting this selection failed");
			e.printStackTrace();
			return null;
		}
	}

	protected void registryChanged() {
		partTypeChanged();
	}

	public void partTypeChanged() {
		List<ComponentDefinition> components = searchParts(
				isTypeSelection() ? (Part) typeSelection.getSelectedItem() : null, url);
		((ComponentDefinitionTableModel) table.getModel()).setElements(components);
		tableLabel.setText("Matching parts (" + components.size() + ")");
	}

	private void updateFilter(String filterText) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TableRowSorter<ComponentDefinitionTableModel> sorter = (TableRowSorter) table.getRowSorter();
		if (filterText.length() == 0) {
			sorter.setRowFilter(null);
		} else {
			try {
				RowFilter<ComponentDefinitionTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
				sorter.setRowFilter(rf);
			} catch (java.util.regex.PatternSyntaxException e) {
				sorter.setRowFilter(null);
			}
		}

		tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
	}
}
