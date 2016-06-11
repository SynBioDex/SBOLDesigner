package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.collect.Lists;

/**
 * A GUI for choosing a CD from an SBOLDocument
 * 
 * @author Michael Zhang
 */
public class PartInputDialog extends InputDialog<SBOLDocument> {
	private static final String TITLE = "Select a part to import";

	private Part part;
	private JComboBox<Part> roleSelection;
	public static final Part ALL_PARTS = new Part("All parts", "All");

	private JTable table;
	private JLabel tableLabel;

	private JCheckBox importSubparts;

	private SBOLDocument doc;

	public PartInputDialog(final Component parent, SBOLDocument doc, final Part part) {
		super(parent, TITLE);

		this.doc = doc;
		this.part = part;
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
					partRoleChanged();
				}
			});
			builder.add("Part role", roleSelection);
		} else {
			roleSelection = null;
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

	private boolean isRoleSelection() {
		return roleSelection != null;
	}

	@Override
	protected JPanel initMainPanel() {
		List<ComponentDefinition> components = SBOLUtils.getCDOfRole(doc, isRoleSelection() ? part : ALL_PARTS);
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
			if (importSubparts.isSelected()) {
				return doc.createRecursiveCopy(comp);
			} else {
				// only insert the CD and it's sequence (if it exists)
				SBOLDocument newDoc = new SBOLDocument();
				if (comp.getSequenceByEncoding(Sequence.IUPAC_DNA) != null) {
					newDoc.createCopy(comp.getSequenceByEncoding(Sequence.IUPAC_DNA));
				}
				// remove all dependencies
				comp.clearSequenceConstraints();
				comp.clearSequenceAnnotations();
				comp.clearComponents();

				newDoc.createCopy(comp);
				return newDoc;
			}
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
