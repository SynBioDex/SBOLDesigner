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

package com.clarkparsia.versioning.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.helpers.StatementCollector;

import com.clarkparsia.sbol.editor.sparql.LocalEndpoint;
import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Infos;
import com.clarkparsia.versioning.PersonInfo;
import com.clarkparsia.versioning.RVT;
import com.clarkparsia.versioning.RVTFactory;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.sparql.Terms;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class MergeTests {
	private static PersonInfo user = Infos.forPerson("urn:Evren", "Evren Sirin", "mailto:evren@sirin.org");
	private static SPARQLEndpoint endpoint;
	private static RVT rvt;
	
	@BeforeClass
	public static void beforeClass() {
		endpoint = new LocalEndpoint();
		rvt = RVTFactory.init(endpoint);
	}

	@Before
	public void beforeTest() throws Exception {
		endpoint.clear();		
	}
	
	public static ActionInfo info(String msg) {
		return Infos.forAction(user, msg);
	}
	
	@Test
	public void fastForwardOnlyAdd() throws Exception {
		Statement stmt1 = Terms.stmt(user.getURI(), RDFS.LABEL, Terms.literal("master version"));
		Statement stmt2 = Terms.stmt(user.getURI(), RDFS.COMMENT, Terms.literal("devel version"));
		Set<Statement> firstCommit = Sets.newHashSet(stmt1); 
		Set<Statement> develCommit = Sets.newHashSet(stmt1, stmt2); 
		Set<Statement> masterCommit = ImmutableSet.of();
		Set<Statement> mergeResult = develCommit;
		testMerge(firstCommit, develCommit, masterCommit, mergeResult);
	}
	
	@Test
	public void fastForwardAddRemove() throws Exception {
		Statement stmt1 = Terms.stmt(user.getURI(), RDFS.LABEL, Terms.literal("master version"));
		Statement stmt2 = Terms.stmt(user.getURI(), RDFS.COMMENT, Terms.literal("devel version"));
		Set<Statement> firstCommit = Sets.newHashSet(stmt1); 
		Set<Statement> develCommit = Sets.newHashSet(stmt2); 
		Set<Statement> masterCommit = ImmutableSet.of();
		Set<Statement> mergeResult = develCommit;
		testMerge(firstCommit, develCommit, masterCommit, mergeResult);
	}
	
	@Test
	public void mergeNoConflict() throws Exception {
		Statement stmt1 = Terms.stmt(user.getURI(), RDFS.LABEL, Terms.literal("master version"));
		Statement stmt2 = Terms.stmt(user.getURI(), RDFS.COMMENT, Terms.literal("devel version"));
		Statement stmt3 = Terms.stmt(user.getURI(), RDFS.SEEALSO, Terms.literal("see also"));
		Set<Statement> firstCommit = Sets.newHashSet(stmt1); 
		Set<Statement> develCommit = Sets.newHashSet(stmt2); 
		Set<Statement> masterCommit = Sets.newHashSet(stmt3); 
		Set<Statement> mergeResult = Sets.newHashSet(stmt2, stmt3); 
		testMerge(firstCommit, develCommit, masterCommit, mergeResult);
	}
	

	public void testMerge(Set<Statement> firstCommit, Set<Statement> develCommit, Set<Statement> masterCommit, Set<Statement> mergeResult) throws Exception {
		Repository repo = rvt.createRepo("design", info("my design"));
		
		Branch master = repo.branches().get(Branch.MASTER);
		Revision rev1 = master.commit(RDFInput.forStatements(firstCommit), info("first commit"));
		
		Branch devel = rev1.branch("devel", info("devel branch"));
		Revision rev2 = devel.commit(RDFInput.forStatements(develCommit), info("devel commit"));	
		
		if (!masterCommit.isEmpty()) {
			master.commit(RDFInput.forStatements(masterCommit), info("first commit"));
		}
		
		StatementCollector collector = new StatementCollector();
		Revision rev3 = master.merge(rev2, info("merge devel branch"));
		rev3.checkout(collector);
		assertEquals(mergeResult, Sets.newHashSet(collector.getStatements()));
	}
}
