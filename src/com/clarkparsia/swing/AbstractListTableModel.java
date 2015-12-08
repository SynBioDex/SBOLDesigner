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

import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractListTableModel<T> extends AbstractTableModel {
	private final String[] columns;
	private final double[] widths;
	private List<T> elements;

	public AbstractListTableModel(List<T> components, String[] columns, double[] widths) {
		super();
		this.elements = components;
		this.columns = columns;
		this.widths = widths;
	}
	
	public double[] getWidths() {
		return widths;
	}

	public void setElements(List<T> components) {
		this.elements = components;
		fireTableDataChanged();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public int getRowCount() {
		return elements.size();
	}

	public String getColumnName(int col) {
		return columns[col];
	}

	public T getElement(int row) {
		return elements.get(row);
	}

	public Class<?> getColumnClass(int col) {
		return Object.class;
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public Object getValueAt(int row, int col) {
		T element = getElement(row);
		return getField(element, col);
	}
	
	protected abstract Object getField(T element, int field);
}