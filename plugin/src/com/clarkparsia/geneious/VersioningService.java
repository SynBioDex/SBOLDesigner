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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLValidationException;

import com.biomatters.geneious.publicapi.databaseservice.AdvancedSearchQueryTerm;
import com.biomatters.geneious.publicapi.databaseservice.BasicSearchQuery;
import com.biomatters.geneious.publicapi.databaseservice.CompoundSearchQuery;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseService;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.databaseservice.Query;
import com.biomatters.geneious.publicapi.databaseservice.QueryField;
import com.biomatters.geneious.publicapi.databaseservice.RetrieveCallback;
import com.biomatters.geneious.publicapi.documents.Condition;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.EditableSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.clarkparsia.geneious.SBOLImporter.Visitor;
import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.RVTDocumentIO;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.RVT;
import com.clarkparsia.versioning.RVTFactory;
import com.clarkparsia.versioning.Repository;

/**
 * This class is an example of a DatabaseService which provides access to the contents of a fasta file.
 *
 * @version $Id$
 */

public class VersioningService extends DatabaseService {
	private final RVT rvt;
	
    public VersioningService(String endpointURL) {
        rvt = RVTFactory.get(new StardogEndpoint(endpointURL));
    }
    
    @Override
    public boolean isBrowsable() {
        return true;
    }

    public String getUniqueID() {
        return VersioningService.class.getName();
    }

    public String getName() {
        return "SBOL Version Repository (" + rvt.getEndpoint().getURL() + ")";
    }

    public String getDescription() {
        return "SBOL Version Repository";
    }

    public String getHelp() {
        return "";
    }

    public Icons getIcons() {
    	return GeneiousActions.icon("repository.gif");
    }

    public QueryField[] getSearchFields() {
        return new QueryField[]{
                new QueryField(new DocumentField("Name","The design name","name",String.class,false,false),new Condition[]{Condition.CONTAINS}),
                new QueryField(new DocumentField("Creation date","The date the design was created in the version repository","creationDate",Date.class,false,false),new Condition[]{Condition.DATE_AFTER_OR_ON}),
                new QueryField(new DocumentField("Modification date","The last date the design was updated in the version repository","modifyDate",Date.class,false,false),new Condition[]{Condition.DATE_AFTER_OR_ON})
        };
    }


    //we find rsults based on the given query, and return them using the callback supplied.
    public void retrieve(Query query, RetrieveCallback callback, URN[] urnsToNotRetrieve) throws DatabaseServiceException {        
        try{
//        	 StringBuilder builder = new StringBuilder();
//        	 builder.append("Document Operations:\n");
//        	 for (DocumentOperation operation : PluginUtilities.getDocumentOperations()) {
//        	     builder.append(operation.getActionOptions().getName()+" has id: "+operation.getUniqueId()+"\n");
//        	 }
//        	 builder.append("\n\nAnnotation Generators:\n");
//        	 for (SequenceAnnotationGenerator generator : PluginUtilities.getSequenceAnnotationGenerators()) {
//        	     builder.append(generator.getActionOptions().getName()+" has id: "+generator.getUniqueId()+"\n");
//        	 }
//        	 String message=builder.toString();
//        	 Dialogs.showMessageDialog(message);
        	 
            ArrayList<String> nameToMatch = new ArrayList<String>();
            ArrayList<String> residuesToMatch = new ArrayList<String>();
            boolean matchEverything = false; //this is true if we want both the name and the residues to match for a document to be included in the search results

            //we will store a list of the queries
            java.util.List<Query> queries;

            //a compoundSearchQuery consists of a number of queries
            //we'll put them in the list
            if(query instanceof CompoundSearchQuery){
                CompoundSearchQuery cQuery = (CompoundSearchQuery)query;
                matchEverything = cQuery.getOperator() == CompoundSearchQuery.Operator.AND;
                queries = (java.util.List<Query>) cQuery.getChildren();
            }
            //if the query is not a CompoundSearchQuery, then we can create a one-element list containing the query
            else {
                queries = new ArrayList<Query>();
                queries.add(query);
            }

            //we'll loop through all the queries, and set the nameToMatch and residuesToMatch
            for(Query q : queries){
                //we have the sequence and name, do the searching
                if(q instanceof AdvancedSearchQueryTerm){
                    AdvancedSearchQueryTerm advancedQuery = (AdvancedSearchQueryTerm)q;
                    if(advancedQuery.getField().getCode().equals("name")){
                        nameToMatch.add(advancedQuery.getValues()[0].toString().toUpperCase());
                    }
                }

                //a {@link BasicSearchQuery} consists of one field (search text)
                //you can extend a basic query, for example using a {@link CompoundSearchQuery}
                else if (q instanceof BasicSearchQuery) {
                    //set both the name and the residue searches to the query entered
                    BasicSearchQuery bq = (BasicSearchQuery)query;
                    nameToMatch.add(bq.getSearchText().toUpperCase());
                }
                else{
                    //do nothing
                }
            }

            //if neither nameToMatch or residuesToMatch are set at this point, the search will return no results.

            
            for (Repository repo : rvt.repos().list()) {            	
                SequenceDocument doc = match(repo, nameToMatch,residuesToMatch, matchEverything);
                if(doc != null){
                    //add a search result if there is one
                    callback.add(doc, Collections.<String,Object>emptyMap());
                }
            }
        }
        catch(Exception e){
            throw new DatabaseServiceException(e,e.getMessage(),false);
        }
    }

    //this utility method returns a SequenceDocument based on the given name and residues, if they match the search parameters
    //or null if there is no match
    private SequenceDocument match(Repository repo, ArrayList<String> namesToMatch,  ArrayList<String> residuesToMatch, boolean matchBoth) throws SBOLValidationException, IOException{
    	String name = repo.getName().toUpperCase();
        boolean nameMatch = false;
        boolean residueMatch = false;
        if(namesToMatch.size() > 0){
            for(String nameToMatch : namesToMatch){
                if(name.contains(nameToMatch)){
                    nameMatch = true;
                }
            }
        }

//        if(residuesToMatch.size() > 0){
//            for(String residueToMatch : residuesToMatch){
//                if(residues.toUpperCase().contains(residueToMatch)){
//                    residueMatch = true;
//                }
//            }
//        }
        boolean match;
        if (matchBoth) {
            match=residueMatch && nameMatch;
        }
        else {
            match=residueMatch || nameMatch;
        }
        if (match) {
    		DocumentIO newIO =  RVTDocumentIO.createForBranch(repo, Branch.MASTER);
    		SBOLDocument sbolDocument = newIO.read();
			List<EditableSequenceDocument> geneiousDocs = new Visitor().importDocument(sbolDocument);
			return geneiousDocs.get(0);
        }
        return null;
    }

//    public JPanel getPanel() {
//		JPanel p =new JPanel();
//		p.add(new JLabel("test"));
//	    return p;
//    }
}
