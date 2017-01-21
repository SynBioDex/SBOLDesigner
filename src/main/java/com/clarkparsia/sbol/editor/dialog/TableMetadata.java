package com.clarkparsia.sbol.editor.dialog;

import org.sbolstack.frontend.IdentifiedMetadata;

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
