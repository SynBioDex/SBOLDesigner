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
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;

import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.SublimeSBOLFactory;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class SelectPartDialog extends InputDialog<DnaComponent> {
	private static final String TITLE = "Select a part from registry";
	
	private static final Part ALL_PARTS = new Part("All parts", "All");

	private Part part;
	
	private JComboBox typeSelection;

	private JTable table;
	private JLabel tableLabel;
	
	private JCheckBox importSubparts;

	public SelectPartDialog(final Component parent, final Part part) {
		super(parent, TITLE, RegistryType.PART);	

		this.part = part;
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
		}
		else {
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
		List<DnaComponent> components = SPARQLUtilities.findMatchingParts(endpoint, isTypeSelection() ? part : ALL_PARTS);
		DnaComponentTableModel tableModel = new DnaComponentTableModel(components);
		
		JPanel panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");

		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");
		
		return panel;
	}

	@Override
    protected DnaComponent getSelection() {
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		DnaComponent comp = ((DnaComponentTableModel) table.getModel()).getElement(row);
		if (importSubparts.isSelected()) {
			try {
				SBOLDocument doc = SublimeSBOLFactory.createReader(endpoint, false).read(comp.getURI().toString());
		        comp = SBOLUtils.getRootComponent(doc);
	        }
	        catch (Exception e) {
		        e.printStackTrace();
	        }
		}
		return comp;
	}
    
	@Override
	protected void registryChanged() {
		partTypeChanged();
	}
	
	public void partTypeChanged() {
		Part part = isTypeSelection() ? (Part) typeSelection.getSelectedItem() : ALL_PARTS;
		List<DnaComponent> components = SPARQLUtilities.findMatchingParts(endpoint, part);
		((DnaComponentTableModel) table.getModel()).setElements(components);
		tableLabel.setText("Matching parts (" + components.size() + ")");
	}

	private void updateFilter(String filterText) {		
		@SuppressWarnings( { "rawtypes", "unchecked" })
		TableRowSorter<DnaComponentTableModel> sorter = (TableRowSorter) table.getRowSorter();
		if (filterText.length() == 0) {
			sorter.setRowFilter(null);
		}
		else {
			try {
				RowFilter<DnaComponentTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
				sorter.setRowFilter(rf);
			}
			catch (java.util.regex.PatternSyntaxException e) {
				sorter.setRowFilter(null);
			}
		}
		
		tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
	}
}
