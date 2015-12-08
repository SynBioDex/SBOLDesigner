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

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import com.clarkparsia.swing.Buttons;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class SBOLEditorAction extends AbstractAction {
	protected boolean isToggle = false;
	protected boolean allowed = true;
	protected Supplier<Boolean> precondition = Suppliers.ofInstance(Boolean.TRUE);
	
	public static final SBOLEditorAction DIVIDER = new SBOLEditorAction("", "", "") {		
		@Override
		protected void perform() {
			throw new UnsupportedOperationException();
		}
	};
	
	public static final SBOLEditorAction SPACER = new SBOLEditorAction("", "", "") {		
		@Override
		protected void perform() {
			throw new UnsupportedOperationException();
		}
	};
	
	public SBOLEditorAction(String description, String image) {
		this("", description, image);
	}
	
	public SBOLEditorAction(String name, String description, String image) {
        super(name, createIcon(image));
        
        putValue(SHORT_DESCRIPTION, description);
    }
	
	static ImageIcon createIcon(String imageFile) {			
		Image image = Images.getActionImage(imageFile);
		if (image != null) {
			image = Images.scaleImageToWidth(image, 16);
			return new ImageIcon(image);
		}
		return null;
	}

	public SBOLEditorAction toggle() {
		isToggle = true;
		return this;
	}
	
	public SBOLEditorAction precondition(Supplier<Boolean> precondition) {
		this.precondition = precondition;
		return this;
	}
	
	public SBOLEditorAction allowed(boolean allowed) {
		this.allowed = allowed;
		return this;
	}
	
	protected abstract void perform();
	
	public final void actionPerformed(ActionEvent e) {
		if (Boolean.TRUE.equals(precondition.get())) {
			perform();
		}			
	}
	
	protected AbstractButton createButton() {
		Preconditions.checkState(allowed, "This action is not allowed");
		final AbstractButton button = isToggle ? Buttons.createToggleButton(this) : Buttons.createButton(this);
		button.setText("");
		return button;
	}		
	
	JMenuItem createMenuItem() {
		Preconditions.checkState(allowed, "This action is not allowed");
		return new JMenuItem(this);
	}
}