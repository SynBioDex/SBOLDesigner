package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	Set<URI> collections;
	TableUpdater tableUpdater;
	ArrayList<IdentifiedMetadata> identified;
	LoadingDialog loading;

	public SBOLStackQuery(StackFrontend stack, Set<URI> roles, Set<URI> types, Set<URI> collections,
			TableUpdater tableUpdater, Component parent) throws IOException {
		this.stack = stack;
		this.roles = roles;
		this.types = types;
		for (URI uri : collections) {
			if (uri == null || uri.toString().equals("")) {
				// a uri of "" means "all collections"
				collections = new HashSet<URI>();
				break;
			}
		}
		this.collections = collections;
		this.tableUpdater = tableUpdater;
		this.loading = new LoadingDialog(parent);
		this.identified = new ArrayList<IdentifiedMetadata>();
	}

	@Override
	protected ArrayList<IdentifiedMetadata> doInBackground() throws Exception {
		loading.start();
		// fetch collections
		if (collections.isEmpty()) {
			identified.addAll(stack.fetchRootCollectionMetadata());
		} else {
			for (URI collection : collections) {
				try {
					identified.addAll(stack.fetchSubCollectionMetadata(collection));
				} catch (StackException e1) {
					JOptionPane.showMessageDialog(null, "There was a problem fetching collections: " + e1.getMessage());
					e1.printStackTrace();
				}
			}
		}
		identified.addAll(stack.searchComponentMetadata(null, roles, types, collections, null, null));
		return identified;
	}

	@Override
	protected void done() {
		tableUpdater.updateTable(identified);
		loading.stop();
	}
}
