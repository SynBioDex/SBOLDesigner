package edu.utah.ece.aync.sboldesigner.sbol.editor.dialog;

import org.synbiohub.frontend.IdentifiedMetadata;

/**
 * A wrapper for IdentifiedMetadata that also knows if it's a collection
 */
public class TableMetadata {

	public boolean isCollection;

	public IdentifiedMetadata identified;

	public TableMetadata(IdentifiedMetadata identified, boolean isCollection) {
		this.identified = identified;
		this.isCollection = isCollection;
	}
}
