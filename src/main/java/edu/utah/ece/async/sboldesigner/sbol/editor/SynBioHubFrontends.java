package edu.utah.ece.async.sboldesigner.sbol.editor;

import java.util.Collection;
import java.util.HashMap;

import org.synbiohub.frontend.SynBioHubFrontend;

/**
 * Represents the SynBioHubFrontends that the user is currently logged into.
 * This is used instead of SBOLDocument's registries map because this will
 * persist across multiple SBOLDocuments.
 */
public class SynBioHubFrontends {
	private static HashMap<String, SynBioHubFrontend> frontends = null;

	public SynBioHubFrontends() {
		if (frontends == null) {
			frontends = new HashMap<>();
		}
	}

	public boolean hasFrontend(String url) {
		return frontends.containsKey(url);
	}

	public SynBioHubFrontend getFrontend(String url) {
		return frontends.get(url);
	}

	public Collection<SynBioHubFrontend> getFrontends() {
		return frontends.values();
	}

	public void addFrontend(String url, SynBioHubFrontend frontend) {
		frontends.put(url, frontend);
	}

	public SynBioHubFrontend removeFrontend(String url) {
		return frontends.remove(url);
	}
}
