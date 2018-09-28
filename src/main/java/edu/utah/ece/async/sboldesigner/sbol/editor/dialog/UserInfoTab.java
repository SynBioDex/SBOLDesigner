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

package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openrdf.model.URI;

import com.google.common.base.Strings;

import edu.utah.ece.async.sboldesigner.sbol.editor.Images;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.Infos;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;
import edu.utah.ece.async.sboldesigner.versioning.Terms;

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
		return new ImageIcon(Images.getActionImage("user.png"));
	}

	@Override
	public Component getComponent() {
		PersonInfo info = SBOLEditorPreferences.INSTANCE.getUserInfo();
		FormBuilder builder = new FormBuilder();
		name = builder.addTextField("Full name", info == null ? null : info.getName());
		email = builder.addTextField("Email",
				info == null || info.getEmail() == null ? null : info.getEmail().getLocalName());
		uri = builder.addTextField("Owner's domain [required]", info == null ? null : info.getURI().stringValue());
		JPanel formPanel = builder.build();

		JButton deleteInfo = new JButton("Delete user info");
		deleteInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PersonInfo userInfo = Infos.forPerson(uri.getText());
				SBOLEditorPreferences.INSTANCE.saveUserInfo(userInfo);
				name.setText(null);
				email.setText(null);
				uri.setText("http://dummy.org/");
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
	public boolean save() {
		boolean noURI = Strings.isNullOrEmpty(uri.getText());
		boolean noName = Strings.isNullOrEmpty(name.getText());
		boolean noEmail = Strings.isNullOrEmpty(email.getText());

		if (hasNamespaceCollision(uri.getText())) {
			JOptionPane.showMessageDialog(getComponent(),
					"The user's domain namespace cannot conflict with an existing Registry namespace.\n"
							+ "Please enter a valid domain for your organization (ex. http://dummy.org/).");
			return false;
		}
		if (!isURIprefixCompliant(uri.getText())) {
			JOptionPane.showMessageDialog(getComponent(), "Invalid URI provided for the domain.\n"
					+ "Please enter a valid domain for your organization (ex. http://dummy.org/).");
			return false;
		}

		URI personURI = noURI ? Terms.uri("http://dummy.org/") : Terms.uri(uri.getText());
		String personName = noName ? "" : name.getText();
		URI personEmail = noEmail ? null : Terms.uri("mailto:" + email.getText());
		PersonInfo info = Infos.forPerson(personURI, personName, personEmail);
		SBOLEditorPreferences.INSTANCE.saveUserInfo(info);
		return true;
	}

	private boolean hasNamespaceCollision(String newNamespace) {
		for (Registry r : Registries.get()) {
			if (!r.isPath()) {

				ArrayList<String> variations = new ArrayList<>();
				variations.add(r.getLocation());
				if (r.getLocation().startsWith("https")) {
					variations.add(r.getLocation().replace("https", "http"));
				} else {
					variations.add(r.getLocation().replace("http", "https"));
				}

				for (String variation : variations) {
					if (newNamespace.startsWith(variation)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}

	private static final String delimiter = "[/|#|:]";

	private static final String protocol = "(?:https?|ftp|file)://";

	private static final String URIprefixPattern = "\\b(?:" + protocol
			+ ")?[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	private static final Pattern URIprefixPatternPat = Pattern.compile(URIprefixPattern + delimiter);

	private static boolean isURIprefixCompliant(String URIprefix) {
		Matcher m = URIprefixPatternPat.matcher(URIprefix);
		return m.matches();
	}

}