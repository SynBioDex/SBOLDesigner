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

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AbstractPluginDocument;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.EditableSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.clarkparsia.geneious.SBOLImporter.Visitor;
import com.clarkparsia.sbol.editor.dialog.CheckoutDialog;
import com.clarkparsia.sbol.editor.dialog.CheckoutDialog.CheckoutResult;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLCheckout extends DocumentOperation {
    //This method is so this DocumentOperation can be returned by PluginUtilities.getDocumentOperation(String id);
    public String getUniqueId() {
        return SBOLCheckout.class.getName();
    }

    //GeneiousActionOptions specify how the action is going to be displayed within Geneious.
    //in this case it is going to be displayed on the toolbar with the label "New Sequence", and the pencil icon.
    public GeneiousActionOptions getActionOptions() {
        return GeneiousActions.options("Checkout", "Checks out a version of an SBOL document from a version repository", "checkout.gif").setInMainToolbar(true);
    }

    public String getHelp(){
        return "Checks out an SBOL document from a version repository";
    }

    //DocumentSelection signatures define what types of documents (and numbers of documents) the operation can take.
    //in this case we do not need to take documents, so we can just return an empty set.
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[0];
    }

    //This is the method that does all the work.  Geneious passes a list of the documents that were selected when the user
    //started the operation, a progressListener, and the options panel that we returned in the getOptionsPanel() method above.
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws DocumentOperationException.Canceled {
		CheckoutResult result = new CheckoutDialog(null).getInput();
		if (result == null) {
			throw new DocumentOperationException.Canceled();
		}
		
		RVTDocumentIO docIO = result.getDocumentIO();
		SBOLDocument sbolDocument;
        try {
	        sbolDocument = docIO.read();
			List<EditableSequenceDocument> geneiousDocs = new Visitor().importDocument(sbolDocument);
			if (geneiousDocs.size() != 1) {
				Dialogs.showMessageDialog("Cannot load documents with multiple designs");
				return Collections.emptyList();
			}
			
			EditableSequenceDocument geneiousDoc = geneiousDocs.get(0);
			GeneiousUtils.setVersioningInfo((AbstractPluginDocument) geneiousDoc, docIO);
			return DocumentUtilities.createAnnotatedPluginDocuments(geneiousDoc);
        }
        catch (Exception e) {
	        e.printStackTrace();
	        return Collections.emptyList();
        }
   }
}
