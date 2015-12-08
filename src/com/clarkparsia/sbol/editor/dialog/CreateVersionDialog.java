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

import javax.swing.JTextField;

import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.swing.FormBuilder;

/**
 * 
 * @author Evren Sirin
 */
public class CreateVersionDialog extends InputDialog<RVTDocumentIO> {
	private JTextField designName;
	private JTextField designMsg;
		
	public CreateVersionDialog(final Component parent) {
		super(parent, "Create version", RegistryType.VERSION);
	}
	
	@Override
	protected String initMessage() {
		return "This is your first commit for this design.\n" +
				"\n" +
				"You need to select the registry where your design will be saved\n" +
				"and provide a name and description for your design.";
	}
	
	@Override
	protected void initFormPanel(FormBuilder builder) {
		designName = builder.addTextField("Name", "");
		designMsg = builder.addTextField("Description", "");
	}
	
	@Override
	protected void initFinished() {
		setSelectAllowed(true);
	}

	@Override
    protected RVTDocumentIO getSelection() {
    	if (endpoint == null || !SPARQLUtilities.setCredentials(endpoint)) {
    		return null;
    	}
    	
		String repoName = designName.getText();
		String repoMsg = designMsg.getText();
		return RVTDocumentIO.createForNewRepo(endpoint, repoName, repoMsg);
	}
}
