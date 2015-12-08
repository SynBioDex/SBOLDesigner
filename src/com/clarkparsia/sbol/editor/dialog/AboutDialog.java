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

package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.clarkparsia.sbol.editor.SBOLDesignerMetadata;

public class AboutDialog {
	private AboutDialog() {
	}

	public static void show(final Component parent) {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		// html content
		JEditorPane ep = new JEditorPane(
		                "text/html",
		                "<html><body style=\"" + style+ "\">"
                        + SBOLDesignerMetadata.NAME + " v" + SBOLDesignerMetadata.VERSION
                        + "<br><br>"
                        + "See <a href='" + SBOLDesignerMetadata.HOME_PAGE + "'>" + SBOLDesignerMetadata.HOME_PAGE + "</a> for more info.<br><br>"
                        + "Send your questions and comments to <a href='mailto:" + SBOLDesignerMetadata.EMAIL + "'>" + SBOLDesignerMetadata.EMAIL + "</a>."
                        + "<br><br>" + "Copyright &copy; 2013 " + SBOLDesignerMetadata.AUTHORS.replace("&", "&amp;") 
                        + "</html>");

		// handle link events
		ep.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					if (java.awt.Desktop.isDesktopSupported()) {
						java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

						if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
							try {
								desktop.browse(e.getURL().toURI());
								return;
							}
							catch (Exception e1) {
							}
						}
					}

					JOptionPane.showMessageDialog(parent,
					                "Cannot open the URL in your browser, please type in the address in your browser manually.");
				}
			}
		});
		ep.setEditable(false);
		ep.setBackground(label.getBackground());

		// show
		JOptionPane.showMessageDialog(parent, ep, "About", JOptionPane.PLAIN_MESSAGE);
	}
}
