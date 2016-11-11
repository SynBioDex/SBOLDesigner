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
import javax.swing.SwingWorker;

import org.sbolstack.frontend.ComponentMetadata;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.editor.Images;

public class SBOLStackQuery extends SwingWorker<ArrayList<ComponentMetadata>, Object> {

	StackFrontend stack;
	Set<URI> roles;
	LoadingDialog loadingFrame;

	public SBOLStackQuery(StackFrontend stack, Set<URI> roles) {
		this.stack = stack;
		this.roles = roles;
		// TODO not showing up properly
		// loadingFrame = new LoadingDialog();
	}

	@Override
	protected ArrayList<ComponentMetadata> doInBackground() throws Exception {
		ArrayList<ComponentMetadata> result = stack.searchComponentMetadata(null, roles, null, null);
		// loadingFrame.setVisible(false);
		return result;
	}
}
