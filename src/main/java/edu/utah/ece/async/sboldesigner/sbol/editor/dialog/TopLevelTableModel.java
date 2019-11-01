package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.util.List;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;

public class TopLevelTableModel extends AbstractListTableModel<TopLevel>{
	private static final String[] COLUMNS = { "Type", "Display Id", "Name", "Version", "Description" };
	private static final double[] WIDTHS = { 0.1, 0.2, 0.2, 0.1, 0.4 };

	public TopLevelTableModel(List<TopLevel> Objs) {
		super(Objs, COLUMNS, WIDTHS);
	}
	
	public Object getField(TopLevel obj, int col) {
		if(obj == null)
			return null;
		String type = removePrefix(obj.getClass().toString(), "class org.sbolstandard.core2.");
		switch (col) {
		case 0:
			if(type.equals("ComponentDefinition"))
				return "Part";
			else if(type.equals("CombinatorialDerivation"))
				return "Derivation";
			else
				return type;
		case 1:
			return obj.getDisplayId();
		case 2:
			return obj.getName();
		case 3:
			return obj.getVersion();
		case 4:
			return obj.getDescription();
		default:
			throw new IndexOutOfBoundsException();
		}
	}
	
	private String removePrefix(String s, String prefix)
	{
		if(s != null && s.startsWith(prefix))
		{
			return s.split(prefix)[1];
		}
		return s;
	}
}
