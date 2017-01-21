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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ComboBoxRenderer<T> extends JLabel implements ListCellRenderer {
	public ComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
	                boolean cellHasFocus) {
		if (isSelected || (index == list.getSelectedIndex())) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		@SuppressWarnings("unchecked")
        T item = (T) value;
		if (item == null) {
			setText("");
			setToolTipText("");
			setIcon(null);
		}
		else {
			setText(getLabel(item));
			setToolTipText(getToolTip(item));
			
			Icon icon = getImage(item);
			boolean isExpanded = (index != -1);
			if (isExpanded || !isOnlyExpandedIcon()) {
				setIcon(icon);
			}
			else {
				setIcon(null);
			}
		}

		return this;
	}
	
	protected String getLabel(T item) {
		return item.toString();
	}
	
	protected String getToolTip(T item) {
		return "";
	}
	
	protected Icon getImage(T item) {
		return null;
	}
	
	protected boolean isOnlyExpandedIcon() {
		return true;
	}
}