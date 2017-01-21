package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class LoadingDialog {

	private JDialog dialog;

	public LoadingDialog(Component parent) throws IOException {
		// Starts a progress loading indicator
		dialog = new JDialog();
		ImageIcon loading = new ImageIcon(getClass().getResource("/com/clarkparsia/sbol/editor/images/loading.gif"));
		JLabel label = new JLabel(loading);
		dialog.add(label, BorderLayout.CENTER);
		dialog.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
	}

	public void start() {
		dialog.setVisible(true);
	}

	public void stop() {
		dialog.setVisible(false);
		dialog.dispose();
	}
}
