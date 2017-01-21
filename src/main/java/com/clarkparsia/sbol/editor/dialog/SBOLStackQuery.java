package com.clarkparsia.sbol.editor.dialog;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.sbolstack.frontend.IdentifiedMetadata;
import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;

import com.clarkparsia.sbol.editor.dialog.RegistryInputDialog.TableUpdater;

public class SBOLStackQuery extends SwingWorker<Object, Object> {

	StackFrontend stack;
	Set<URI> roles;
	Set<URI> types;
	Set<URI> collections;
	TableUpdater tableUpdater;
	ArrayList<TableMetadata> identified;
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
		this.identified = new ArrayList<TableMetadata>();
	}

	@Override
	protected ArrayList<TableMetadata> doInBackground() throws Exception {
		loading.start();
		// fetch collections
		if (collections.isEmpty()) {
			identified.addAll(getTableMetadata(stack.searchRootCollectionMetadata(), null));
		} else {
			for (URI collection : collections) {
				try {
					identified.addAll(getTableMetadata(stack.searchSubCollectionMetadata(collection), null));
				} catch (StackException e1) {
					JOptionPane.showMessageDialog(null, "There was a problem fetching collections: " + e1.getMessage());
					e1.printStackTrace();
				}
			}
		}
		// fetch parts
		identified.addAll(getTableMetadata(null,
				stack.searchComponentDefinitionMetadata(null, roles, types, collections, null, null)));
		return identified;
	}

	/**
	 * Takes a list of part metadata and collection metadata and returns a
	 * single list of table metadata
	 */
	private List<TableMetadata> getTableMetadata(List<IdentifiedMetadata> collectionMeta,
			List<IdentifiedMetadata> partMeta) {
		List<TableMetadata> tableMeta = new ArrayList<TableMetadata>();
		if (collectionMeta != null) {
			for (IdentifiedMetadata meta : collectionMeta) {
				tableMeta.add(new TableMetadata(meta, true));
			}
		}
		if (partMeta != null) {
			for (IdentifiedMetadata meta : partMeta) {
				tableMeta.add(new TableMetadata(meta, false));
			}
		}
		return tableMeta;
	}

	@Override
	protected void done() {
		loading.stop();
		tableUpdater.updateTable(identified);
	}
}
