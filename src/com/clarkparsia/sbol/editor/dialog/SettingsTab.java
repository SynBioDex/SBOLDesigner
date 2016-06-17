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

	private JRadioButton askUser;
	private JRadioButton overwrite;
	private JRadioButton newVersion;

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
		askUser = new JRadioButton("Ask", SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 0);
		overwrite = new JRadioButton("Overwrite", SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 1);
		newVersion = new JRadioButton("Create a new version", SBOLEditorPreferences.INSTANCE.getSaveBehavior() == 2);

		ButtonGroup group = new ButtonGroup();
		group.add(askUser);
		group.add(overwrite);
		group.add(newVersion);

		FormBuilder builder = new FormBuilder();
		builder.add("", saveLabel);
		builder.add("", askUser);
		builder.add("", overwrite);
		builder.add("", newVersion);

		return builder.build();
	}

	@Override
	public void save() {
		int saveBehavior = 0;
		if (askUser.isSelected()) {
			saveBehavior = 0;
		} else if (overwrite.isSelected()) {
			saveBehavior = 1;
		} else if (newVersion.isSelected()) {
			saveBehavior = 2;
		}
		SBOLEditorPreferences.INSTANCE.setSaveBehavior(saveBehavior);
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
