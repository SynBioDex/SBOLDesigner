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

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.KeyStroke;

import jebl.util.ProgressListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virion.jam.util.SimpleListener;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.Dialogs.DialogIcon;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;
import com.biomatters.geneious.publicapi.utilities.Interval;

/**
 * Plugin for generating sequence verification annotations.
 * 
 * @author Evren Sirin
 */
public class SequenceVerificationAnnotationGenerator extends SequenceAnnotationGenerator {
	private static Logger LOGGER = LoggerFactory.getLogger(SequenceVerificationAnnotationGenerator.class.getName());
	
	@Override
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Add Sequence Verification Annotation",
                "Adds an annotation for sequence verification results").
                setMainMenuLocation(GeneiousActionOptions.MainMenu.AnnotateAndPredict)
                .setShortcutKey(KeyStroke.getKeyStroke(Character.valueOf('V'), InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
    }

    @Override
    public String getHelp() {
        return "This plugin adds annotations for sequence variants.";
    }

    @Override
    public SequenceVerificationAnnotationGeneratorOptions getOptions(AnnotatedPluginDocument[] documents, SelectionRange selectionRange) throws DocumentOperationException {
    	SequenceAlignmentDocument doc = (SequenceAlignmentDocument) documents[0].getDocument();
    	
    	if (selectionRange == null) {
    		Dialogs.showMessageDialog("\nNo bases selected!", "Error", null, DialogIcon.ERROR);
    	}
    	else {    	
	    	try {
		        SublimeAlignmentDocument alignmentDoc = new SublimeAlignmentDocument(doc);
		        
		        Interval range = selectionRange.getResidueIntervals().get(0);
		        SequenceAnnotationInterval interval = new SequenceAnnotationInterval(range.getMin() + 1, range.getMaxExclusive(), Direction.none);
	
		        SequenceVerificationAnnotation result = new SequenceVerificationAnnotation(interval, alignmentDoc);
		        
		        return new SequenceVerificationAnnotationGeneratorOptions(result);
	        }
	        catch (IllegalArgumentException e) {
	        	Dialogs.showMessageDialog("\nUnsupported alignment document.\n" + e.getMessage(), "Error", null, DialogIcon.ERROR);
	        }
    	}
    	
		throw new DocumentOperationException.Canceled();
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
		return new DocumentSelectionSignature[] { new DocumentSelectionSignature(SequenceAlignmentDocument.class, 1, 1) };
    }

    @Override
    public List<List<SequenceAnnotation>> generateAnnotations(AnnotatedPluginDocument[] annotatedPluginDocuments, SelectionRange selectionRange, ProgressListener progressListener, Options _options) throws DocumentOperationException {
    	SequenceAlignmentDocument doc = (SequenceAlignmentDocument) annotatedPluginDocuments[0].getDocument();
    	
    	SequenceVerificationAnnotationGeneratorOptions options = (SequenceVerificationAnnotationGeneratorOptions) _options;    	
    	
    	SequenceVerificationAnnotation verificationResult = options.getResult();
    	
    	SequenceAnnotation annotation = verificationResult.generateAnnotation();

    	List<List<SequenceAnnotation>> result = new ArrayList<List<SequenceAnnotation>>();
    	for (int i = 0; i < doc.getSequences().size(); i++) {
    		result.add(Collections.<SequenceAnnotation>emptyList());
    	}    	
    	result.add(Arrays.asList(annotation));
    	
        return result;
    }    
    
    public static class SequenceVerificationAnnotationGeneratorOptions extends Options {
    	private SequenceVerificationAnnotation result;
    	
        private EditableComboBoxOption variationType;
        private StringOption label;
        private BooleanOption ambiguousResult;
        private StringOption affectedComponent;
        
        private SequenceVerificationAnnotationGeneratorOptions(SequenceVerificationAnnotation svr) {
        	this.result = svr;
        	
        	LOGGER.debug("initial variant {}", svr.getVariantType());
        	LOGGER.debug("initial identifier {}", svr.getIdentifier());
        	
        	variationType = addEditableComboBoxOption("variationType","Variation type", result.getVariantType().toString(), SequenceVariantType.NAMES);
        	variationType.addChangeListener(new SimpleListener() {				
				public void objectChanged() {					
					SequenceVariantType variantType = SequenceVariantType.find(variationType.getValue());
					
					LOGGER.debug("change variant {}", variantType);
					
					result.setVariantType(variantType);
					
		        	LOGGER.debug("change identifier {}", result.getIdentifier());
					
					label.setValue(result.getIdentifier());
				}
			});
        	
        	variationType.setValue(result.getVariantType().toString());
        	
        	label = addStringOption("label", "Label", result.getIdentifier());
        	label.setValue(result.getIdentifier());
        	label.setEnabled(false);

        	ambiguousResult = addBooleanOption("ambiguousResult", "Verification result ambiguous", result.isAmbiguous());
        	ambiguousResult.addChangeListener(new SimpleListener() {				
				public void objectChanged() {
					result.setAmbiguous(ambiguousResult.getValue());
				}
			});
        	ambiguousResult.setValue(result.isAmbiguous());
        	        	
        	SequenceAnnotation affectedAnnotation = result.getAffectedComponent();
        	String affectedComponentName = affectedAnnotation == null 
        					? "No component found at the selected interval"
        					: affectedAnnotation.getName();
        	LOGGER.debug("Affected component: {}", affectedComponentName);
        	affectedComponent = addStringOption("affectedComponent", "Affected Component", affectedComponentName);
        	affectedComponent.setValue(affectedComponentName);
        	affectedComponent.setEnabled(false);
        }

		public SequenceVerificationAnnotation getResult() {
            return result;
		}
    }
    
}
