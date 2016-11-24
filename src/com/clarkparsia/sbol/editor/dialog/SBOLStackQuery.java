package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
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

import org.sbolstack.frontend.IdentifiedMetadata;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.editor.Images;
import com.clarkparsia.sbol.editor.dialog.RegistryInputDialog.TableUpdater;

public class SBOLStackQuery extends SwingWorker<Object, Object> {

	StackFrontend stack;
	Set<URI> roles;
	Set<URI> types;
	TableUpdater tableUpdater;
	ArrayList<IdentifiedMetadata> components;
	LoadingDialog loading;

	public SBOLStackQuery(StackFrontend stack, Set<URI> roles, Set<URI> types, TableUpdater tableUpdater,
			Component parent) throws IOException {
		this.stack = stack;
		this.roles = roles;
		this.types = types;
		this.tableUpdater = tableUpdater;
		this.loading = new LoadingDialog(parent);
	}

	@Override
	protected ArrayList<IdentifiedMetadata> doInBackground() throws Exception {
		loading.start();
		this.components = stack.searchComponentMetadata(null, roles, types, new HashSet<URI>(), null, null);
		return components;
	}

	@Override
	protected void done() {
		tableUpdater.updateTable(components);
		loading.stop();
	}
}
