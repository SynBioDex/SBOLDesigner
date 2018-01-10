package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.util.List;

import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;

class TableMetadataTableModel extends AbstractListTableModel<TableMetadata> {
	private static final String[] COLUMNS = { "Type", "Display Id", "Name", "Version", "Description" };
	private static final double[] WIDTHS = { 0.1, 0.2, 0.2, 0.1, 0.4 };

	public TableMetadataTableModel(List<TableMetadata> components) {
		super(components, COLUMNS, WIDTHS);
	}

	public Object getField(TableMetadata component, int col) {
		String type;
		if (component.isCollection) {
			type = "Collection";
		} else if (component.identified.getType()==null) {
			type = "Part";
		} else if (component.identified.getType().endsWith("Collection")) {
			type = "Collection";
		} else if (component.identified.getType().endsWith("ComponentDefinition")) {
			type = "Part";
		} else if (component.identified.getType().endsWith("ModuleDefinition")) {
			type = "Design";
		} else if (component.identified.getType().endsWith("Model")) {
			type = "Model";
		} else if (component.identified.getType().endsWith("Sequence")) {
			type = "Sequence";
		} else if (component.identified.getType().endsWith("Attachment")) {
			type = "Attachment";
		} else if (component.identified.getType().endsWith("Implementation")) {
			type = "Implementation";
		} else if (component.identified.getType().endsWith("CombinatorialDerivation")) {
			type = "Derivation";
		} else {
			type = "Other";
		}
		switch (col) {
		case 0:
			return type;
		case 1:
			return component.identified.getDisplayId();
		case 2:
			return component.identified.getName();
		case 3:
			return component.identified.getVersion();
		case 4:
			return component.identified.getDescription();
		default:
			throw new IndexOutOfBoundsException();
		}
	}
}
