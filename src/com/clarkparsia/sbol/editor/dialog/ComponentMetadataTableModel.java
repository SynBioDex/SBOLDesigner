package com.clarkparsia.sbol.editor.dialog;

import java.util.List;

import org.sbolstack.frontend.ComponentMetadata;
import com.clarkparsia.swing.AbstractListTableModel;

class ComponentMetadataTableModel extends AbstractListTableModel<ComponentMetadata> {
	private static final String[] COLUMNS = { "URI", "Name", "Description" };
	private static final double[] WIDTHS = { 0.2, 0.2, 0.6 };

	public ComponentMetadataTableModel(List<ComponentMetadata> components) {
		super(components, COLUMNS, WIDTHS);
	}

	public Object getField(ComponentMetadata component, int col) {
		switch (col) {
		case 0:
			return component.uri;
		case 1:
			return component.name;
		case 2:
			return component.description;
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
