package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.utah.ece.async.sboldesigner.sbol.editor.Images;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum SettingsTab implements PreferencesTab {
	INSTANCE;
	private boolean requiresRestart = false;
	// askUser is 0, overwrite is 1, and keep is 2
	private JRadioButton seqAskUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 0);
	private JRadioButton seqOverwrite = new JRadioButton("Overwrite",
			SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 1);
	private JRadioButton seqKeep = new JRadioButton("Keep", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 2);
	
	// askUser is 0, overwrite is 1, and keep is 2
	private JRadioButton missingAskUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getMissingBehavior() == 0);
	private JRadioButton missingOverwrite = new JRadioButton("Overwrite",
			SBOLEditorPreferences.INSTANCE.getMissingBehavior() == 1);
	private JRadioButton missingKeep = new JRadioButton("Keep", SBOLEditorPreferences.INSTANCE.getMissingBehavior() == 2);

	// show name is 0, show displayId is 1
	private JRadioButton showName = new JRadioButton("Show name when set",
			SBOLEditorPreferences.INSTANCE.getNameDisplayIdBehavior() == 0);
	private JRadioButton showDisplayId = new JRadioButton("Show displayId",
			SBOLEditorPreferences.INSTANCE.getNameDisplayIdBehavior() == 1);
	
	// regular file chooser is 0, mac file chooser is 1
	private JRadioButton defaultFileChooser = new JRadioButton("Default file chooser",
			SBOLEditorPreferences.INSTANCE.getFileChooserBehavior() == 0);
	private JRadioButton macFileChooser = new JRadioButton("Mac file chooser",
			SBOLEditorPreferences.INSTANCE.getFileChooserBehavior() == 1);

	private JRadioButton defaultCDS = new JRadioButton("Default CDS Glyph",
			SBOLEditorPreferences.INSTANCE.getCDSBehavior() == 0);
	private JRadioButton arrowCDS = new JRadioButton("Arrow CDS Glyph",
			SBOLEditorPreferences.INSTANCE.getCDSBehavior() == 1);
	
	private JTextField queryLimit = new JTextField(SBOLEditorPreferences.INSTANCE.getQueryLimit().toString());
	
	@Override
	public String getTitle() {
		return "Designer";
	}

	@Override
	public String getDescription() {
		return "Miscellaneous settings";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("sbol.jpg"));
	}

	@Override
	public Component getComponent() {
		JLabel impliedSequence = new JLabel(
				"<html>Every time the implied sequence is different than the original <br>sequence, would you like to overwrite or keep the original sequence?</html>");
		ButtonGroup seqGroup = new ButtonGroup();
		seqGroup.add(seqAskUser);
		seqGroup.add(seqOverwrite);
		seqGroup.add(seqKeep);
		
		JLabel impliedMissingSequence = new JLabel(
				"<html>Every time the implied sequence has missing <br>sequences, would you like to overwrite or keep the original sequence?</html>");
		ButtonGroup missingGroup = new ButtonGroup();
		missingGroup.add(missingAskUser);
		missingGroup.add(missingOverwrite);
		missingGroup.add(missingKeep);

		JLabel showNameOrDisplayId = new JLabel("<html>Always show displayId or always show name when set?</html>");
		ButtonGroup nameDisplayIdGroup = new ButtonGroup();
		nameDisplayIdGroup.add(showName);
		nameDisplayIdGroup.add(showDisplayId);

		JLabel macOrDefaultFileChooser = new JLabel("<html>Use the default or Mac file chooser?</html>");
		ButtonGroup macOrDefaultFileChooserGroup = new ButtonGroup();
		macOrDefaultFileChooserGroup.add(defaultFileChooser);
		macOrDefaultFileChooserGroup.add(macFileChooser);
		
		JLabel arrowOrDefault = new JLabel("<html>Use default or arrow CDS?</html>");
		ButtonGroup arrowOrDefaultGroup = new ButtonGroup();
		arrowOrDefaultGroup.add(defaultCDS);
		arrowOrDefaultGroup.add(arrowCDS);
		
		JLabel queryLimitLabel = new JLabel("<html>Set the query limit. Default & max is 10,000.</html>");
		
		FormBuilder builder = new FormBuilder();
		builder.add("", impliedSequence);
		builder.add("", seqAskUser);
		builder.add("", seqOverwrite);
		builder.add("", seqKeep);
		builder.add("", impliedMissingSequence);
		builder.add("", missingAskUser);
		builder.add("", missingOverwrite);
		builder.add("", missingKeep);
		builder.add("", showNameOrDisplayId);
		builder.add("", showName);
		builder.add("", showDisplayId);
		builder.add("", macOrDefaultFileChooser);
		builder.add("", defaultFileChooser);
		builder.add("", macFileChooser);
		builder.add("", arrowOrDefault);
		builder.add("", defaultCDS);
		builder.add("", arrowCDS);
		builder.add("", queryLimitLabel);
		builder.add("", queryLimit);

		return builder.build();
	}

	@Override
	public boolean save() {
		int seqBehavior = 0;
		if (seqAskUser.isSelected()) {
			seqBehavior = 0;
		} else if (seqOverwrite.isSelected()) {
			seqBehavior = 1;
		} else if (seqKeep.isSelected()) {
			seqBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setSeqBehavior(seqBehavior);
		
		int missingBehavior = 0;
		if (missingAskUser.isSelected()) {
			missingBehavior = 0;
		} else if (missingOverwrite.isSelected()) {
			missingBehavior = 1;
		} else if (missingKeep.isSelected()) {
			missingBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setMissingBehavior(missingBehavior);

		int showNameOrDisplayId = 0;
		if (showName.isSelected()) {
			showNameOrDisplayId = 0;
		} else if (showDisplayId.isSelected()) {
			showNameOrDisplayId = 1;
		}
		SBOLEditorPreferences.INSTANCE.setNameDisplayIdBehavior(showNameOrDisplayId);
		
		int macOrDefault = 0;
		if (defaultFileChooser.isSelected()) {
			macOrDefault = 0;
		} else if (macFileChooser.isSelected()) {
			macOrDefault = 1;
		}
		SBOLEditorPreferences.INSTANCE.setFileChooserBehavior(macOrDefault);
		
		int qLimit = Integer.parseInt(this.queryLimit.getText());
		if(qLimit > 0 && qLimit <10000)
		{
			SBOLEditorPreferences.INSTANCE.setQueryLimit(qLimit);
			this.queryLimit.setText(String.valueOf(qLimit));
		}else {
			SBOLEditorPreferences.INSTANCE.setQueryLimit(10000);
			this.queryLimit.setText("10000");
		}
		SynBioHubQuery.QUERY_LIMIT = SBOLEditorPreferences.INSTANCE.getQueryLimit();
		
		int arrowOrDefault = 0;
		if (defaultCDS.isSelected()) {
			arrowOrDefault = 0;
		} else if (arrowCDS.isSelected()) {
			arrowOrDefault = 1;
		}
		if(SBOLEditorPreferences.INSTANCE.getCDSBehavior() != arrowOrDefault) {
			requiresRestart = true;
		}
		SBOLEditorPreferences.INSTANCE.setCDSBehavior(arrowOrDefault);
		
		return true;
	}

	@Override
	public boolean requiresRestart() {
		if(this.requiresRestart) {
			this.requiresRestart = false;
			return true;
		}else {
			return false;
		}
	}
}
