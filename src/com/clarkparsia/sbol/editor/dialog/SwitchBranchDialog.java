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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;

import com.clarkparsia.versioning.Branch;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Evren Sirin
 */
public class SwitchBranchDialog extends InputDialog<Branch> {
	private Branch currentBranch;
	
	private RefTableModel<Branch> tableModel;
	private JTable table;
	
	public SwitchBranchDialog(final Component parent, final Branch currentBranch) {
		super(parent, "Switch", RegistryType.NONE);
		
		this.currentBranch = currentBranch;
	}
	
	@Override
	protected JPanel initMainPanel() {	
		tableModel = new RefTableModel<Branch>(branches());
		
		JPanel panel = createTablePanel(tableModel, "Select a branch");

		table = (JTable) panel.getClientProperty("table");
		
		return panel;
	}

    protected Branch getSelection() {
    	if (table.getSelectedRow() < 0) {
    		return null;
    	}
    	int row = table.convertRowIndexToModel(table.getSelectedRow());
		return tableModel.getElement(row);
	}
    
    private List<Branch> branches() {
    	List<Branch> branches = ImmutableList.<Branch>of();    	
    	try {
    		branches = currentBranch.getRepository().branches().list();
    		branches.remove(currentBranch);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
    	return branches;
    }
}
