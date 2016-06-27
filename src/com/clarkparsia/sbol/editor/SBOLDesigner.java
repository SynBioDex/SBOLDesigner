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

package com.clarkparsia.sbol.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;

public class SBOLDesigner extends JFrame {
	
	SBOLDesignerPanel panel = null;

	public SBOLDesigner() throws SBOLValidationException {
		panel = new SBOLDesignerPanel();

		setContentPane(panel);
		setLocationRelativeTo(null);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void openDesign(DocumentIO documentIO)
			throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument doc = documentIO.read();
		doc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		if (!panel.getEditor().getDesign().load(doc)) {
			panel.setCurrentFile(documentIO);
		}
	}

	public static void main(String[] args) throws SBOLValidationException {
		setup();

		final SBOLDesigner frame = new SBOLDesigner();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);

		if (args.length > 0) {
			try {
				File file = new File(args[0]);
				Preferences.userRoot().node("path").put("path", file.getPath());
				frame.openDesign(new FileDocumentIO(false));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void setup() {
		setupLogging();
		setupLookAndFeel();
	}

	private static void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setupLogging() {
		final InputStream inputStream = SBOLDesigner.class.getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final Exception e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
}
