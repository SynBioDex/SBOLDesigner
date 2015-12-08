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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.sbolstandard.core.DnaComponent;

import com.adamtaft.eb.EventHandler;
import com.clarkparsia.sbol.editor.event.DesignLoadedEvent;
import com.clarkparsia.sbol.editor.event.FocusInEvent;
import com.clarkparsia.sbol.editor.event.FocusOutEvent;

/**
 * 
 * @author Evren Sirin
 */
public class ThumbnailsPanel extends JPanel {
	private int WIDTH = 150;
	private final SBOLEditor editor;

	private int count = 0;

	public ThumbnailsPanel(SBOLEditor editor) {
		this.editor = editor;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));
		setPreferredSize(new Dimension(WIDTH, WIDTH));
		add(Box.createVerticalGlue());

		editor.getEventBus().subscribe(this);
	}

	private JComponent createButton(final DnaComponent comp, final Image image) {
		final int width = image.getWidth(null);
		final int height = image.getHeight(null);
		// JComponent button = Buttons.createButton("", new ImageIcon(Images.scaleImage(img, 0.6))) {
		//
		// };
		final JPanel button = new JPanel() {

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				double scale = Math.min(0.8, (double)getWidth()/width);
				g.drawImage(image, 0, 0, (int) (width*scale), (int) (height*scale), this);
//				g.drawImage(Images.scaleImage(image, scale), 0, 0, null);
			}

		};
		button.setOpaque(false);
		button.putClientProperty("comp", comp);
		 
		final DnaComponent parentComponent = editor.getDesign().getParentComponent();
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.getDesign().focusOut(parentComponent);
			}
		});

		return button;
	}
	
	private void addButton(final DnaComponent comp, final Image image) {
		add(createButton(comp, image), count++);
	}

	@EventHandler
	public void designLoaded(DesignLoadedEvent event) {
		while (count > 0) {
			remove(--count);
		}
		repaint();
	}

	@EventHandler
	public void focusedIn(FocusInEvent event) {
		addButton(event.getComponent(), event.getSnapshot());
	}

	@EventHandler
	public void focusedOut(FocusOutEvent event) {
		DnaComponent comp = event.getComponent();
		while (count > 0) {
			JComponent button = (JComponent) getComponent(count - 1);
			if (comp != button.getClientProperty("comp")) {
				remove(--count);
			}
			else {
				break;
			}
		}
		repaint();
	}
}
