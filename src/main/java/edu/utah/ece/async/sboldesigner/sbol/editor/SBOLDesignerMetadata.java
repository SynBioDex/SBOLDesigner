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

import java.util.Properties;

//import org.sbolstandard.core2.SBOLVersion;

public class SBOLDesignerMetadata {
	private SBOLDesignerMetadata() {
	};

	public static final String NAME = "SBOLDesigner";

	public static final String AUTHORS = "the University of Utah, the University of Washington, and Clark & Parsia, LLC";

	public static final String HOME_PAGE = "http://www.async.ece.utah.edu/SBOLDesigner";

	public static final String EMAIL = "michael13162@gmail.com";

	public static final String VERSION = readVersion();

	private static String readVersion() {
		Properties versionProperties = new Properties();

		// InputStream vstream =
		// SBOLVersion.class.getResourceAsStream("/plugin.properties");
		// if (vstream != null) {
		// try {
		// versionProperties.load(vstream);
		// }
		// catch (IOException e) {
		// System.err.println("Could not load version properties:");
		// e.printStackTrace();
		// }
		// finally {
		// try {
		// vstream.close();
		// }
		// catch (IOException e) {
		// System.err.println("Could not close version properties:");
		// e.printStackTrace();
		// }
		// }
		// }

		return versionProperties.getProperty("plugin-version", "3");
	}
}
