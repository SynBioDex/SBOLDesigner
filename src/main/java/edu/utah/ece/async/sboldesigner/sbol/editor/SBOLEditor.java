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

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.common.eventbus.EventBus;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.MessageDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.event.ThumbnailVisibilityChangedEvent;
import edu.utah.ece.async.sboldesigner.swing.InvisibleSplitPane;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLEditor extends JPanel {
	private final EventBus eventBus;
	private final SBOLDesign design;
	private final PartsPanel toolbar;
	private final OverviewPanel thumbnails;
	private final InvisibleSplitPane top;
	private final boolean editable;
	private JFileChooser snapshotFileChooser;

	public SBOLEditor(boolean isEditable){
		super(new BorderLayout());

		editable = isEditable;
		eventBus = new EventBus();
		design = new SBOLDesign(eventBus);
		toolbar = new PartsPanel(this);
		thumbnails = new OverviewPanel(this);

		eventBus.register(this);

		JComponent designPanel = createTitledPanel("Design", design.getPanel(), VERTICAL_SCROLLBAR_AS_NEEDED,
				HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JComponent thumbnailsPanel = createTitledPanel("Thumbnails", thumbnails, VERTICAL_SCROLLBAR_AS_NEEDED,
				HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JComponent partsPanel = createTitledPanel("Parts", toolbar, VERTICAL_SCROLLBAR_NEVER,
				HORIZONTAL_SCROLLBAR_ALWAYS);

		top = new InvisibleSplitPane(JSplitPane.HORIZONTAL_SPLIT, designPanel, thumbnailsPanel);
		top.setBackground(Color.WHITE);
		top.setResizeWeight(1.0);
		top.setBorder(null);
		top.setDividerSize(5);
		add(top, BorderLayout.CENTER);

		add(partsPanel, BorderLayout.SOUTH);

		thumbnailsPanel.setVisible(false);
		top.setDividerVisible(false);
	}

	public void setOverviewVisible(boolean isVisible) {
		Component thumnailsPanel = top.getRightComponent();
		if (thumnailsPanel.isVisible() != isVisible) {
			thumnailsPanel.setVisible(isVisible);
			top.setDividerVisible(isVisible);

			eventBus.post(new ThumbnailVisibilityChangedEvent(isVisible));
		}
	}

	private JComponent createTitledPanel(String title, JComponent contents, int vsbPolicy, int hsbPolicy) {
		contents.setBackground(Color.WHITE);
		contents.setBorder(null);

		if (vsbPolicy != VERTICAL_SCROLLBAR_NEVER || vsbPolicy != HORIZONTAL_SCROLLBAR_NEVER) {
			JScrollPane scroller = new JScrollPane(contents, vsbPolicy, hsbPolicy);
			scroller.setBackground(Color.WHITE);
			contents = scroller;
		}

		contents.setBackground(Color.WHITE);
		contents.setBorder(BorderFactory.createTitledBorder(title));

		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(contents);
		return outerPanel;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public SBOLDesign getDesign() {
		return design;
	}

	public OverviewPanel getThumbnails() {
		return thumbnails;
	}

	public boolean isEditable() {
		return editable;
	}

	public void takeSnapshot() {
		String[] buttons = { "Copy to clipboard", "Save to file" };
		int returnValue = JOptionPane.showOptionDialog(this, "What do you want to do with the snapshot of the design?",
				"Take a snapshot", JOptionPane.INFORMATION_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, buttons,
				buttons[1]);

		BufferedImage image = design.getSnapshot();

		switch (returnValue) {
		case 0:
			// Copy to clipboard
			Images.copyToClipboard(image);
			break;
		case 1:
			// Save to file
			JFileChooser fc = getSnapshotFileChooser();
			int saveFile = fc.showSaveDialog(this);
			if (saveFile == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					String format = fc.getFileFilter().getDescription();
					String formatExt = "." + format.toLowerCase();
					String fileName = file.getName();
					if (!fileName.contains(".")) {
						file = new File(file + formatExt);
					}

					ImageIO.write(image, format, file);
				} catch (Exception ex) {
					MessageDialog.showMessage(this, "Error saving image: ", ex.getMessage());
				}
			}
			break;
		case JOptionPane.CLOSED_OPTION:
			// Cancel
			break;
		default:
			throw new IllegalArgumentException("Getting a snapshot failed");
		}
	}

	private JFileChooser getSnapshotFileChooser() {
		if (snapshotFileChooser == null) {
			snapshotFileChooser = new JFileChooser(new File("."));
			snapshotFileChooser.setMultiSelectionEnabled(false);
			snapshotFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String[] formats = new String[] { "gif", "jpg", "png" };
			for (String format : formats) {
				snapshotFileChooser.setFileFilter(new FileNameExtensionFilter(format.toUpperCase(), format));
			}
		}

		return snapshotFileChooser;
	}
}
