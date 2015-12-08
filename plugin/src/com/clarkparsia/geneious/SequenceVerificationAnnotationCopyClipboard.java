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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jebl.util.ProgressListener;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.Dialogs.DialogIcon;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceListOnDisk;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceListSummary;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument.Alphabet;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceListSummary.PairwiseSimilarity;
import com.biomatters.geneious.publicapi.implementations.SequenceExtractionUtilities;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;
import com.google.common.base.Joiner;

/**
 * Plugin for copying sequence verification annotations to clipboard.
 * 
 * @author Evren Sirin
 */
public class SequenceVerificationAnnotationCopyClipboard extends SequenceAnnotationGenerator {
	public GeneiousActionOptions getActionOptions() {
		return new GeneiousActionOptions("Copy Sequence Verification Annotation(s) to Clipboard",
		                "Copies a text description of annotations for sequence verification results")
		                .setMainMenuLocation(GeneiousActionOptions.MainMenu.AnnotateAndPredict);
	}

	public String getHelp() {
		return "This plugin copies text description of annotations for sequence variants.";
	}

	public DocumentSelectionSignature[] getSelectionSignatures() {
		return new DocumentSelectionSignature[] { new DocumentSelectionSignature(SequenceAlignmentDocument.class, 1, 1) };
	}

	public List<List<SequenceAnnotation>> generateAnnotations(AnnotatedPluginDocument[] documents,
	                SelectionRange selectionRange, ProgressListener progressListener, Options _options)
	                throws DocumentOperationException {
        SequenceAlignmentDocument doc = (SequenceAlignmentDocument) documents[0].getDocument();
        
        String copyText = showCopyText(doc);
        if (copyText != null) { 
        	StringSelection stringSelection = new StringSelection(copyText);
        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        	clipboard.setContents(stringSelection, new ClipboardOwner() {
        		public void lostOwnership(Clipboard clipboard, Transferable transferable) {
        			// nothing to do
        		}
        	});
        }
    	
		throw new DocumentOperationException.Canceled();
	}


