package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadingDialog {

	private JFrame loadingFrame;

	public LoadingDialog() {
		// Starts a progress loading indicator
		// TODO use loading.gif in images folder
		loadingFrame = new JFrame("Loading...");
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		final JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new JLabel("Loading..."), BorderLayout.NORTH);
		contentPane.add(progressBar, BorderLayout.CENTER);
		loadingFrame.setContentPane(contentPane);
		loadingFrame.pack();
		loadingFrame.setLocationRelativeTo(null);
		loadingFrame.toFront();
		loadingFrame.setVisible(true);
	}

	public void setVisible(boolean visible) {
		loadingFrame.setVisible(visible);
	}

}
