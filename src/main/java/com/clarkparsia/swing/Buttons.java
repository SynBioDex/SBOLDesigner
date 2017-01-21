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

package com.clarkparsia.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 * Utility functions for dealing with images.
 * 
 * @author Evren Sirin
 */
public class Buttons {
	public static JButton createButton(String text, Icon icon) {
		return setStyle(new JButton(text, icon));
	}
	
	public static JButton createButton(Action action) {
		return setStyle(new JButton(action));
	}
	
	public static JButton setStyle(JButton button) {
		button.addMouseListener(BUTTON_ROLLOVER);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		return button;
	}
	
	public static JToggleButton createToggleButton(String text, Icon icon) {
		final JToggleButton button = new JToggleButton(text, icon);
		return button;
	}
	
	public static JToggleButton createToggleButton(Action action) {
		final JToggleButton button = new JToggleButton(action);
		button.setFocusPainted(false);
		return button;
	}
	

	private static final MouseListener BUTTON_ROLLOVER = new MouseAdapter() {
		public void mouseEntered(MouseEvent event) {
			AbstractButton button = (AbstractButton) event.getSource();
			if (button.isEnabled()) {
				button.setBorderPainted(true);
				button.setContentAreaFilled(true);
			}
		}

		public void mouseExited(MouseEvent event) {
			AbstractButton button = (AbstractButton) event.getSource();
			button.setBorderPainted(false);
			button.setContentAreaFilled(false);
		}
	};

}
