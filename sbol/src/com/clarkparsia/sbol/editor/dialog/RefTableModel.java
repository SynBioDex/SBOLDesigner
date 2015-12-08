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

import java.util.Date;
import java.util.List;

import com.clarkparsia.swing.AbstractListTableModel;
import com.clarkparsia.versioning.Ref;


class RefTableModel<T extends Ref> extends AbstractListTableModel<T> {
	private static final String[] COLUMNS = { "Name", "Description", "Creator", "Date" };
	private static final double[] WIDTHS = { 0.2, 0.4, 0.2, 0.2 };	
	
	public RefTableModel(List<T> repos) {
        super(repos, COLUMNS, WIDTHS);
    }

	public Object getField(T component, int col) {
		switch (col) {
			case 0:
				return component.getName();
			case 1:
				return component.getActionInfo().getMessage();
			case 2:
				return component.getActionInfo().getAuthor();						
			case 3:
				return component.getActionInfo().getDate().getTime();
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	public Class<?> getColumnClass(int col) {
		return col == 3 ? Date.class : Object.class;
	}
};