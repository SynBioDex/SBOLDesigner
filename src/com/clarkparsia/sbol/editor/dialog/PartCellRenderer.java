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
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.clarkparsia.sbol.editor.Part;
import com.clarkparsia.sbol.editor.Parts;
import com.clarkparsia.swing.ComboBoxRenderer;

public class PartCellRenderer extends ComboBoxRenderer<Part> {
	@Override
	protected Icon getImage(Part item) {
		if (item.getImage() == null) {
			return new InvisibleIcon();
		}
		return new ImageIcon(item.getImage());
	}

	private static class InvisibleIcon implements Icon {
		private static final int DEFAULT_WIDTH = Parts.GENERIC.getImage().getWidth(null);

		private static final int DEFAULT_HEIGHT = Parts.GENERIC.getImage().getHeight(null);

		public int getIconHeight() {
			return DEFAULT_HEIGHT;
		}

		public int getIconWidth() {
			return DEFAULT_WIDTH;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.clearRect(x, y, getIconWidth(), getIconHeight());
		}
	}
}
