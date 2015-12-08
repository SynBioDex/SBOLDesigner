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

package com.clarkparsia.versioning.ui;

import java.util.List;

import com.clarkparsia.versioning.Ref;
import com.google.common.collect.Lists;

public class HistoryNode {
	private final Ref ref;

	private HistoryLane lane;

	private List<HistoryLane> passingLanes;

	private List<HistoryNode> children;

	private List<HistoryNode> parents;
	
	private List<Ref> tags;

	public HistoryNode(final Ref ref) {
		this.ref = ref;
		passingLanes = Lists.newArrayList();
		parents = Lists.newArrayList();
		children = Lists.newArrayList();
		tags =  Lists.newArrayList();
	}
	
	void setupLane(final HistoryLane lane, Iterable<HistoryLane> passingLanes) {
		this.lane = lane;
		this.passingLanes = Lists.newArrayList(passingLanes);
		this.passingLanes.remove(lane);
	}

	void addPassingLane(final HistoryLane lane) {
		passingLanes.add(lane);
	}

	void addChild(final HistoryNode node) {
		children.add(node);
	}

	void removeChild(final HistoryNode node) {
		children.remove(node);
	}

	void addParent(final HistoryNode node) {
		parents.add(node);
	}

	void removeParent(final HistoryNode node) {
		parents.remove(node);
	}

	public List<HistoryNode> getChildren() {
		return children;
	}

	public List<HistoryNode> getParents() {
		return parents;
	}

	public final HistoryLane getLane() {
		return lane;
	}

	public List<HistoryLane> getPassingLanes() {
    	return passingLanes;
    }

	public List<Ref> getTags() {
    	return tags;
    }

	public Ref getRef() {
		return ref;
	}
	
	public String toString() {
		return ref.toString();
	}
}
