package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingWorker;

import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SearchCriteria;
import org.synbiohub.frontend.SearchQuery;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryInputDialog.TableUpdater;

public class SynBioHubQuery extends SwingWorker<Object, Object> {

	public static int QUERY_LIMIT = 10000;
	static SynBioHubQuery lastQuery = null;
	private boolean cancelled = false;

	SynBioHubFrontend synBioHub;
	Set<URI> roles;
	Set<URI> types;
	Set<URI> collections;
	String filterText;
	TableUpdater tableUpdater;
	ArrayList<TableMetadata> identified;
	LoadingDialog loading;
	String objectType;
	Boolean isRoot;

	public SynBioHubQuery(SynBioHubFrontend synbiohub, Set<URI> roles, Set<URI> types, Set<URI> collections,
			String filterText, String objectType, TableUpdater tableUpdater, Component parent) throws IOException {
		this.synBioHub = synbiohub;
		this.roles = roles;
		this.types = types;
		Boolean isRoot = false;
		for (URI uri : collections) {
			if (uri.toString().equals("http://RootCollections")) {
				isRoot = true;
				collections = new HashSet<URI>();
				break;
			}else if(uri.toString().equals("http://AllCollections")) {
				collections = new HashSet<URI>();
				break;
			}
		}
		this.isRoot = isRoot;
		this.collections = collections;
		this.filterText = filterText;
		this.objectType = objectType;
		this.tableUpdater = tableUpdater;
		this.loading = new LoadingDialog(parent);
		this.identified = new ArrayList<TableMetadata>();
	}

	@Override
	protected ArrayList<TableMetadata> doInBackground() throws Exception {
		cancelPrevious();
		loading.start();

		// collections are empty, so we show only root collections
		if (isRoot) {
			ArrayList<IdentifiedMetadata> rootCollections = synBioHub.getRootCollectionMetadata();
			if (!rootCollections.isEmpty()) {
				identified.addAll(getTableMetadata(rootCollections, null));
				return identified;
			}
		}

		// collections aren't empty, or there aren't any root collections
		if (objectType != null && objectType !="" && objectType != "Collection") {
			for (URI collection : collections) {
				try {
					identified.addAll(getTableMetadata(synBioHub.getSubCollectionMetadata(collection), null));
				} catch (SynBioHubException e1) {
					MessageDialog.showMessage(null, "There was a problem fetching collections: ", e1.getMessage());
					e1.printStackTrace();
				}
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
		
		if (filterText != null && filterText != "") {
			SearchCriteria filterTextCriteria = new SearchCriteria();
			filterTextCriteria.setKey("name");
			filterTextCriteria.setValue(filterText);
			query.addCriteria(filterTextCriteria);
		}

		if (objectType != null && objectType != "") {
			SearchCriteria objectTypeCriteria = new SearchCriteria();
			objectTypeCriteria.setKey("objectType");
			objectTypeCriteria.setValue(objectType);
			query.addCriteria(objectTypeCriteria);
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
	
	private void cancelPrevious() {
		if (lastQuery!=null) {
			lastQuery.loading.stop();
			lastQuery.cancelled = true;
		}
		lastQuery = this;
	}

	@Override
	protected void done() {
		if(!cancelled) {
			loading.stop();
			tableUpdater.updateTable(identified, filterText);
		}
	}
}
