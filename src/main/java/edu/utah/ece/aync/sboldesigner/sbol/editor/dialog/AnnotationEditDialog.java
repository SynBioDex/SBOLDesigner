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

package edu.utah.ece.aync.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLValidationException;

import com.google.common.base.Strings;

import edu.utah.ece.aync.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.aync.sboldesigner.swing.FormBuilder;

/**
 * 
 * @author Evren Sirin
 */
public class AnnotationEditDialog extends InputDialog<Annotation> {
	private JTextField namespace;
	private JTextField prefix;
	private JTextField name;
	private JTextComponent value;
	private JCheckBox uri = new JCheckBox();
	private Annotation oldAnnotation = null;
	private ComponentDefinition cd = null;

	/**
	 * Prepopulates fields with oldAnnotation if oldAnnotation isn't null
	 */
	public AnnotationEditDialog(Component parent, Annotation oldAnnotation, ComponentDefinition cd) {
		super(JOptionPane.getFrameForComponent(parent), "AnnotationEditor");
		this.oldAnnotation = oldAnnotation;
		this.cd = cd;
	}

	@Override
	protected void initFormPanel(FormBuilder builder) {
		String oldNamespace = "";
		String oldPrefix = "";
		String oldName = "";
		String oldValue = "";

		if (oldAnnotation != null) {
			oldNamespace = oldAnnotation.getQName().getNamespaceURI();
			oldPrefix = oldAnnotation.getQName().getPrefix();
			oldName = oldAnnotation.getQName().getLocalPart();
			oldValue = getValue(oldAnnotation);
			uri.setSelected(oldAnnotation.isURIValue());
		}

		namespace = builder.addTextField("Namespace", oldNamespace);
		prefix = builder.addTextField("Prefix", oldPrefix);
		name = builder.addTextField("Name", oldName);
		value = builder.addTextField("Value", oldValue);
		builder.add("Is clickable URI?", uri);
	}

	private String getValue(Annotation ann) {
		if (ann.getBooleanValue() != null) {
			return ann.getBooleanValue().toString();
		} else if (ann.getDoubleValue() != null) {
			return ann.getDoubleValue().toString();
		} else if (ann.getIntegerValue() != null) {
			return ann.getIntegerValue().toString();
		} else if (ann.getStringValue() != null) {
			return ann.getStringValue();
		} else if (ann.getURIValue() != null) {
			return ann.getURIValue().toString();
		} else {
			return "No value found";
		}
	}

	@Override
	protected void initFinished() {
		setSelectAllowed(true);
	}

	@Override
	protected Annotation getSelection() {
		try {
			if (uri.isSelected()) {
				return cd.createAnnotation(new QName(namespace.getText(), name.getText(), prefix.getText()),
						new URI(value.getText()));
			} else {
				return cd.createAnnotation(new QName(namespace.getText(), name.getText(), prefix.getText()),
						value.getText());
			}
		} catch (SBOLValidationException | URISyntaxException e) {
			MessageDialog.showMessage(null, "Oops", Arrays.asList(e.getMessage().split("\"|,")));
			return null;
		}
	}
}