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

package com.clarkparsia.sbol.editor.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.StatementCollector;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLValidationException;

import com.clarkparsia.sbol.SBOLRDFReader;
import com.clarkparsia.sbol.SBOLRDFWriter;
import com.clarkparsia.sbol.SBOLSPARQLReader;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.sbol.editor.SPARQLUtilities;
import com.clarkparsia.sbol.editor.dialog.PreferencesDialog;
import com.clarkparsia.sbol.editor.dialog.RegistryPreferencesTab;
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

/**
 * 
 * @author Evren Sirin
 */
public class RVTDocumentIO implements DocumentIO {
	private static final RDFFormat FORMAT = RDFFormat.RDFXML;
	
	private final Revision revision;
	private final Branch branch;
	private final SBOLRDFReader reader;
	private final SBOLRDFWriter writer;
	
	public static RVTDocumentIO createForNewRepo(SPARQLEndpoint endpoint, String repoName, String repoMsg) {
		RVT rvt = null;
		
		try {
	        rvt = RVTFactory.get(endpoint);
        }
        catch (IllegalArgumentException e) {
        	rvt = RVTFactory.init(endpoint);
        	addUserInfo(endpoint, false);
        }		
		
		Repository repo = rvt.createRepo(repoName, info(repoMsg, endpoint));
		
		return createForBranch(repo, Branch.MASTER);
	}
	
	public static RVTDocumentIO createForBranch(Registry registry, String repoName, String branchName) {
		return createForBranch(registry.createEndpoint(), repoName, branchName);
	}
	
	public static RVTDocumentIO createForBranch(SPARQLEndpoint endpoint, String repoName, String branchName) {
		RVT rvt = RVTFactory.get(endpoint);		
		
		Repository repo = rvt.repos().get(repoName);
		
		return createForBranch(repo, branchName);
	}
	
	public static RVTDocumentIO createForBranch(Repository repo, String branchName) {
		return new RVTDocumentIO(repo.branches().get(branchName), null);
	}
	
	public static RVTDocumentIO createForRevision(Revision revision) {
		return new RVTDocumentIO(revision.getBranch(), revision);
	}
	
	private RVTDocumentIO(Branch branch, Revision revision) {
		this(branch, revision, new SBOLRDFReader(FORMAT, SBOLEditorPreferences.INSTANCE.getValidate()), new SBOLRDFWriter(FORMAT, SBOLEditorPreferences.INSTANCE.getValidate()));
	}
		
	private RVTDocumentIO(Branch branch, Revision revision, SBOLRDFReader reader, SBOLRDFWriter writer) {
		this.revision = revision;
		this.branch = branch;
		this.reader= reader;
		this.writer = writer;
    }
	
	@Override
	public SBOLDocument read() throws SBOLValidationException, IOException {
		StatementCollector handler = new StatementCollector();
		Revision rev = (revision == null) ? branch.getHead() : revision;
		rev.checkout(handler);
		
		try {
	        LocalEndpoint endpoint = new LocalEndpoint();
	        endpoint.addData(RDFInput.forStatements(handler.getStatements()));

	        return new SBOLSPARQLReader(endpoint, false).read(branch.getRepository().getURI().stringValue());
        }
        catch (Exception e) {
	        throw new IOException(e);
        }
    }

	@Override
    public void write(SBOLDocument doc) throws SBOLValidationException, IOException {
		setCredentials();
		
		DnaComponent comp = (DnaComponent) doc.getContents().iterator().next();
		comp.setURI(java.net.URI.create(branch.getRepository().getURI().stringValue()));
		
		String msg = JOptionPane.showInputDialog("Enter commit message");
	    try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			writer.write(doc, bytes);
			branch.commit(RDFInput.forBytes(bytes.toByteArray()), info(msg));
        }	   
		catch (Exception e) {
			throw new IOException(e);
		}	    
    }

	@Override
    public String toString() {
	    return branch.getRepository().getName() + " - " + branch.getName();
    }

	public RVTDocumentIO createBranch(String name, String msg) {
		setCredentials();
		
	    Branch newBranch = branch.getHead().branch(name, info(msg));
	    return new RVTDocumentIO(newBranch, null, reader, writer);
    }	

	public RVTDocumentIO switchBranch(String name) {
	    Branch newBranch = branch.getRepository().branches().get(name);
	    return switchBranch(newBranch);
    }

	public RVTDocumentIO switchBranch(Branch newBranch) {
	    return new RVTDocumentIO(newBranch, null, reader, writer);
    }

	public RVTDocumentIO mergeBranch(Branch mergeBranch, String msg) {
		setCredentials();
	    branch.merge(mergeBranch.getHead(), info(msg));
	    return this;
    }

	public void createTag(String name, String msg) {
		setCredentials();
		branch.getHead().tag(name, info(msg));
    }
	
	public Branch getBranch() {
		return branch;
	}
	
	private boolean setCredentials() {
		SPARQLEndpoint endpoint = branch.getEndpoint();
		return SPARQLUtilities.setCredentials(endpoint);
	}
	
	public ActionInfo info(String msg) {
		return info(msg, branch.getEndpoint());
	}
	
	private static ActionInfo info(String msg, SPARQLEndpoint endpoint) {
		PersonInfo userInfo = addUserInfo(endpoint, true);
		return Infos.forAction(userInfo, msg);
	}
	
	private static PersonInfo addUserInfo(SPARQLEndpoint endpoint, boolean skipIfInPreferences) {
		boolean add = true;
		PersonInfo userInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
		if (userInfo == null) {
			PreferencesDialog.showPreferences(null, RegistryPreferencesTab.INSTANCE.getTitle());
			userInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
			if (userInfo == null) {
				throw new UnsupportedOperationException("Cannot perform operation without user information");
			}
		}
		else {
			add = !skipIfInPreferences;
		}
		
		if (add) {
			RVTFactory.get(endpoint).addPersonInfo(userInfo);
		}		
		
		return userInfo;
	}
}
