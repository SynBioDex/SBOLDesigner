package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.util.List;

import org.sbolstandard.core2.Sequence;

import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;

class SequenceTableModel extends AbstractListTableModel<Sequence> {
	private static final String[] COLUMNS = { "Display Id", "Name", "Description", "Elements" };
	private static final double[] WIDTHS = { 0.2, 0.2, 0.2, 0.4 };

	public SequenceTableModel(List<Sequence> sequences) {
		super(sequences, COLUMNS, WIDTHS);
	}

	public Object getField(Sequence seq, int col) {
		switch (col) {
		case 0:
			return seq.getDisplayId();
		case 1:
			return seq.getName();
		case 2:
			return seq.getDescription();
		case 3:
			return seq.getElements();
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
