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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import jebl.util.ProgressListener;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.Dialogs.DialogIcon;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval.Direction;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;
import com.biomatters.geneious.publicapi.utilities.Interval;
import com.clarkparsia.sbol.terms.SOFA;
import com.clarkparsia.sbol.terms.Term;
import com.clarkparsia.swing.FilterTree;
import com.clarkparsia.swing.FilterTree.FilterTreeModel;
import com.clarkparsia.swing.FilterTree.FilterTreeNode;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Plugin for generating sequence verification annotations.
 * 
 * @author Evren Sirin
 */
public class SOFAAnnotationGenerator extends SequenceAnnotationGenerator {
	@Override
	public GeneiousActionOptions getActionOptions() {
		return new GeneiousActionOptions("Add SOFA Annotation", "Adds an annotation using based on SOFA types")
		                .setMainMenuLocation(GeneiousActionOptions.MainMenu.AnnotateAndPredict);
	}

	@Override
	public String getHelp() {
		return "This plugin adds annotations using SOFA types.";
	}

	@Override
	public SOFAAnnotationGeneratorOptions getOptions(AnnotatedPluginDocument[] documents, SelectionRange selectionRange)
	                throws DocumentOperationException {
		if (selectionRange == null) {
			Dialogs.showMessageDialog("\nNo bases selected!", "Error", null, DialogIcon.ERROR);
		}
		else {
			try {
				Interval range = selectionRange.getResidueIntervals().get(0);
				boolean isForward = range.getFrom() <= range.getTo();

				return new SOFAAnnotationGeneratorOptions(isForward);
			}
			catch (IllegalArgumentException e) {
				Dialogs.showMessageDialog("\nUnsupported alignment document.\n" + e.getMessage(), "Error", null,
				                DialogIcon.ERROR);
			}
		}

		throw new DocumentOperationException.Canceled();
	}

	@Override
	public DocumentSelectionSignature[] getSelectionSignatures() {
		return new DocumentSelectionSignature[] { DocumentSelectionSignature.forNucleotideSequences(1, 1) };
	}

	@Override
	public List<List<SequenceAnnotation>> generateAnnotations(AnnotatedPluginDocument[] annotatedPluginDocuments,
	                SelectionRange selectionRange, ProgressListener progressListener, Options _options)
	                throws DocumentOperationException {
		SOFAAnnotationGeneratorOptions options = (SOFAAnnotationGeneratorOptions) _options;
        
        String name = options.getName();        
        if (name == null) {
            throw new DocumentOperationException("Must specify a name");
        }
		
		Term type = options.getType();
        if (type == null) {
            throw new DocumentOperationException("Must select a SOFA type");
        }

		Interval range = selectionRange.getResidueIntervals().get(0);

		SequenceAnnotationInterval interval = new SequenceAnnotationInterval(range.getMin() + 1,
		                range.getMaxExclusive(), options.getDirection());

		SequenceAnnotation annotation = new SequenceAnnotation(name, type.getLabel(), interval);
    	annotation.addQualifier(GeneiousQualifiers.SO_TYPE, type.getURI());

		return Collections.singletonList(Collections.singletonList(annotation));
	}

	public static class SOFAAnnotationGeneratorOptions extends Options {
		private static final OptionValue FWD = new OptionValue("fwd", "Forward");
		private static final OptionValue REV = new OptionValue("rev", "Reverse");
		private static final OptionValue NONE = new OptionValue("none", "Undirected");
		
		private static final Map<OptionValue, Direction> DIRECTIONS = ImmutableMap.of(
			FWD, Direction.leftToRight,
			REV, Direction.rightToLeft,
			NONE, Direction.none
		);
		
		private final StringOption nameOption;
		private final StringOption filterOption;
		private final RadioOption<OptionValue> directionOption;
		private final FilterTree tree; 

		private SOFAAnnotationGeneratorOptions(boolean isForward) {
			nameOption = addStringOption("name", "Name", "");

			directionOption = addRadioOption("direction", "Direction", Lists.newArrayList(DIRECTIONS.keySet()), isForward ? FWD :REV, Alignment.HORIZONTAL_ALIGN);

			filterOption = addStringOption("filter", "SOFA type", "", "Enter text to filter types and select a type from the list");

			tree = new FilterTree(createTreeModel(), Functions.toStringFunction());
			tree.setFilterField(filterOption.getComponent());
			tree.expandAll();
			
			Option<?,?> option = addCustomComponent(new JScrollPane(tree));
			option.setFillHorizontalSpace(true);
		}
		
		private String getName() {
			return nameOption.getValue();
		}
		
		private Term getType() {
			TreePath selection = tree.getSelectionPath();
			return selection == null ? null : (Term) ((FilterTreeNode) selection.getLastPathComponent()).getUserObject();
		}
		
		private Direction getDirection() {
			return DIRECTIONS.get(directionOption.getValue());
		}
	}

	private static FilterTreeModel createTreeModel() {		
		return new FilterTreeModel(createNode(SOFA.getInstance().getTopTerm()));
	}

	private static FilterTreeNode createNode(Term term) {
		FilterTreeNode node = new FilterTreeNode(term);
		for (Term subClass : term.getSubClasses()) {
			FilterTreeNode child = createNode(subClass);
			node.add(child);
		}
		return node;
	}

	public static void main(String[] args) {
		final FilterTreeModel model = createTreeModel();
		final FilterTree tree = new FilterTree(model, Functions.toStringFunction());

		final JTextField field = new JTextField(10);
		tree.setFilterField(field);
		
	    JFrame frame = new JFrame();
	    frame.getContentPane().add(field, BorderLayout.NORTH);
	    frame.getContentPane().add(new JScrollPane(tree));
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(600, 480);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
}
