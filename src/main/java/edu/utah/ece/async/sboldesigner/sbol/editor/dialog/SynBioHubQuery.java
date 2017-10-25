package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SearchCriteria;
import org.synbiohub.frontend.SearchQuery;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryInputDialog.TableUpdater;

public class SynBioHubQuery extends SwingWorker<Object, Object> {

	public static int QUERY_LIMIT = 10000;

	SynBioHubFrontend synBioHub;
	Set<URI> roles;
	Set<URI> types;
	Set<URI> collections;
	String filterText;
	TableUpdater tableUpdater;
	ArrayList<TableMetadata> identified;
	LoadingDialog loading;

	public SynBioHubQuery(SynBioHubFrontend synbiohub, Set<URI> roles, Set<URI> types, Set<URI> collections,
			String filterText, TableUpdater tableUpdater, Component parent) throws IOException {
		this.synBioHub = synbiohub;
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
		this.filterText = filterText;
		this.tableUpdater = tableUpdater;
		this.loading = new LoadingDialog(parent);
		this.identified = new ArrayList<TableMetadata>();
	}

	@Override
	protected ArrayList<TableMetadata> doInBackground() throws Exception {
		loading.start();

		// collections are empty, so we show only root collections
		if (collections.isEmpty()) {
			ArrayList<IdentifiedMetadata> rootCollections = synBioHub.getRootCollectionMetadata();
			if (!rootCollections.isEmpty()) {
				identified.addAll(getTableMetadata(rootCollections, null));
				return identified;
			}
		}

		// collections aren't empty, or there aren't any root collections
		for (URI collection : collections) {
			try {
				identified.addAll(getTableMetadata(synBioHub.getSubCollectionMetadata(collection), null));
			} catch (SynBioHubException e1) {
				JOptionPane.showMessageDialog(null, "There was a problem fetching collections: " + e1.getMessage());
				e1.printStackTrace();
			}
		}

		// fetch parts
		SearchQuery query = new SearchQuery();
		query.setOffset(0);
		query.setLimit(QUERY_LIMIT);

		for (URI role : roles) {
			SearchCriteria criteria = new SearchCriteria();
			criteria.setKey("role");
			criteria.setValue(role.toString());
			query.addCriteria(criteria);
		}

		for (URI type : types) {
			SearchCriteria criteria = new SearchCriteria();
			criteria.setKey("type");
			criteria.setValue(type.toString());
			query.addCriteria(criteria);
		}

		for (URI collection : collections) {
			SearchCriteria criteria = new SearchCriteria();
			criteria.setKey("collection");
			criteria.setValue(collection.toString());
			query.addCriteria(criteria);
		}

		SearchCriteria objectTypeCriteria = new SearchCriteria();
		objectTypeCriteria.setKey("objectType");
		objectTypeCriteria.setValue("ComponentDefinition");
		query.addCriteria(objectTypeCriteria);

		if (filterText != null) {
			SearchCriteria filterTextCriteria = new SearchCriteria();
			// TODO what's the key here?
			filterTextCriteria.setKey("filterText");
			filterTextCriteria.setValue(filterText);
			query.addCriteria(filterTextCriteria);
		}

		identified.addAll(getTableMetadata(null, synBioHub.search(query)));

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