	public String showCopyText(SequenceAlignmentDocument doc) {
		SublimeAlignmentDocument alignmentDoc = new SublimeAlignmentDocument(doc);
		
		List<SequenceAnnotation> annotations = GeneiousUtils.getVariantAnnotations(alignmentDoc.getAnnotations());
		
		if (annotations.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No sequence annotations found!", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		String copyText = computeCopyText(alignmentDoc, annotations);
		
		final JTextArea textArea = new JTextArea();
		textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		textArea.setEditable(false);
		textArea.setText(copyText);
		
		JScrollPane scrollPane = new JScrollPane(textArea);		
		scrollPane.setPreferredSize(new Dimension(500, 300));
		
		JPanel panel = new JPanel(new BorderLayout(5, 10));		
		JLabel label = new JLabel("<html>Sequence annotations found: " + annotations.size() + "<br>Click OK to copy the following contents to clipboard.</html>");
		panel.add(label, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);		
		
//		int confirmation = JOptionPane.showConfirmDialog(null, panel, "Copy annotations to clipboard", JOptionPane.OK_CANCEL_OPTION);
//		return confirmation == JOptionPane.OK_OPTION ? copyText : null;
		
		boolean confirmed = Dialogs.showOkCancelDialog(panel, "Copy annotations to clipboard", null, DialogIcon.NO_ICON);
		return confirmed ? copyText : null;
	}

	
	private String computeCopyText(SublimeAlignmentDocument alignmentDoc, List<SequenceAnnotation> annotations) {		
		String newLine = System.getProperty("line.separator");
		// we might use a tab here
		String separator = "\t";
		
		DateFormat df = DateFormat.getDateTimeInstance();
		StringBuilder sb = new StringBuilder();
		
		SequenceDocument designDoc = alignmentDoc.getDesign();
		
		sb.append("Documents").append(newLine);
		sb.append("---------").append(newLine);
		Joiner.on(separator).appendTo(sb, "Document", "Name", "Date");
        sb.append(newLine);

		sb.append("Design").append(separator);
		sb.append(designDoc.getName()).append(separator);
		sb.append(designDoc.getCreationDate() == null ? "" : df.format(designDoc.getCreationDate()));
		sb.append(newLine);
		
		for (SequenceDocument sequencingDoc : alignmentDoc.getSequencingData()) {
			sb.append("Sequencing").append(separator);
			sb.append(sequencingDoc.getName()).append(separator);
			sb.append(sequencingDoc.getCreationDate() == null ? "" : df.format(sequencingDoc.getCreationDate()));
			sb.append(newLine);
		}
		
		sb.append(newLine);
		sb.append("Annotations").append(newLine);
		sb.append("-----------").append(newLine);            
		Joiner.on(separator).appendTo(sb, "Name", "Type", "Interval", "Component", "Ambiguity");
        sb.append(newLine);
        
		for (SequenceAnnotation annotation : annotations) {
	        SequenceAnnotationInterval interval = annotation.getIntervals().get(0);

            //sb.append(i+1).append(separator);
	        sb.append(annotation.getName()).append(separator);
	        sb.append(annotation.getType()).append(separator);
	        sb.append(alignmentDoc.originalInterval(interval)).append(separator);
	        // sb.append(annotation.getQualifierValue(GeniousQualifiers.SO_TYPE)).append(separator);
	        sb.append(annotation.getQualifierValue(GeneiousQualifiers.AFFECTED_COMPONENT)).append(separator);
	        Boolean isAmbiguous = GeneiousUtils.mapAmbiguousVerification(annotation.getQualifierValue(GeneiousQualifiers.AMBIGUOUS_VERIFICATION));
	        if (isAmbiguous != null) {
	        	sb.append(isAmbiguous ? "Ambiguous" : "Not ambiguous").append(separator);
	        }
	        sb.append(newLine);
        }
		
		sb.append(newLine);
		sb.append("Statistics").append(newLine);
		sb.append("-----------").append(newLine);
		Joiner.on(separator).appendTo(sb, "Name", "Type", "Coverage (%)", "Pairwise Identity (%)");
        sb.append(newLine);
		
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(1);
		
		List<SequenceAnnotation> components = alignmentDoc.getDesign().getSequenceAnnotations();
//		SequenceAnnotation root = GeneiousUtils.findRootAnnotation(alignmentDoc.getDesign());
		for (SequenceAnnotation component : components) {
//			SequenceAnnotation parent = GeneiousUtils.findParentAnnotation(component, components);
			if (/*parent == null || parent == root &&*/ !GeneiousUtils.isVariant(component) && !component.getIntervals().isEmpty()) {
		        SequenceAnnotationInterval interval = component.getIntervals().get(0);
				SequenceListSummary summary = computeSummary(alignmentDoc, interval); 
				
        		double pairwiseIdentity = getPairwiseIdentityPercentage(summary);        		
        		double coverage = getCoveragePercentage(summary, interval);
        		
    	        sb.append(component.getName()).append(separator);
    	        sb.append(component.getType()).append(separator);
    	        sb.append(percentFormat.format(coverage)).append(separator);
    	        sb.append(percentFormat.format(pairwiseIdentity)).append(separator);
    	        sb.append(newLine);
			}
        }
		
		return sb.toString();
	}
		
	private double getPairwiseIdentityPercentage(SequenceListSummary summary) {
		PairwiseSimilarity sim = summary.getAlignmentPairwiseSimilarity(true);
		return sim.getNumberOfPairs() == 0 ? 0 : sim.getNumberOfIdenticalPairs() / sim.getNumberOfPairs();
	}
	
	private double getCoveragePercentage(SequenceListSummary summary, SequenceAnnotationInterval interval) {
		return (double) summary.getReferenceSequenceBasesCovered() / interval.getLength();
	}
	
	private SequenceListSummary computeSummary(SublimeAlignmentDocument alignmentDoc, SequenceAnnotationInterval interval) {
		try {
			SequenceListOnDisk.Builder<SequenceDocument> builder = new SequenceListOnDisk.Builder<SequenceDocument>(false, Alphabet.NUCLEOTIDE, true);
			
			ProgressListener listener = ProgressListener.EMPTY;
			SequenceDocument ref = SequenceExtractionUtilities.extractIntervals(alignmentDoc.getDesign(), interval);
			builder.addAlignmentReferenceSequence(ref, listener);
			for (SequenceDocument doc : alignmentDoc.getSequencingData()) {
				SequenceDocument seq = SequenceExtractionUtilities.extractIntervals(doc, interval);
				builder.addSequence(seq, listener);    
            }
			
			return builder.toSequenceList(listener).getSummary();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
