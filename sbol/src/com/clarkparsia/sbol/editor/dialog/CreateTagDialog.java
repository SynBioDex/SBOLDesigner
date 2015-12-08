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

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.clarkparsia.swing.FormBuilder;
import com.google.common.base.Strings;

/**
 * 
 * @author Evren Sirin
 */
public class CreateTagDialog extends InputDialog<String> {
	private JTextField tagName;
	private JTextComponent tagMsg;
		
	public CreateTagDialog(final Component parent) {
		super(parent, "Create tag", RegistryType.NONE);
	}
	
	@Override
	protected void initFormPanel(FormBuilder builder) {
		tagName = builder.addTextField("Name", "");
		tagMsg = builder.addTextField("Description", "");
	}
	
	@Override
	protected void initFinished() {
		setSelectAllowed(true);
	}

	@Override
    protected String getSelection() {
    	return tagName.getText();
	}

	@Override
    protected boolean validateInput() {
		String name = tagName.getText();
		if (Strings.isNullOrEmpty(name)) {
			JOptionPane.showMessageDialog(getParent(), "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
    }
	
	public String getTagMessage() {
		return tagMsg.getText();
	}
}
