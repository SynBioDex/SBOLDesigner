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
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

public class FormBuilder {
	private JPanel panel = new JPanel(new SpringLayout());	
	
	public JPanel build() {	
		int cols = 2;
		int rows = panel.getComponentCount() / 2;
		int initX = 6, initY = 6;
		int padX = 6, padY = 6;
		makeCompactGrid(panel, rows, cols, initX, initY, padX, padY);
		
		return panel;
	}
	
	public void add(String labelText, JComponent component) {
        JLabel label = new JLabel(labelText, JLabel.TRAILING);
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setLabelFor(component);
        panel.add(label);
        panel.add(component);
	}
	
	public void add(String label, JTextComponent component, String value) {
    	component.setText(value);
    	add(label, component);
	}
	
	public JTextField addTextField(String label,  String value) {
		JTextField field = new JTextField();    	
    	add(label, field, value);
    	return field;
	}
	
	public JTextArea addTextArea(String label,  String value) {
		JTextArea field = new JTextArea();    
		field.setLineWrap(true);
		
		JScrollPane scroller = new JScrollPane(field);
		scroller.setPreferredSize(new Dimension(350, 75));
		scroller.setAlignmentX(Component.LEFT_ALIGNMENT);		
		
		add(label, scroller);
    	
    	return field;
	}

	
	public JPasswordField addPasswordField(String label,  String value) {
		JPasswordField field = new JPasswordField();    	
    	add(label, field, value);
    	return field;
	}

	public void add(String label, JComponent... components) {
		JComponent component;
		if (components.length == 1) {
			component = components[0];
		}
		else {
			Box box = Box.createHorizontalBox();
			for (JComponent c : components) {
				box.add(c);
			}
			component = box;
		}
		add(label, component);
	}	

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
                                                int row, int col,
                                                Container parent,
                                                int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }
 
    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }
 
        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                                   getConstraintsForCell(r, c, parent, cols).
                                       getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }
 
        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                                    getConstraintsForCell(r, c, parent, cols).
                                        getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }
 
        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}

