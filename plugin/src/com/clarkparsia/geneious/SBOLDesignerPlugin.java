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

import java.util.List;

import com.biomatters.geneious.publicapi.plugin.DocumentFileExporter;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.GeneiousService;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;
import com.clarkparsia.sbol.editor.SBOLDesignerMetadata;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLDesignerPlugin extends GeneiousPlugin {
    public String getName() {
        return SBOLDesignerMetadata.AUTHORS;
    }

    public String getHelp() {
        return getDescription();
    }

    public String getDescription() {
        return  "This plugin provides functionality to import SBOL files into Geneious, export Geneious sequences " +
        		"as SBOL files, edit designs using SBOL visual icons, import DNA components from an SBOL parts registry, " +
        		"and create sequence verification annotations.\n" +
        		"\n" +
        		"See <a href='" + SBOLDesignerMetadata.HOME_PAGE + "'>" + SBOLDesignerMetadata.HOME_PAGE + "</a> for more info.\n\n" +
        		"Send your questions and comments to <a href='mailto:" + SBOLDesignerMetadata.EMAIL + "'>" + SBOLDesignerMetadata.EMAIL + "</a>.\n";
    }

    public String getAuthors() {
        return SBOLDesignerMetadata.AUTHORS;
    }

    public String getVersion() {
        return SBOLDesignerMetadata.VERSION;
    }

    public String getMinimumApiVersion() {
        return "4.1";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
    
    public String getEmailAddressForCrashes() {
    	return SBOLDesignerMetadata.EMAIL;
    }

    @Override
    public DocumentFileImporter[] getDocumentFileImporters() {
        return new DocumentFileImporter[]{new SBOLImporter()};
    }

    @Override
    public DocumentFileExporter[] getDocumentFileExporters() {
        return new DocumentFileExporter[]{new SBOLExporter()};
    }
    
    @Override
    public SequenceAnnotationGenerator[] getSequenceAnnotationGenerators() {
        return new SequenceAnnotationGenerator[] {
                new SequenceVerificationAnnotationGenerator(),
                new SequenceVerificationAnnotationCopyClipboard(),
                new SOFAAnnotationGenerator()
        };
    }
    
    @Override
	public DocumentViewerFactory[] getDocumentViewerFactories() {
		return new DocumentViewerFactory[] { new SBOLViewer.Factory() };
	}
    
    @Override
    public DocumentOperation[] getDocumentOperations() {
    	return new DocumentOperation[] { new SBOLCheckout(), new SBOLCommit() };
    }
    
    @Override
    public GeneiousService[] getServices() {
    	List<GeneiousService> services = Lists.newArrayList();
    	try {
	        services.add(new VersioningService("http://localhost:5822/rvt"));
	        services.add(new VersioningService("http://ec2-174-129-47-60.compute-1.amazonaws.com:8080/demo"));
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
        return Iterables.toArray(services, GeneiousService.class);
    }
}