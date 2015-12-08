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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openrdf.model.URI;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import com.clarkparsia.swing.FormBuilder;
import com.clarkparsia.versioning.Infos;
import com.clarkparsia.versioning.PersonInfo;
import com.clarkparsia.versioning.sparql.Terms;
import com.google.common.base.Strings;

public enum UserInfoTab implements PreferencesTab {
	INSTANCE;
	
	private JTextField name;
	private JTextField email;
	private JTextField uri;

	@Override
	public String getTitle() {
		return "User";
	}

	@Override
	public String getDescription() {
		return "User information added to designs";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("user.gif"));
	}

	@Override
	public Component getComponent() {
		PersonInfo info = SBOLEditorPreferences.INSTANCE.getUserInfo();
		FormBuilder builder = new FormBuilder();
		name = builder.addTextField("Full name", info == null ? null : info.getName());
		email = builder.addTextField("Email", info == null || info.getEmail() == null ? null : info.getEmail()
		                .getLocalName());
		uri = builder.addTextField("URI [Optional]", info == null ? null : info.getURI().stringValue());
		JPanel formPanel = builder.build();
						
		JButton deleteInfo = new JButton("Delete user info");
		deleteInfo.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				SBOLEditorPreferences.INSTANCE.saveUserInfo(null);
				name.setText(null);
				email.setText(null);
				uri.setText(null);
			}
		});
		deleteInfo.setAlignmentX(Component.RIGHT_ALIGNMENT);
		deleteInfo.setEnabled(info != null);
		
		Box buttonPanel = Box.createHorizontalBox();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(deleteInfo);
		
		JPanel p = new JPanel(new BorderLayout());			
		p.add(formPanel, BorderLayout.NORTH);
		p.add(buttonPanel, BorderLayout.SOUTH);

		return p;
	}

	@Override
	public void save() {
		boolean noURI = Strings.isNullOrEmpty(uri.getText());
		boolean noName = Strings.isNullOrEmpty(name.getText());
		boolean noEmail = Strings.isNullOrEmpty(email.getText());
		if (!(noURI && noName && noEmail)) {
			URI personURI = noURI ? Terms.unique("Person") : Terms.uri(uri.getText());
			String personName = noName ? null : name.getText();
			URI personEmail = noEmail ? null : Terms.uri("mailto:" + email.getText());
			PersonInfo info = Infos.forPerson(personURI, personName, personEmail);
			SBOLEditorPreferences.INSTANCE.saveUserInfo(info);
		}
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}