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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.URI;

import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Revision;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class HistoryList {
	private static final Ordering<Ref> NEWEST_FIRST = Ordering.from(new Comparator<Ref>() {
		@Override
        public int compare(Ref rev1, Ref rev2) {
            return rev2.getActionInfo().getDate().compareTo(rev1.getActionInfo().getDate());
        }
	});
	
	private static final Function<Revision, Branch> REV_BRANCH = new Function<Revision, Branch>() {
		@Override
		public Branch apply(Revision rev) {
			return rev.getBranch();
		}
	};
	
	private static final Function<Branch, Revision> BRANCH_TAIL = new Function<Branch, Revision>() {
		@Override
		public Revision apply(Branch branch) {
			return branch.getTail();
		}
	};
	
	private final Map<Branch, HistoryLane> openLanes = Maps.newHashMap();
	private final Set<Revision> tailRevisions = Sets.newHashSet();

	private final ColorPool colors = new ColorPool();
	private final ColumnPool columns = new ColumnPool();

	private List<HistoryNode> nodes = Lists.newArrayList();

	private Map<URI, HistoryNode> refs = Maps.newHashMap();
	
	private boolean showBranches;
	

	public HistoryList() {		
	}
	
	public HistoryList(Revision head) {
		this(head, true);
	}
	
	public HistoryList(Revision head, boolean showBranches) {
		this.showBranches = showBranches;
		
		addHistory(head);
	}
	
	public void clear() {
		openLanes.clear();
		tailRevisions.clear();
		nodes.clear();
		refs.clear();
		colors.clear();
		columns.clear();
	}
	
	public HistoryNode get(int index) {
		return nodes.get(index);
	}
	
	public int size() {
		return nodes.size();
	}

	protected HistoryLane openLane(Branch branch) {
		HistoryLane lane = openLanes.get(branch);
		if (lane == null) {
			lane = new HistoryLane(columns.remove(), colors.remove());
			openLanes.put(branch, lane);
			tailRevisions.add(branch.getTail());
		}
		return lane;
	}

	protected void closeLane(HistoryLane closedLane) {
		openLanes.values().remove(closedLane);
		columns.put(closedLane.getColumn());
		colors.put(closedLane.getColor());
	}

	private HistoryNode createNode(Ref ref) {
		HistoryNode node = refs.get(ref.getURI());
		if (node == null) {
			node = new HistoryNode(ref);
			refs.put(ref.getURI(), node);
		}
		return node;
	}

	private void addHistory(Revision head) {
		Set<Ref> revisions = Sets.newTreeSet(NEWEST_FIRST);
		Queue<Revision> pending = Lists.newLinkedList();
		pending.add(head);
		while (!pending.isEmpty()) {
			Revision rev = pending.remove();
			revisions.add(rev);
			if (showBranches) {
				revisions.add(rev.getBranch());
			}
			pending.addAll(rev.getParents());
		}

		for (Ref rev : revisions) {
			add(rev);
		}
	}

	private void add(Ref ref) {
		HistoryNode node = createNode(ref);

		nodes.add(node);

		boolean isBranch = (ref instanceof Branch);
		Revision revision = isBranch ? null : ((Revision) ref);
		Branch branch = isBranch ? (Branch) ref : revision.getBranch();

		HistoryLane lane = openLane(branch);		
		node.setupLane(lane, openLanes.values());

		HistoryNode childNode = node;
		if (isBranch) {
			node.getTags().add(branch);
			
			Revision tailRev = branch.getTail();
			HistoryNode tailNode = createNode(tailRev);
			for (HistoryNode tailParent : tailNode.getParents()) {
				tailParent.getChildren().remove(tailNode);
				tailParent.getChildren().add(node);
				node.addParent(tailParent);
			}

			tailNode.getParents().clear();
			
			node.addChild(tailNode);
			tailNode.addParent(node);

			closeLane(lane);
		}
		else {
			if (!showBranches && tailRevisions.contains(revision)) {
				node.getTags().add(branch);				
				closeLane(lane);
			}
			node.getTags().addAll(revision.getTags());
		}
		
		Iterable<? extends Revision> parents;
		
		if (isBranch) {
			Revision parent = branch.getParent();
			parents = (parent != null) ? ImmutableList.<Revision>of(parent) : ImmutableList.<Revision>of();
		}
		else {
			if (showBranches) {
				parents = NEWEST_FIRST.onResultOf(REV_BRANCH).sortedCopy(revision.getParents());
			}
			else {
				parents = NEWEST_FIRST.onResultOf(BRANCH_TAIL).onResultOf(REV_BRANCH).sortedCopy(revision.getParents());
			}
		}
		
		for (Revision parent : parents) {
			HistoryNode parentNode = createNode(parent);
			childNode.addParent(parentNode);
			parentNode.addChild(childNode);
			openLane(parent.getBranch());
		}
	}
	
	public String toString() {
		return nodes.toString();
	}
}
