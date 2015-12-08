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

package com.clarkparsia.geneious;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.biomatters.geneious.publicapi.plugin.GeneiousAction;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.clarkparsia.sbol.editor.Images;

public class GeneiousActions {

	static GeneiousAction action(final Action action) {
    	return action(action, new Icons((Icon) action.getValue(Action.SMALL_ICON)));
    }

	static GeneiousAction action(final Action action, Icons icons) {
    	GeneiousAction geneiousAction = new GeneiousAction(options(action, icons)) {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			action.actionPerformed(e);
    		}
    	};
    	geneiousAction.setEnabled(action.isEnabled());
    	return geneiousAction;
    }
	
	static GeneiousActionOptions options(String name, String description, String icon) {
		return new GeneiousActionOptions(name, description, icon(icon));
	}
	
	static GeneiousActionOptions options(Action action) {
		return options(action, new Icons((Icon) action.getValue(Action.SMALL_ICON)));
	}
	
	static GeneiousActionOptions options(Action action,Icons icons) {
		return new GeneiousActionOptions(action.getValue(Action.NAME).toString(),
    					action.getValue(Action.SHORT_DESCRIPTION).toString(), new Icons((Icon) action.getValue(Action.SMALL_ICON)));
	}
	
	static Icons icon(String file) {
    	try {
            return new Icons(new ImageIcon(Images.getActionImage(file)));
        }
        catch (Exception e) {
        	System.err.println("Cannot load icon " + file);
            return null;
        }
    }

}
