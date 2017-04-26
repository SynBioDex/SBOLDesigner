package edu.utah.ece.aync.sboldesigner.sbol.editor.dialog;

import java.util.List;

import org.sbolstandard.core2.Annotation;

import edu.utah.ece.aync.sboldesigner.swing.AbstractListTableModel;

public class AnnotationTableModel extends AbstractListTableModel<Annotation> {
	private static final String[] COLUMNS = { "Namespace + Prefix", "Name", "Value" };
	private static final double[] WIDTHS = { 0.15, 0.1, 0.75 };

	public AnnotationTableModel(List<Annotation> annotations) {
		super(annotations, COLUMNS, WIDTHS);
	}

	public Object getField(Annotation ann, int col) {
		switch (col) {
		case 0:
			return ann.getQName().getNamespaceURI() + ann.getQName().getPrefix();
		case 1:
			return ann.getQName().getLocalPart();
		case 2:
			if (ann.getBooleanValue() != null) {
				return ann.getBooleanValue().toString();
			} else if (ann.getDoubleValue() != null) {
				return ann.getDoubleValue().toString();
			} else if (ann.getIntegerValue() != null) {
				return ann.getIntegerValue().toString();
			} else if (ann.getStringValue() != null) {
				return ann.getStringValue();
			} else if (ann.getURIValue() != null) {
				return ann.getURIValue().toString();
			} else {
				return "No value found";
			}
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
