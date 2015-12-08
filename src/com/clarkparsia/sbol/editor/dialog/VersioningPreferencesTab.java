/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import com.clarkparsia.swing.FormBuilder;

public enum VersioningPreferencesTab implements PreferencesTab {
	INSTANCE;
	
	private JCheckBox versioningEnabled;
	private JCheckBox branchingEnabled;

	@Override
	public String getTitle() {
		return "Versioning";
	}

	@Override
	public String getDescription() {
		return "Versioning preferences";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("repository.gif"));
	}

	@Override
	public Component getComponent() {
		branchingEnabled = new JCheckBox("Enable branching", SBOLEditorPreferences.INSTANCE.isBranchingEnabled());
		versioningEnabled = new JCheckBox("Enable versioning", SBOLEditorPreferences.INSTANCE.isVersioningEnabled());

		branchingEnabled.setEnabled(versioningEnabled.isSelected());

		versioningEnabled.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				branchingEnabled.setEnabled(versioningEnabled.isSelected());
			}
		});
		
		FormBuilder builder = new FormBuilder();
		builder.add("", versioningEnabled);
		builder.add("", branchingEnabled);
		
		return builder.build();
	}

	@Override
	public void save() {
		if (requiresRestart()) {
			SBOLEditorPreferences.INSTANCE.setVersioningEnabled(versioningEnabled.isSelected());
			SBOLEditorPreferences.INSTANCE.setBranchingEnabled(branchingEnabled.isSelected());
		}
	}

	@Override
	public boolean requiresRestart() {
		return (versioningEnabled.isSelected() != SBOLEditorPreferences.INSTANCE.isVersioningEnabled())
			|| (branchingEnabled.isSelected() != SBOLEditorPreferences.INSTANCE.isBranchingEnabled());
	}
}