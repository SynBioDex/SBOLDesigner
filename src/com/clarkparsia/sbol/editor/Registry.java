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

package com.clarkparsia.sbol.editor;

import java.io.Serializable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.clarkparsia.sbol.editor.sparql.LocalEndpoint;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.google.common.base.Preconditions;

public class Registry implements Serializable {
	private final String name;
	private final String description;
	private final String location;

	public static final Registry BUILT_IN = new Registry("Built-in parts",
			"Built-in registry with minimal set of parts", "N/A");

	public static final Registry STACK = new Registry("SBOL Stack", "The Newcastle instance of the SBOL Stack",
			"http://synbiohub.org:9090");

	public Registry(String name, String description, String location) {
		Preconditions.checkNotNull(name, "Name cannot be null");
		Preconditions.checkNotNull(location, "URL/Path cannot be null");
		this.name = name;
		this.description = description;
		this.location = location;
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

	public boolean isPath() {
		return !location.startsWith("http://");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Registry))
			return false;
		Registry that = (Registry) obj;
		return this.location.equals(that.location);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	public boolean isBuiltin() {
		return this.equals(Registry.BUILT_IN);
	}

	public SPARQLEndpoint createEndpoint() {
		return isBuiltin() ? new LocalEndpoint(location) : new StardogEndpoint(location);
	}

	@Override
	public String toString() {
		return name + " (" + location + ")";
	}
}
