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

package com.clarkparsia.versioning.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import com.clarkparsia.sbol.editor.sparql.LocalEndpoint;
import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Infos;
import com.clarkparsia.versioning.PersonInfo;
import com.clarkparsia.versioning.RVT;
import com.clarkparsia.versioning.RVTFactory;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.sparql.Terms;
import com.clarkparsia.versioning.ui.HistoryTable.HistoryTableModel;

public class TestUI {
	static String url = "http://localhost:5822/rvt";
	static PersonInfo user = Infos.forPerson("urn:Evren", "Evren Sirin", "mailto:evren@sirin.org");
	static SPARQLEndpoint endpoint;

	public static void main(String[] args) throws Exception {
		endpoint = args.length == 0 ? new LocalEndpoint() : new StardogEndpoint(url);
		String repoName = args.length == 0 ? "cmy" : args[0];
		final Revision head = rev(repoName);

		final JCheckBox showBranches = new JCheckBox("Show branch nodes");
		showBranches.setSelected(true);

		final HistoryTable table = new HistoryTable(new HistoryTableModel(new HistoryList(head, showBranches.isSelected())));
		
		showBranches.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				table.setModel(new HistoryTableModel(new HistoryList(head, showBranches.isSelected())));
			}
		});
		
		JFrame frame = new JFrame("History for " + head.getBranch().getRepository().getName());
		frame.getContentPane().add(showBranches, BorderLayout.NORTH);
		frame.getContentPane().add(new JScrollPane(table));
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
//	public static JComponent createJGraph(Revision head) {
//		mxGraph graph = new mxGraph();
//		Object parent = graph.getDefaultParent();
//
//		Map<URI, Object> nodes = Maps.newHashMap();
//		graph.getModel().beginUpdate();
//		try {
//			Queue<Revision> pending = Lists.newLinkedList();
//			pending.add(head);
//
//			while (!pending.isEmpty()) {
//				Revision rev = pending.remove();
//				Object vertex = nodes.get(rev.getURI());
//				if (vertex == null) {
//					vertex = graph.insertVertex(parent, null, rev.getActionInfo().getMessage(), 40, 0, 120, 30);
//					nodes.put(rev.getURI(), vertex);
//				}
//				for (Revision parentRev : rev.getParents()) {
//					Object parentVertex = nodes.get(parentRev.getURI());
//					if (parentVertex == null) {
//						parentVertex = graph
//						                .insertVertex(parent, null, parentRev.getActionInfo().getMessage(), 40, 0, 120, 30);
//						nodes.put(parentRev.getURI(), parentVertex);
//					}
//					graph.insertEdge(parent, null, null, parentVertex, vertex);
//					pending.add(parentRev);
//				}
//			}
//		}
//		finally {
//			graph.getModel().endUpdate();
//		}
//
//		mxIGraphLayout layout = new mxHierarchicalLayout(graph);
//		layout.execute(graph.getDefaultParent());
//
//		mxGraphComponent graphComponent = new mxGraphComponent(graph);
//		return graphComponent;
//	}
	
	public static ActionInfo info(String msg) {
		ActionInfo info = Infos.forAction(user, msg);
		System.out.println(info);
		return info;
	}
	
	public static Revision rev(String name) throws Exception {
		RVT rvc = null;
		try {
			rvc = RVTFactory.get(endpoint);
			Repository repo =  rvc.repos().get(name);
			Branch branch = repo.branches().get(Branch.MASTER);
			Revision rev = branch.getHead();
			return rev;
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        Revision rev = create(rvc, name);
        endpoint.export(Rio.createWriter(RDFFormat.TURTLE, System.out), Terms.Metadata.toString());
        return rev;
	}
	
	public static Revision create(RVT rvc, String name) throws Exception {
		if (rvc == null) {
			rvc = RVTFactory.init(endpoint);
		}
		
		if (name.equals("design")) {
			return createDesign(rvc);
		}
		else if (name.equals("cmy")) {
			return createCMY(rvc);
		}
		
		throw new RuntimeException("Unknown name:  + name");
	}
	
	public static Revision createDesign(RVT rvc) throws Exception {
		Repository repo = rvc.createRepo("design", info("my design"));
		Branch master = repo.branches().get(Branch.MASTER);
		Revision rev1 = master.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("version 1"))), info("first commit"));
		Revision rev2 = master.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("version 2"))), info("second commit"));
		Branch devel = rev2.branch("devel", info("devel branch"));
		Revision rev3 = devel.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("devel version"))), info("devel commit"));
		endpoint.export(Rio.createWriter(RDFFormat.TRIG, System.out));
		Revision rev4 = master.merge(rev3, info("merge devel branch"));
		return rev4;
	}
	
	public static Revision createCMY(RVT rvc) throws Exception {
		Repository repo = rvc.createRepo("cmy", info("CMY design"));		
		Branch master = repo.branches().get(Branch.MASTER);
		Revision rev1 = master.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("version 1"))), info("Initial commit"));
		Branch moduleM = rev1.branch("moduleM", info("Create M branch"));
		Branch moduleY = rev1.branch("moduleY", info("Create Y branch"));
		Revision commitY = moduleY.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("Y impl"))), info("Implement Y module"));
		Branch moduleC = rev1.branch("moduleC", info("Create C branch"));
		Revision commitC = moduleC.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("C impl"))), info("Implement C module"));
		Revision bugfixC = moduleC.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("C impl"))), info("Bug fix (replace amilCP with eCFP)"));
		Revision commitM = moduleM.commit(RDFInput.forStatements(Terms.stmt(user.getURI(), Terms.name, Terms.literal("M impl"))), info("Implement M module"));
		Revision mergeRev = master.merge(bugfixC, info("Merge C module"));
		mergeRev = master.merge(commitM, info("Merge M module"));
		mergeRev = master.merge(commitY, info("Merge Y module"));
		mergeRev.tag("Release 1.0", info("Wrap up Release 1.0"));
		return mergeRev;
	}
}
