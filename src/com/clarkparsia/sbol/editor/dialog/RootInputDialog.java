package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
import org.sbolstandard.core2.SBOLValidationException;

import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Lists;

/**
 * A GUI for choosing a root CD from an SBOLDocument
 * 
 * @author Michael Zhang
 */
public class RootInputDialog extends InputDialog<SBOLDocument> {
	private static final String TITLE = "Select a root ComponentDefinition to open";

	private JTable table;
	private JLabel tableLabel;

	private JComboBox<Part> roleSelection;
	private static final Part ALL_PARTS = new Part("All parts", "All");

	private SBOLDocument doc;

	/**
	 * this.getInput() returns an SBOLDocument with a single rootCD selected
	 * from the rootCDs in doc.
	 */
	public RootInputDialog(final Component parent, SBOLDocument doc) {
		super(parent, TITLE);

		this.doc = doc;
	}

	@Override
	protected String initMessage() {
		return "There are multiple root ComponentDefinitions.  Which would you like to load?  (You will be editing a new partial design)";
	}

	@Override
	public void initFormPanel(FormBuilder builder) {
		List<Part> parts = Lists.newArrayList(Parts.sorted());
		parts.add(0, ALL_PARTS);

		roleSelection = new JComboBox<Part>(parts.toArray(new Part[0]));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.setSelectedItem(ALL_PARTS);
		roleSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				partRoleChanged();
			}
		});
		builder.add("Part role", roleSelection);

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

	@Override
	protected JPanel initMainPanel() {
		List<ComponentDefinition> components = new ArrayList<ComponentDefinition>();
		components.addAll(doc.getRootComponentDefinitions());

		ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);
		JPanel panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");
		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");

		return panel;
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			ComponentDefinition comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
			return doc.createRecursiveCopy(comp);
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(null, "This ComponentDefinition cannot be imported: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void partRoleChanged() {
		Part part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
		List<ComponentDefinition> components = SBOLUtils.getCDOfRole(doc, part);
		((ComponentDefinitionTableModel) table.getModel()).setElements(components);
		tableLabel.setText("Matching parts (" + components.size() + ")");
	}

	private boolean isRoleSelection() {
		return roleSelection != null;
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
