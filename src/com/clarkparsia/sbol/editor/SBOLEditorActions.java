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

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;


public class SBOLEditorActions implements Iterable<SBOLEditorAction> {
	private final List<SBOLEditorAction> actions = Lists.newArrayList();

	public SBOLEditorActions add(SBOLEditorAction action) {
		actions.add(action);
		return this;
	}
	
	public SBOLEditorActions add(SBOLEditorAction... actions) {
		for (SBOLEditorAction action : actions) {
	        add(action);
        }
		return this;
	}
	
	public SBOLEditorActions addIf(boolean condition, SBOLEditorAction... actions) {
		if (condition) {
			add(actions);
		}
		return this;
	}
	
	@Override
    public Iterator<SBOLEditorAction> iterator() {
	    return actions.iterator();
    }
}