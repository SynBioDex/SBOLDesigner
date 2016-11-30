package com.clarkparsia.sbol.editor.dialog;

import java.util.List;

import org.sbolstack.frontend.IdentifiedMetadata;
import com.clarkparsia.swing.AbstractListTableModel;

class TableMetadataTableModel extends AbstractListTableModel<TableMetadata> {
	private static final String[] COLUMNS = { "Type", "Display Id", "Name", "Version", "Description" };
	private static final double[] WIDTHS = { 0.1, 0.2, 0.2, 0.1, 0.4 };

	public TableMetadataTableModel(List<TableMetadata> components) {
		super(components, COLUMNS, WIDTHS);
	}

	public Object getField(TableMetadata component, int col) {
		switch (col) {
		case 0:
			return component.isCollection ? "Collection" : "Part";
		case 1:
			return component.identified.displayId;
		case 2:
			return component.identified.name;
		case 3:
			return component.identified.version;
		case 4:
			return component.identified.description;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
