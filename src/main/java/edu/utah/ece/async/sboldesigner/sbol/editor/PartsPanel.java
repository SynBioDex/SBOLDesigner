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

package edu.utah.ece.async.sboldesigner.sbol.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.sbolstandard.core2.SBOLValidationException;

import com.adamtaft.eb.EventHandler;
import com.google.common.collect.Maps;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.MessageDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.DesignChangedEvent;

/**
 * 
 * @author Evren Sirin
 */
public class PartsPanel extends JPanel {
	private final SBOLDesign design;
	private final Map<Part, JButton> buttons = Maps.newHashMap();

	public PartsPanel(SBOLEditor editor) {
		super();

		design = editor.getDesign();

		for (Part part : Parts.all()) {
			// don't show the un specified part in the parts panel
			if (part != Parts.UNSPECIFIED) {
				addPartButton(part);
			}
		}

		editor.getEventBus().subscribe(this);
	}

	private void addPartButton(final Part part) {
		JButton button = createPartButton(part);
		add(button);

		buttons.put(part, button);
	}

	private JButton createPartButton(final Part part) {
		JButton button = new JButton();
		button.setText(part.getDisplayId());
		button.setIcon(new ImageIcon(part.getImage()));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setToolTipText(part.getName());
		button.setFocusPainted(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					design.addCD(part, part == Parts.UNSPECIFIED);
				} catch (SBOLValidationException e) {
					MessageDialog.showMessage(null, "There was an error: ", e.getMessage());
					e.printStackTrace();
				}
			}
		});
		return button;
	}

	@EventHandler
	public void designChanged(DesignChangedEvent event) {
		buttons.get(Parts.CIRCULAR).setEnabled(!event.getDesign().isCircular());
	}
}
