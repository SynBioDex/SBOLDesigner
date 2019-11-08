package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

import com.google.common.collect.Lists;

import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.CombinatorialDerivationInputDialog;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public class ComboDerivDialog extends InputDialog<CombinatorialDerivation> {
	private static final String TITLE = "Select a root design to open";

	private JTable table;
	private JLabel tableLabel;
	private JButton deleteCD;
	private SBOLDocument doc;

	private ComponentDefinition root;

	/**
	 * this.getInput() returns an SBOLDocument with a single rootCD selected
	 * from the rootCDs in doc.
	 * 
	 * Root will reference the root CD that was selected.
	 */
	public ComboDerivDialog(final Component parent, SBOLDocument doc, ComponentDefinition root) {
		super(parent, TITLE);

		this.doc = doc;
		this.root = root;
	}

	@Override
	protected String initMessage() {
		return "There are multiple combinatorial derivations. Which would you like to enumerate?";
	}

	@Override
	public void initFormPanel(FormBuilder builder) {


		deleteCD = new JButton("Delete selected derivations. (This will resave the file)");
		deleteCD.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int[] rows = table.getSelectedRows();
					for (int row : rows) {
						row = table.convertRowIndexToModel(row);
						TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row);
						doc.removeCombinatorialDerivation((CombinatorialDerivation)comp);
					}
					//File file = SBOLUtils.setupFile();
					//SBOLWriter.write(doc, new FileOutputStream(file));
					updateTable();
				} catch (Exception e1) {
					MessageDialog.showMessage(rootPane, "Failed to delete CombinatorialDerivation: ", e1.getMessage());
					e1.printStackTrace();
				}
			}
		});
		builder.add("", deleteCD);
	}

	@Override
	protected JPanel initMainPanel() {
		List<TopLevel> derivations = new ArrayList<TopLevel>();

		for (CombinatorialDerivation derivation : doc.getCombinatorialDerivations()) {
			if (root == null || derivation.getTemplate().equals(root)) {
				derivations.add(derivation);
			}
		}

		TopLevelTableModel tableModel = new TopLevelTableModel(derivations);
		JPanel panel = createTablePanel(tableModel, "Matching derivations (" + tableModel.getRowCount() + ")");
		table = (JTable) panel.getClientProperty("table");
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableLabel = (JLabel) panel.getClientProperty("label");

		updateTable();

		return panel;
	}

	@Override
	protected CombinatorialDerivation getSelection() {
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		TopLevel deriv = ((TopLevelTableModel) table.getModel()).getElement(row);
		CombinatorialDerivation comb = (CombinatorialDerivation)deriv;
		return comb;
	}


	private void updateTable() {
		List<TopLevel> derivations = new ArrayList<TopLevel>();
		derivations.addAll(doc.getCombinatorialDerivations());
		
		((TopLevelTableModel) table.getModel()).setElements(derivations);
		tableLabel.setText("Derivations (" + derivations.size() + ")");
	}

}