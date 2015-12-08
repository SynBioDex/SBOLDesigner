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

package com.clarkparsia.sbol.order;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * A collection of partially ordered elements. One or more elements can be added to this class and {@link #iterator()}
 * will return the elements in a ascending topological order.   
 * 
 * @author Evren Sirin
 */
public class PartialOrder<T> implements Iterable<T> {
	private final Map<T, Set<T>> precededBy = Maps.newLinkedHashMap();
	private final PartialOrderComparator<T> comparator;

	public PartialOrder(PartialOrderComparator<T> comparator) {
		this.comparator = comparator;
	}

	public PartialOrder(Iterable<T> elements, PartialOrderComparator<T> comparator) {
		this.comparator = comparator;
		addAll(elements);
	}

	public boolean addAll(Iterable<T> newElements) {
		boolean added = false;
		for (T newElement : newElements) {
	        added |= add(newElement);
        }
		return added;
	}

	public boolean add(T newElement) {
		if (precededBy.containsKey(newElement)) {
			return false;
		}

		Set<T> precededByList = Sets.newHashSet();
		List<T> precedesList = Lists.newArrayList();
		for (T e : precededBy.keySet()) {
			PartialOrderRelation cmp = comparator.compare(newElement, e);
			if (cmp == PartialOrderRelation.LESS) {
				precedesList.add(e);
			}
			else if (cmp == PartialOrderRelation.GREATER) {
				precededByList.add(e);
			}
		}

		for (T e : precedesList) {
			precededBy.get(e).add(newElement);
		}

		precededBy.put(newElement, precededByList);

		return true;
	}

	/**
	 * Returns the elements in an ascending topological order.
	 * 
	 * @throws IllegalStateException if there are cycles between the elements
	 */
	@Override
	public Iterator<T> iterator() throws IllegalStateException {
		Multiset<T> degrees = HashMultiset.create();
		Queue<T> nodesPending = new ArrayDeque<T>();
		List<T> nodesSorted = Lists.newArrayList();

		for (Entry<T, Set<T>> entry : precededBy.entrySet()) {
			T node = entry.getKey();
			Set<T> precededByList = entry.getValue();
			int degree = precededByList.size();
			degrees.setCount(node, degree);
			if (degree == 0) {
				nodesPending.add(node);
			}			
		}

		while (!nodesPending.isEmpty()) {
			T node = nodesPending.remove();

			int deg = degrees.count(node);
			if (deg != 0)
				throw new IllegalStateException("Cycle detected " + node + " " + deg + " " + nodesSorted.size());

			nodesSorted.add(node);

			for (Entry<T, Set<T>> entry : precededBy.entrySet()) {
				T n = entry.getKey();
				Set<T> precededByList = entry.getValue();
				if (precededByList.contains(node)) {
					int degree = degrees.count(n);
					if (degree == 1) {
						nodesPending.add(n);
						degrees.setCount(n, 0);
					}
					else {
						degrees.remove(n);
					}
				}
			}
		}

		if (nodesSorted.size() != precededBy.size()) {
			throw new IllegalStateException("Failed to sort elements");
		}

		return nodesSorted.iterator();
	}
	
	public int size() {
		return precededBy.size();
	}
}
