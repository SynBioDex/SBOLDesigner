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

package com.clarkparsia.sbol.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JPanel;

import org.sbolstandard.core.SBOLDocument;

import com.clarkparsia.sbol.SublimeSBOLFactory;
import com.clarkparsia.sbol.editor.SBOLDesign;
import com.clarkparsia.sbol.editor.SBOLEditor;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.google.common.base.Preconditions;

public class SBOLVisualServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_ENDPOINT = "http://localhost:5822/SBPkb";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			SBOLDocument doc = SublimeSBOLFactory.read(request.getInputStream());
	        writeImage(doc, response);
        }
        catch (Exception e) {
	        throw new IOException(e);
        }
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String componentURI = request.getParameter("component");
		Preconditions.checkNotNull(componentURI, "Mssing parameter component");
				
		String endpointURL = request.getParameter("endpoint");
		if (endpointURL == null) {
			endpointURL = DEFAULT_ENDPOINT;
		}
		
		System.out.println("endpointURL " + endpointURL);
		System.out.println("componentURI " + componentURI);
		try {
	        SBOLDocument doc = SublimeSBOLFactory.createReader(new StardogEndpoint(endpointURL), false).read(componentURI);
	        writeImage(doc, response);
        }
        catch (Exception e) {
	        throw new IOException(e);
        }
	}
	
	private void writeImage(SBOLDocument doc, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");

		SBOLEditor editor = new SBOLEditor(false);
		SBOLDesign design = editor.getDesign();
		design.load(doc);

		JPanel panel = design.getPanel();
		panel.addNotify();
		panel.setSize(panel.getPreferredSize());
		panel.validate();

		BufferedImage bi = design.getSnapshot();
		OutputStream out = response.getOutputStream();
		ImageIO.write(bi, "png", out);
		out.close();
	}

}