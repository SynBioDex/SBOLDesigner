package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import com.clarkparsia.swing.FormBuilder;

public enum SettingsTab implements PreferencesTab {
	INSTANCE;

	// askUser is 0, overwrite is 1, and keep is 2
	private JRadioButton seqAskUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 0);
	private JRadioButton seqOverwrite = new JRadioButton("Overwrite",
			SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 1);
	private JRadioButton seqKeep = new JRadioButton("Keep", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 2);

	// show name is 0, show displayId is 1
	private JRadioButton showName = new JRadioButton("Show name when set",
			SBOLEditorPreferences.INSTANCE.getNameDisplayIdBehavior() == 0);
	private JRadioButton showDisplayId = new JRadioButton("Show displayId",
			SBOLEditorPreferences.INSTANCE.getNameDisplayIdBehavior() == 1);

	@Override
	public String getTitle() {
		return "Settings";
	}

	@Override
	public String getDescription() {
		return "Miscellaneous settings";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("settings.png"));
	}

	@Override
	public Component getComponent() {
		JLabel impliedSequence = new JLabel(
				"<html>Every time the implied sequence is shorter than the original <br>sequence, would you like to overwrite or keep the original sequence?</html>");
		ButtonGroup seqGroup = new ButtonGroup();
		seqGroup.add(seqAskUser);
		seqGroup.add(seqOverwrite);
		seqGroup.add(seqKeep);

		JLabel showNameOrDisplayId = new JLabel("<html>Always show displayId or always show name when set?</html>");
		ButtonGroup nameDisplayIdGroup = new ButtonGroup();
		nameDisplayIdGroup.add(showName);
		nameDisplayIdGroup.add(showDisplayId);

		FormBuilder builder = new FormBuilder();
		builder.add("", impliedSequence);
		builder.add("", seqAskUser);
		builder.add("", seqOverwrite);
		builder.add("", seqKeep);
		builder.add("", showNameOrDisplayId);
		builder.add("", showName);
		builder.add("", showDisplayId);

		return builder.build();
	}

	@Override
	public void save() {
		int seqBehavior = 0;
		if (seqAskUser.isSelected()) {
			seqBehavior = 0;
		} else if (seqOverwrite.isSelected()) {
			seqBehavior = 1;
		} else if (seqKeep.isSelected()) {
			seqBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setSeqBehavior(seqBehavior);

		int showNameOrDisplayId = 0;
		if (showName.isSelected()) {
			showNameOrDisplayId = 0;
		} else if (showDisplayId.isSelected()) {
			showNameOrDisplayId = 1;
		}
		SBOLEditorPreferences.INSTANCE.setNameDisplayIdBehavior(showNameOrDisplayId);
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
