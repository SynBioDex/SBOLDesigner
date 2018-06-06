package edu.utah.ece.async.sboldesigner.sbol.boost;

import java.util.prefs.Preferences;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

public class BOOSTPreferences {

	/**
	 * save BOOST JWT Token in to Preferences
	 **/
	public void setBOOSTToken(String boostToken) {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("token");
		prefs.put("boostJWTToken", boostToken);
	}
	
	/**
	 * get JWT Token from Preferences
	 */
	public String getBOOSTToken() {
		Preferences prefs = Preferences.userNodeForPackage(SBOLEditorPreferences.class).node("token");
		return prefs.get("boostJWTToken", "");
	}
}
