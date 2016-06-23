package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import com.clarkparsia.swing.FormBuilder;

public enum SettingsTab implements PreferencesTab {
	INSTANCE;

	private JRadioButton saveAskUser;
	private JRadioButton saveOverwrite;
	private JRadioButton saveNewVersion;

	private JRadioButton seqAskUser;
	private JRadioButton seqOverwrite;
	private JRadioButton seqKeep;

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
		return new ImageIcon(Images.getActionImage("settings.gif"));
	}

	@Override
	public Component getComponent() {
		JLabel saveLabel = new JLabel(
				"<html>Every time a part is edited, would you like to overwrite the old part or <br>create a new version of the part?</html>");
		// askUser is 0, overwrite is 1, and newVersion is 2
		saveAskUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 0);
		saveOverwrite = new JRadioButton("Overwrite", SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 1);
		saveNewVersion = new JRadioButton("Create a new version",
				SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 2);
		ButtonGroup saveGroup = new ButtonGroup();
		saveGroup.add(saveAskUser);
		saveGroup.add(saveOverwrite);
		saveGroup.add(saveNewVersion);

		JLabel impliedSequence = new JLabel(
				"<html>Every time the implied sequence is shorter than the original <br>sequence, would you like to overwrite or keep the original sequence?</html>");
		// askUser is 0, overwrite is 1, and keep is 2
		seqAskUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 0);
		seqOverwrite = new JRadioButton("Overwrite", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 1);
		seqKeep = new JRadioButton("Keep", SBOLEditorPreferences.INSTANCE.getSeqBehavior() == 2);
		ButtonGroup seqGroup = new ButtonGroup();
		seqGroup.add(seqAskUser);
		seqGroup.add(seqOverwrite);
		seqGroup.add(seqKeep);

		FormBuilder builder = new FormBuilder();
		builder.add("", saveLabel);
		builder.add("", saveAskUser);
		builder.add("", saveOverwrite);
		builder.add("", saveNewVersion);
		builder.add("", impliedSequence);
		builder.add("", seqAskUser);
		builder.add("", seqOverwrite);
		builder.add("", seqKeep);
		return builder.build();
	}

	@Override
	public void save() {
		int saveBehavior = 0;
		if (saveAskUser.isSelected()) {
			saveBehavior = 0;
		} else if (saveOverwrite.isSelected()) {
			saveBehavior = 1;
		} else if (saveNewVersion.isSelected()) {
			saveBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setSaveBehavior(saveBehavior);

		int seqBehavior = 0;
		if (seqAskUser.isSelected()) {
			seqBehavior = 0;
		} else if (seqOverwrite.isSelected()) {
			seqBehavior = 1;
		} else if (seqKeep.isSelected()) {
			seqBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setSeqBehavior(seqBehavior);
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
