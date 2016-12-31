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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import com.clarkparsia.sbol.editor.io.DocumentIO;
import com.clarkparsia.sbol.editor.io.FileDocumentIO;

/**
 * The JFrame shown for the standalone SBOLDesigner application
 * 
 * @author Michael Zhang
 *
 */
public class SBOLDesignerStandalone extends JFrame {

	SBOLDesignerPanel panel = null;

	public SBOLDesignerStandalone() throws SBOLValidationException, IOException {
		// reset the path
		Preferences.userRoot().node("path").put("path", "");
		// creates the panel with this frame so title can be set
		panel = new SBOLDesignerPanel(this);
		// Only ask for a URI prefix if the current one is
		// "http://www.dummy.org"
		panel.newPart(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString().equals("http://www.dummy.org"),
				true);

		setContentPane(panel);
		setLocationRelativeTo(null);
		setSize(800, 600);
		setIconImage(ImageIO.read(getClass().getResourceAsStream("/com/clarkparsia/sbol/editor/images/icon.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// set behavior for close operation
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					if (panel.confirmSave()) {
						System.exit(0);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) throws SBOLValidationException, IOException {
		setup();

		final SBOLDesignerStandalone frame = new SBOLDesignerStandalone();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);

		if (args.length > 0) {
			try {
				File file = new File(args[0]);
				Preferences.userRoot().node("path").put("path", file.getPath());
				frame.panel.openDocument(new FileDocumentIO(false));
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
		final InputStream inputStream = SBOLDesignerStandalone.class.getResourceAsStream("/logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final Exception e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
}
