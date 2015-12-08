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
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;

import com.clarkparsia.sbol.SBOLUtils;
import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.sbol.editor.io.ReadOnlyDocumentIO;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.ui.HistoryList;
import com.clarkparsia.versioning.ui.HistoryTable;
import com.clarkparsia.versioning.ui.HistoryTable.HistoryTableModel;

/**
 * 
 * @author Evren Sirin
 */
public class HistoryDialog {
	private enum Action { CHECKOUT, CLONE, CLOSE }

	public static DocumentIO show(Component parent, RVTDocumentIO documentIO) {
		final AtomicReference<DocumentIO> result = new AtomicReference<DocumentIO>();
		
		final Revision head = documentIO.getBranch().getHead();

		final JCheckBox showBranches = new JCheckBox("Show branch nodes");
		showBranches.setSelected(true);

		final HistoryList historyList = new HistoryList(head, showBranches.isSelected());
		final HistoryTable table = new HistoryTable(new HistoryTableModel(historyList));
		
		showBranches.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				table.setModel(new HistoryTableModel(new HistoryList(head, showBranches.isSelected())));
			}
		});			
		
		final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parent), "History for " + documentIO, true);		
		
	    ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Action action = Action.valueOf(e.getActionCommand());
				if (action != Action.CLOSE) {
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					Ref ref = historyList.get(row).getRef();
					if (ref instanceof Branch) {
						JOptionPane.showMessageDialog(null, "Please select a revision not a branch");
						return;
					}
					
					Revision rev = (Revision) ref;
					
					if (action == Action.CLONE) {
						try {
	                        SBOLDocument doc = RVTDocumentIO.createForRevision(rev).read();
	                        DnaComponent comp = SBOLUtils.getRootComponent(doc);
	                        SBOLUtils.rename(comp);
	                        result.set(new ReadOnlyDocumentIO(doc));
                        }
                        catch (Exception e1) {
	                        JOptionPane.showMessageDialog(null, "ERROR: " + e1.getMessage());
                        }
					}
					else {
						result.set(RVTDocumentIO.createForRevision(rev));
					}
				}
				
				dialog.setVisible(false);
			}
	    };
	    
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		JButton checkoutButton = new JButton("Checkout");
		checkoutButton.setActionCommand(Action.CHECKOUT.toString());
		checkoutButton.addActionListener(actionListener);
		checkoutButton.setToolTipText("Checkout the selected revision and replace the current design");
		buttonPanel.add(checkoutButton);

		JButton cloneButton = new JButton("Clone");
		cloneButton.setActionCommand(Action.CLONE.toString());
		cloneButton.addActionListener(actionListener);
		cloneButton.setToolTipText("Clone the selected revision and insert into the current design");
		dialog.getRootPane().setDefaultButton(checkoutButton);
		buttonPanel.add(cloneButton);
		
		buttonPanel.add(Box.createHorizontalStrut(200));
		buttonPanel.add(Box.createHorizontalGlue());
		
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand(Action.CLOSE.toString());
		closeButton.addActionListener(actionListener);
		dialog.getRootPane().setDefaultButton(closeButton);
		closeButton.registerKeyboardAction(actionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		buttonPanel.add(closeButton);
		
		dialog.getContentPane().add(showBranches, BorderLayout.NORTH);
		dialog.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		
		return result.get();
	}
	
}
