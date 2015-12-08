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

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * A split pane that does not paint anything
 */
public class InvisibleSplitPane extends JSplitPane {
	private static final long serialVersionUID = 429222851935165219L;

	/**
	 * {@inheritDoc}
	 */
	public InvisibleSplitPane() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public InvisibleSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent,
	                Component newRightComponent) {
		super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvisibleSplitPane(int newOrientation, boolean newContinuousLayout) {
		super(newOrientation, newContinuousLayout);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvisibleSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
		super(newOrientation, newLeftComponent, newRightComponent);
	}

	/**
	 * {@inheritDoc}
	 */
	public InvisibleSplitPane(int newOrientation) {
		super(newOrientation);
	}

	/**
	 * Notification from the <code>UIManager</code> that the L&F has changed. Replaces the current UI object with the
	 * latest version from the <code>UIManager</code>.
	 * 
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		SplitPaneUI ui = new InvisibleSplitPaneUI();
		setUI(ui);
		revalidate();
	}
	
	public void setDividerVisible(boolean isVisible) {
		SplitPaneUI ui = getUI();
		if (ui instanceof InvisibleSplitPaneUI) {
			((InvisibleSplitPaneUI) ui).getDivider().setVisible(isVisible);
		}
	}

	/**
	 * The look and feel UI that draws nothing
	 */
	private class InvisibleSplitPaneUI extends BasicSplitPaneUI {
		/**
		 * Create the UI
		 */
		public InvisibleSplitPaneUI() {

		}

		/**
		 * Installs the UI defaults.
		 */
		protected void installDefaults() {
			super.installDefaults();

			splitPane.setBorder(null);
		}

		/**
		 * Creates the default divider.
		 */
		public BasicSplitPaneDivider createDefaultDivider() {
			BasicSplitPaneDivider d = new BasicSplitPaneDivider(this) {
				private static final long serialVersionUID = 225334791139486944L;

				public void paint(Graphics g) {

				}
			};
			d.setBorder(null);
			return d;
		}
	}
}