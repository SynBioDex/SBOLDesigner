package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SelectionDialog extends JDialog implements ActionListener {

	private final JButton selectButton;
	private final JComboBox options;
	private final JLabel message;
	private int selection = -1;

	/**
	 * Returns the int of the index of the array that was selected. Returns -1
	 * if closed.
	 */
	public static int selectFrom(Component parent, String message, String title, Object[] options,
			Object defaultOption) {
		// TODO should make this overall bigger
		SelectionDialog dialog = new SelectionDialog(parent, message, title, options, defaultOption);
		dialog.selectButton.setEnabled(true);
		dialog.setVisible(true);
		return dialog.selection;
	}

	private SelectionDialog(Component parent, String message, String title, Object[] options, Object defaultOption) {
		super(JOptionPane.getFrameForComponent(parent), title, true);

		this.message = new JLabel(message);

		this.options = new JComboBox(options);
		this.options.setSelectedItem(defaultOption);

		selectButton = new JButton("Select");
		selectButton.addActionListener(this);
		getRootPane().setDefaultButton(selectButton);

		Container panel = getContentPane();
		panel.add(this.message, BorderLayout.PAGE_START);
		panel.add(this.options, BorderLayout.CENTER);
		panel.add(selectButton, BorderLayout.PAGE_END);

		pack();
		setLocationRelativeTo(parent);
		this.options.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectButton) {
			selection = options.getSelectedIndex();
			setVisible(false);
		}
	}

}
