/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.utah.ece.async.sboldesigner.sbol.editor;

import java.util.prefs.Preferences;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.SynBioHubQuery;
import edu.utah.ece.async.sboldesigner.versioning.Infos;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

public enum SBOLEditorPreferences {
	INSTANCE;

	public PersonInfo getUserInfo() {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("user");
		String name = prefs.get("name", "");
		String email = prefs.get("email", "");
		String uri = prefs.get("uri", "http://www.dummy.org/");
		PersonInfo userInfo = Infos.forPerson(uri, name, email);

		return userInfo;
	}

	public void saveUserInfo(PersonInfo userInfo) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("user");

		try {
			if (userInfo == null) {
				prefs.removeNode();
			} else {
				prefs.put("uri", userInfo.getURI().toString());
				prefs.put("name", userInfo.getName());
				if (userInfo.getEmail() != null) {
					prefs.put("email", userInfo.getEmail().toString());
				} else {
					prefs.put("email", "");
				}
			}

			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getValidate() {
		return false;
	}

	private Boolean enableBranching = null;
	private Boolean enableVersioning = null;

	public boolean isBranchingEnabled() {
		if (enableBranching == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("versioning");
			enableBranching = prefs.getBoolean("enableBranching", false);
		}

		return enableBranching;
	}

	public void setBranchingEnabled(boolean enableBranching) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("versioning");
		prefs.putBoolean("enableBranching", enableBranching);
	}

	public boolean isVersioningEnabled() {
		if (enableVersioning == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("versioning");
			// versioning is no longer supported
			enableVersioning = prefs.getBoolean("enable", false);
		}

		return enableVersioning;
	}

	public void setVersioningEnabled(boolean enableVersioning) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("versioning");
		prefs.putBoolean("enable", enableVersioning);
	}

	private Integer seqBehavior = null;

	/**
	 * askUser is 0, overwrite is 1, and keep is 2
	 */
	public Integer getSeqBehavior() {
		if (seqBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
			seqBehavior = prefs.getInt("seqBehavior", 1);
		}
		return seqBehavior;
	}

	/**
	 * askUser is 0, overwrite is 1, and keep is 2
	 */
	public void setSeqBehavior(int seqBehavior) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
		prefs.putInt("seqBehavior", seqBehavior);
		this.seqBehavior = seqBehavior;
	}

	private Integer nameDisplayIdBehavior = null;

	/**
	 * show name is 0, show displayId is 1
	 */
	public Integer getNameDisplayIdBehavior() {
		if (nameDisplayIdBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
			nameDisplayIdBehavior = prefs.getInt("nameDisplayIdBehavior", 0);
		}
		return nameDisplayIdBehavior;
	}

	/**
	 * show name is 0, show displayId is 1
	 */
	public void setNameDisplayIdBehavior(int showNameOrDisplayId) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
		prefs.putInt("nameDisplayIdBehavior", showNameOrDisplayId);
		this.nameDisplayIdBehavior = showNameOrDisplayId;
	}
	
	private Integer fileChooserBehavior = null;

	/**
	 * default is 0, mac is 1
	 */
	public Integer getFileChooserBehavior() {
		if (fileChooserBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
			fileChooserBehavior = prefs.getInt("fileChooserBehavior", 0);
		}
		return fileChooserBehavior;
	}

	/**
	 * default is 0, mac is 1
	 */
	public void setFileChooserBehavior(int macOrDefault) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
		prefs.putInt("fileChooserBehavior", macOrDefault);
		this.fileChooserBehavior = macOrDefault;
	}
	
	private Integer CDSBehavior = null;

	/**
	 * default is 0, mac is 1
	 */
	public Integer getCDSBehavior() {
		if (CDSBehavior == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
			CDSBehavior = prefs.getInt("CDSBehavior", 0);
		}
		return CDSBehavior;
	}

	/**
	 * default is 0, mac is 1
	 */
	public void setCDSBehavior(int arrowOrDefault) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
		prefs.putInt("CDSBehavior", arrowOrDefault);
		this.CDSBehavior = arrowOrDefault;
	}
	
	//Query limit for SynBioHubQuery
	private Integer queryLimit = null;

	/**
	 * default is 10,000
	 */
	public Integer getQueryLimit() {
		if (queryLimit == null) {
			Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
			queryLimit = prefs.getInt("queryLimit", 10000);
		}
		return queryLimit;
	}

	public void setQueryLimit(int limit) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("settings");
		prefs.putInt("queryLimit", limit);
		SynBioHubQuery.QUERY_LIMIT = limit;
		this.queryLimit = limit;
	}
}
