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

package com.clarkparsia.geneious;

import java.util.Collections;
import java.util.List;

import jebl.util.ProgressListener;

import org.sbolstandard.core.SBOLDocument;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.clarkparsia.sbol.editor.dialog.CreateVersionDialog;
import com.clarkparsia.sbol.editor.io.DocumentIO;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLCommit extends DocumentOperation {
    public String getUniqueId() {
        return SBOLCommit.class.getName();
    }

    public GeneiousActionOptions getActionOptions() {
    	return GeneiousActions.options("Commit", "Commits the selected document to the version repository", "commit.gif").setInMainToolbar(true);
    }

    public String getHelp(){
        return "Checks out an SBOL document from a version repository";
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
    	return new DocumentSelectionSignature[] { DocumentSelectionSignature.forNucleotideSequences(1,
		                Integer.MAX_VALUE, true) };
    }

    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws DocumentOperationException.Canceled {
    	final SequenceDocument sequenceDoc = (SequenceDocument) docs[0].getDocumentOrCrash();
		
    	DocumentIO docIO = GeneiousUtils.getVersioningInfo(sequenceDoc); 		
    	if (docIO == null) {
    		docIO = new CreateVersionDialog(null).getInput();
    	}
    	
		if (docIO == null) {
			throw new DocumentOperationException.Canceled();
		}
				
        try {
        	final SBOLDocument sbolDoc = new SBOLExporter().processDocument(sequenceDoc);
        	
        	docIO.write(sbolDoc);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
        
        return Collections.emptyList();
   }
}
