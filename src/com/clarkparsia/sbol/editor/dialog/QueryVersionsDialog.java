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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.sbolstandard.core.DnaComponent;

import com.clarkparsia.swing.InvisibleSplitPane;

/**
 * 
 * @author Evren Sirin
 */
public class QueryVersionsDialog extends InputDialog<DnaComponent> {
	private static final String TITLE = "Query versions";
	
	private static class ExampleQuery {
		private final String name;
		private final String query;
		public ExampleQuery(String name, String query) {
	        super();
	        this.name = name;
	        this.query = query;
        }
		@Override
        public String toString() {
	        return name;
        }		
		
	}
	
	private static final ExampleQuery[] QUERIES = {
		new ExampleQuery("Example queries", ""),
		new ExampleQuery("Retrieve all metadata", "SELECT * \nFROM <tag:rvt:Metadata>\n{\n   ?x a ?y\n}"),
		new ExampleQuery("List all designs", 
				"PREFIX so: <http://purl.obolibrary.org/obo/>\n" +
				"PREFIX rvt: <tag:rvt:>\n" +
				"PREFIX : <http://sbols.org/v1#>\n" +
				"\n" +
				"SELECT ?design ?description\n" +
				"FROM <tag:rvt:Metadata>\n" +
				"{\n" +
				"   ?x a rvt:Repository ;\n" +
				"      rdfs:label ?design ;\n" +
				"      rdfs:comment ?description\n" +
				"}"),
		new ExampleQuery("List all designs with component B0010", 
						"PREFIX so: <http://purl.obolibrary.org/obo/>\n" + 
						"PREFIX rvt: <tag:rvt:>\n" + 
						"PREFIX part: <http://partsregistry.org/part/>PREFIX : <http://sbols.org/v1#>\n" + 
						"\n" + 
						"SELECT ?design ?description (?g as ?revision)\n" + 
						"{\n" + 
						" GRAPH <tag:rvt:Metadata> {\n" + 
						"   ?x a rvt:Repository ;\n" + 
						"      rdfs:label ?design ;\n" + 
						"      rdfs:comment ?description ;\n" + 
						"      rvt:hasBranch ?b .\n" + 
						"   ?b rvt:hasHead ?g .\n" + 
						" }\n" + 
						" GRAPH ?g {\n" + 
						"   ?y :subComponent part:BBa_B0010\n" +
						" }\n" +
						"}"),				
	};
	
	private JTable table;
	
	public QueryVersionsDialog(final Component parent) {
		super(parent, TITLE, RegistryType.VERSION);
	}
	
	@Override
	protected JPanel initMainPanel() {
		final JTextArea queryField = new JTextArea(8, 50);
		queryField.setText("SELECT * \nFROM <tag:rvt:Metadata>\n{\n   ?x a ?y\n}");
		
		final JTable resultTable = new JTable(new DefaultTableModel());
		
		final JComboBox examplesBox = new JComboBox(QUERIES);
		examplesBox.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ExampleQuery q = (ExampleQuery) examplesBox.getSelectedItem();
				if (q!= null) {
					queryField.setText(q.query);	
					((DefaultTableModel) resultTable.getModel()).setRowCount(0);
				}
			}
		});
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				queryField.setText("");	
				((DefaultTableModel) resultTable.getModel()).setRowCount(0);
			}
		});
		
		JButton execButton = new JButton("Execute");
		execButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String queryStr = queryField.getText();
				try {
	                endpoint.executeSelectQuery(queryStr, new TupleQueryResultHandler() {	
	                	DefaultTableModel model; 
	                	List<String> vars;
	                	
	                	@Override
	                	public void startQueryResult(List<String> vars) throws TupleQueryResultHandlerException {
	                		this.vars = vars;
	                		model = new DefaultTableModel(vars.toArray(), 0);
	                	}
	                	
	                	@Override
	                	public void handleSolution(BindingSet b) throws TupleQueryResultHandlerException {
	                		Object[] row = new Object[vars.size()];
	                		for (int i = 0; i < vars.size(); i++) {
	                            String var = vars.get(i);
		                		row[i] = b.getValue(var).stringValue();   
                            }
	                		model.addRow(row);
	                	}
	                	
	                	@Override
	                	public void endQueryResult() throws TupleQueryResultHandlerException {
	                		resultTable.setModel(model);
	                	}
	                });
                }
                catch (QueryEvaluationException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
			}
		});
		
		Box buttonPanel = Box.createHorizontalBox();
		buttonPanel.add(examplesBox);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(clearButton);
		buttonPanel.add(execButton);
		
		InvisibleSplitPane splitPane = new InvisibleSplitPane(InvisibleSplitPane.VERTICAL_SPLIT, new JScrollPane(queryField), new JScrollPane(resultTable));
		splitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		splitPane.setDividerLocation(0.8);
		splitPane.setResizeWeight(1);

		JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(buttonPanel, BorderLayout.NORTH);
		panel.add(splitPane, BorderLayout.CENTER);
		
		return panel;
	}

	@Override
    protected DnaComponent getSelection() {
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		DnaComponent comp = ((DnaComponentTableModel) table.getModel()).getElement(row);
		return comp;
	}
}
