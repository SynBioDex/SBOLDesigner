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

package com.clarkparsia.sbol;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openrdf.model.impl.URIImpl;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;

import com.clarkparsia.sbol.editor.SBOLDesign;
import com.clarkparsia.sbol.editor.SBOLDesigner;
import com.clarkparsia.sbol.editor.SBOLEditor;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;

/**
* @author Evren Sirin
*/
public class TestEndpoint {
	public static void main(String[] args) throws Exception {
		String endpointURL = args[0];
		String componentURI = args[1];
		
		SBOLDocument doc = SublimeSBOLFactory.createReader(new StardogEndpoint(endpointURL), false).read(componentURI);
		System.out.println(new SBOLTextWriter().write(doc));
		SBOLFactory.createNoValidationWriter().write(doc, new FileOutputStream(new URIImpl(componentURI).getLocalName() + ".rdf"));
		
		SBOLEditor editor = new SBOLEditor(false);
		SBOLDesign design = editor.getDesign();
		design.load(doc);
		
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new JScrollPane(design.getPanel()));
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
}

