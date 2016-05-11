package com.clarkparsia.sbol.editor.dialog;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbolstandard.core2.ComponentDefinition;

import com.clarkparsia.sbol.editor.Part;

/**
 * A dialog for searching for a part in the SBOL Stack given a specific role.
 * 
 * @author Michael Zhang
 */
public class SBOLStackDialog extends JDialog implements ActionListener{

	private static final String TITLE = "SBOL Stack";
	private JButton selectButton;
	private ComponentDefinition selectedCD;

	public SBOLStackDialog(Container parent, Part part) {
		super(JOptionPane.getFrameForComponent(parent), TITLE, true);
		URI role = part.getRole();
		// TODO finish this
		
		setVisible(true);
	}

	public ComponentDefinition getSelection() {
		return selectedCD;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectButton) {
			setVisible(false);
			return;
		}
		// otherwise set selectedCD
	}

}
