package com.clarkparsia.sbol.editor.dialog;

import java.util.List;

import org.sbolstack.frontend.IdentifiedMetadata;
import com.clarkparsia.swing.AbstractListTableModel;

class IdentifiedMetadataTableModel extends AbstractListTableModel<IdentifiedMetadata> {
	private static final String[] COLUMNS = { "Display Id", "Name", "Version", "Description" };
	private static final double[] WIDTHS = { 0.2, 0.2, 0.1, 0.5 };

	public IdentifiedMetadataTableModel(List<IdentifiedMetadata> components) {
		super(components, COLUMNS, WIDTHS);
	}

	public Object getField(IdentifiedMetadata component, int col) {
		switch (col) {
		case 0:
			return component.displayId;
		case 1:
			return component.name;
		case 2:
			return component.version;
		case 3:
			return component.description;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
