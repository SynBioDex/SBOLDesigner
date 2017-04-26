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

package edu.utah.ece.aync.sboldesigner.sbol.editor;

import java.io.Serializable;

import com.google.common.base.Preconditions;

import edu.utah.ece.aync.sboldesigner.sbol.editor.sparql.LocalEndpoint;
import edu.utah.ece.aync.sboldesigner.sbol.editor.sparql.SPARQLEndpoint;
import edu.utah.ece.aync.sboldesigner.sbol.editor.sparql.StardogEndpoint;

public class Registry implements Serializable {
	private final String name;
	private final String description;
	private final String location;
	private final String uriPrefix;

	public static final Registry BUILT_IN = new Registry("Built-in parts",
			"Built-in parts obtained from the iGEM registry", "N/A", "N/A");

	public static final Registry WORKING_DOCUMENT = new Registry("Working document",
			"The current file you are working in", "N/A", "N/A");

	public static final Registry SYNBIOHUB = new Registry("SynBioHub", "The reference SynBioHub instance",
			"http://synbiohub.org", "http://synbiohub.org");

	public Registry(String name, String description, String location, String uriPrefix) {
		Preconditions.checkNotNull(name, "Name cannot be null");
		Preconditions.checkNotNull(location, "URL/Path cannot be null");
		this.name = name;
		this.description = description;
		this.location = location;
		this.uriPrefix = uriPrefix;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	/**
	 * @return the uriPrefix
	 */
	public String getUriPrefix() {
		return uriPrefix;
	}

	public boolean isPath() {
		return !location.startsWith("http://") && !location.startsWith("https://");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Registry))
			return false;
		Registry that = (Registry) obj;
		return this.location.equals(that.location) && this.name.equals(that.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	public SPARQLEndpoint createEndpoint() {
		return this.getLocation().equals("N/A") ? new LocalEndpoint(location) : new StardogEndpoint(location);
	}

	@Override
	public String toString() {
		return name + " (" + location + ")";
	}
}
