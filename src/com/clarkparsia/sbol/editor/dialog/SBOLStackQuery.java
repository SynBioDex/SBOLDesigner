package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.sbolstack.frontend.ComponentMetadata;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.editor.Images;

public class SBOLStackQuery implements Runnable {

	StackFrontend stack;
	Set<URI> roles;
	private volatile ArrayList<ComponentMetadata> result;

	public SBOLStackQuery(StackFrontend stack, Set<URI> roles) {
		this.stack = stack;
		this.roles = roles;
	}

	@Override
	public void run() {
		// Starts a progress loading indicator
		// TODO use loading.gif in images folder
		// TODO loading is buggy
		JFrame loadingFrame = new JFrame("Loading...");
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		final JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new JLabel("Loading..."), BorderLayout.NORTH);
		contentPane.add(progressBar, BorderLayout.CENTER);
		loadingFrame.setContentPane(contentPane);
		loadingFrame.pack();
		// loadingFrame.setLocationRelativeTo(null);
		loadingFrame.setVisible(true);

		try {
			result = stack.searchComponentMetadata(null, roles, null, null);
		} catch (StackException e) {
			e.printStackTrace();
		}

		loadingFrame.setVisible(false);
	}

	public ArrayList<ComponentMetadata> getResult() {
		return result;
	}
}
