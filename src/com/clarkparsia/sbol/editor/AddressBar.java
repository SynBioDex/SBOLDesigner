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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.plaf.metal.MetalToolTipUI;

import org.sbolstandard.core.DnaComponent;

import com.adamtaft.eb.EventHandler;
import com.clarkparsia.sbol.editor.event.DesignChangedEvent;
import com.clarkparsia.sbol.editor.event.DesignLoadedEvent;
import com.clarkparsia.sbol.editor.event.FocusInEvent;
import com.clarkparsia.sbol.editor.event.FocusOutEvent;
import com.clarkparsia.swing.Buttons;

/**
 * 
 * @author Evren Sirin
 */
public class AddressBar extends JToolBar {
	private static final ImageIcon ICON = new ImageIcon(Images.getActionImage("right.png"));
	
	private final SBOLDesign design;
	
	private int count = 0;
	
	public AddressBar(final SBOLEditor editor) {
		this.design = editor.getDesign();
		
		setBorder(BorderFactory.createEtchedBorder());
		
		JButton label = new JButton("Component:");
		label.setBorderPainted(false);
		label.setVisible(false);
		add(label);
			
		add(Box.createHorizontalGlue());
		
		final JToggleButton button = Buttons.createToggleButton("Thumbnails", new ImageIcon(Images.getActionImage("down.png")));
		button.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent event) {
				editor.setThumbnailsVisible(button.isSelected());
			}
		});
		add(button);
		
		editor.getEventBus().subscribe(this);
	}

	private JButton createButton(final DnaComponent comp) {
		JButton button = new JButton(comp.getDisplayId(), ICON) {
			public JToolTip createToolTip() {
				Image image = (Image) getClientProperty("thumbnail");
				JToolTipWithIcon tip = new JToolTipWithIcon(new ImageIcon(image));
				tip.setComponent(this);
				return tip;
			}
		};
		button.putClientProperty("comp", comp);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				design.focusOut(comp);
			}
		});		
		Buttons.setStyle(button);
		return button;
	}
	
	private int idx(int count) {
		return count + 1;
	}
	
	private void addButton(final DnaComponent comp) {
		add(createButton(comp), idx(count++));
	}
	
	@EventHandler
	public void designLoaded(DesignLoadedEvent event) {
		while (count > 0) {
			remove(idx(--count));
		}
		addButton(event.getDesign().getCurrentComponent());
		repaint();
	}
	
	@EventHandler
	public void designChanged(DesignChangedEvent event) {
		JButton button = (JButton) getComponent(idx(count - 1));
		button.setText(event.getDesign().getCurrentComponent().getDisplayId());
	}
	
	@EventHandler
	public void focusedIn(FocusInEvent event) {	
		setToolTip(event.getSnapshot());
		addButton(event.getComponent());
	}
	
	@EventHandler
	public void focusedOut(FocusOutEvent event) {
		DnaComponent comp = event.getComponent();
		while (count > 1) {
			JButton button = (JButton) getComponent(idx(count - 1));
			if (comp != button.getClientProperty("comp")) {
				remove(idx(--count));
			}
			else {
				break;
			}
		}
		
		setToolTip(null);
		
		repaint();
	}
	
	private void setToolTip(BufferedImage image) {
		JComponent comp = (JComponent) getComponent(count);
		comp.putClientProperty("thumbnail", Images.scaleImage(image, 0.5));
		comp.setToolTipText(image == null ? null : "");
	}
	
	public class JToolTipWithIcon extends JToolTip {
	    
	    protected ImageIcon icon;

	    public JToolTipWithIcon(ImageIcon icon) {
	        this.icon = icon ;
	        setUI(new IconToolTipUI()) ;
	    }

	    public JToolTipWithIcon(MetalToolTipUI toolTipUI) {
	        setUI(toolTipUI) ;
	    }

	    private class IconToolTipUI extends BasicToolTipUI {
	        @Override
	        public void paint(Graphics g, JComponent c) {
	            FontMetrics metrics = c.getFontMetrics( c.getFont() ) ;
	            Dimension size = c.getSize() ;
	            g.setColor( c.getBackground() ) ;
	            g.fillRect( 0, 0, size.width, size.height ) ;
	            int x = 3 ;
	            if(icon != null) {
	                icon.paintIcon( c, g, 0, 0 ) ;
	                x += icon.getIconWidth() + 1 ;
	            }
	            g.setColor( c.getForeground() ) ;
	            g.drawString( ((JToolTip)c).getTipText(), x, metrics.getHeight() ) ;
	        }
	        
	        @Override
	        public Dimension getPreferredSize(JComponent c) {
	            return new Dimension(icon.getIconWidth(), icon.getIconHeight());
	        }
	    }
	}
}
