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

import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class Registries implements Iterable<Registry> {
	private static Registries INSTANCE;
	
	public static Registries get() {
		if (INSTANCE == null) {
			INSTANCE = new Registries();
		}
		
		return INSTANCE;
	}
	
	private final List<Registry> registries;
	private int partRegistryIndex;
	private int versionRegistryIndex;
	private boolean isModified = false;
	
	private Registries() {
		registries = Lists.newArrayList();
		Preferences prefs = Preferences.userNodeForPackage(Registries.class).node("registries");
		int registryCount = prefs.getInt("size", 0);
		for (int i = 0; i < registryCount; i++) {
			Preferences child = prefs.node("registry" + i);
			String name = child.get("name", null);
			String desc = child.get("description", null);
			String url = child.get("url", null);
			try {
				if (url.startsWith("jar:") || url.startsWith("file:")) {
					registries.add(Registry.BUILT_IN);
				}
				else {
					registries.add(new Registry(name, desc, url));
				}
            }
            catch (Exception e) {
            	e.printStackTrace();
            }
		}
		
		if (registries.isEmpty()) {	
			restoreDefaults();
		}
		else {
			partRegistryIndex = prefs.getInt("partRegistry", 0);
			if (!isValidIndex(partRegistryIndex)) {
				partRegistryIndex = 0;
			}
			
			versionRegistryIndex = prefs.getInt("versionRegistry", 0);
			if (!isValidIndex(versionRegistryIndex)) {
				versionRegistryIndex = 0;
			}
		}
	}
	
	public void restoreDefaults() {
		registries.clear();
		registries.add(Registry.BUILT_IN);	
		registries.add(Registry.SBPKB);
		partRegistryIndex = 1;
		versionRegistryIndex = 1;
		isModified = true;
	}
	
	public void save() {
		if (!isModified) {
			return;
		}
		isModified = false;
		
		Preferences prefs = Preferences.userNodeForPackage(Registries.class).node("registries");
		try {
	        prefs.removeNode();
        }
        catch (BackingStoreException e) {
	        e.printStackTrace();
        }
		prefs = Preferences.userNodeForPackage(Registries.class).node("registries");
		int size = registries.size();
		prefs.putInt("size", size);
		prefs.putInt("partRegistry", partRegistryIndex);
		prefs.putInt("versionRegistry",versionRegistryIndex);
		for (int i = 0; i < size; i++) {
			Registry registry = registries.get(i);
			Preferences child = prefs.node("registry" + i);
			child.put("name", registry.getName());
			child.put("description", registry.getDescription());
			child.put("url", registry.getURL());
		}
	}
	
	private boolean isValidIndex(int index) {
		return index >= 0 && index < registries.size();
	}
	
	public Iterator<Registry> iterator() {
		return registries.iterator();
	}
	
	public int size() {
		return registries.size();
	}
	
	public void setPartRegistryIndex(int index) {
		Preconditions.checkArgument(isValidIndex(index));
		if (partRegistryIndex != index) {
			partRegistryIndex = index;
			isModified = true;
		}
	}
	
	public int getPartRegistryIndex() {
		return partRegistryIndex;
	}	
	
	public void setVersionRegistryIndex(int index) {
		Preconditions.checkArgument(isValidIndex(index));
		if (versionRegistryIndex != index) {
			versionRegistryIndex = index;
			isModified = true;
		}
	}
	
	public int getVersionRegistryIndex() {
		return versionRegistryIndex;
	}
	
	public void add(Registry registry) {
		registries.add(registry);
		isModified = true;
	}
	
	public Registry get(int index) {
		return registries.get(index);
	}
	
	public void remove(int index) {
		Preconditions.checkArgument(isValidIndex(index));
		registries.remove(index);
		partRegistryIndex = updateIndex(partRegistryIndex, index);
		versionRegistryIndex = updateIndex(versionRegistryIndex, index);
		isModified = true;
	}
	
	private int updateIndex(int index, int removedIndex) {
		return (partRegistryIndex == index) ? 0 : (partRegistryIndex > index) ? index - 1 : index;
	}
}
