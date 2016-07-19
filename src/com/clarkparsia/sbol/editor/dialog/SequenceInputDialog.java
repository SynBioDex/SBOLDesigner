package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.Sequence;

import com.clarkparsia.swing.FormBuilder;

import scala.Array;

/**
 * A GUI for choosing a Sequence (String of elements) from a set of Sequences
 * 
 * @author Michael Zhang
 */
public class SequenceInputDialog extends InputDialog<String> {
	private static final String TITLE = "Select a sequence to import";

	private List<Sequence> sequences;

	private JTable table;
	private JLabel tableLabel;

	public SequenceInputDialog(final Component parent, Set<Sequence> sequences) {
		// TODO add role/type selection here too?
		super(parent, TITLE);

		ArrayList<Sequence> list = new ArrayList<Sequence>();
		for (Sequence s : sequences) {
			list.add(s);
		}
		this.sequences = list;
	}

	@Override
	public void initFormPanel(FormBuilder builder) {
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

		builder.add("Filter sequences", filterSelection);
	}

	@Override
	protected JPanel initMainPanel() {
		SequenceTableModel tableModel = new SequenceTableModel(sequences);

		JPanel panel = createTablePanel(tableModel, "Matching sequences (" + tableModel.getRowCount() + ")");

		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");

		return panel;
	}

	@Override
	protected String getSelection() {
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		Sequence seq = ((SequenceTableModel) table.getModel()).getElement(row);
		return seq.getElements();
	}

	private void updateFilter(String filterText) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TableRowSorter<SequenceTableModel> sorter = (TableRowSorter) table.getRowSorter();
		if (filterText.length() == 0) {
			sorter.setRowFilter(null);
		} else {
			try {
				RowFilter<SequenceTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
				sorter.setRowFilter(rf);
			} catch (java.util.regex.PatternSyntaxException e) {
				sorter.setRowFilter(null);
			}
		}

		tableLabel.setText("Matching sequences (" + sorter.getViewRowCount() + ")");
	}
}
