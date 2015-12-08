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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.openrdf.model.impl.URIImpl;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import com.clarkparsia.sbol.terms.SO;
import com.clarkparsia.swing.AbstractListTableModel;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Iterables;

public enum SOMappingTab implements PreferencesTab {
	INSTANCE;

	@Override
	public String getTitle() {
		return "Mappings";
	}

	@Override
	public String getDescription() {
		return "Mappinfs between SO terms and SBOL visual icons";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("so_icon.png"));
	}

	@Override
	public Component getComponent() {
		final URITableModel tableModel = new URITableModel(Collections.<URI>emptyList());
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(100, 100));
		
		InputDialog.setWidthAsPercentages(table, tableModel.getWidths());
		
		final JComboBox typeSelection = new JComboBox(Iterables.toArray(Parts.sorted(), Part.class));
		typeSelection.setRenderer(new PartCellRenderer());
		typeSelection.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				Part part = (Part) typeSelection.getSelectedItem();
				((URITableModel) table.getModel()).setElements(part.getTypes());
			}
		});
		typeSelection.setSelectedItem(Parts.GENERIC);
		
		FormBuilder form  = new FormBuilder();
		form.add("SBOL Part", typeSelection);
		JPanel topPanel = form.build();
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("SO Types");
		label.setLabelFor(table);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		

		JScrollPane tableScroller = new JScrollPane(table);
//		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
		
//		tablePane.add(label);
//		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePanel.add(tableScroller);
//		tablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

//		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
//		table.setRowSorter(sorter);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(tablePanel, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public void save() {		
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
	
	private static class URITableModel extends AbstractListTableModel<URI> {
		private static final String[] COLUMNS = { "Accession Id", "Name"};
		private static final double[] WIDTHS = { 0.3, 0.7 };

		public URITableModel(List<URI> terms) {
			super(terms, COLUMNS, WIDTHS);
		}
		
		public Object getField(URI term, int col) {
			switch (col) {
				case 0:
					return new URIImpl(term.toString()).getLocalName();
				case 1:
					return SO.getInstance().getTerm(term.toString()).getLabel();
				default:
					throw new IndexOutOfBoundsException();
			}
		}
	}
}