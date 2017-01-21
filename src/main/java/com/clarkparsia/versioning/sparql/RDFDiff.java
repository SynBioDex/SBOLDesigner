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

package com.clarkparsia.versioning.sparql;

import java.util.Set;

import org.openrdf.model.Statement;

import com.google.common.collect.Sets;

public class RDFDiff {
	private final Set<Statement> additions = Sets.newHashSet();
	private final Set<Statement> removals = Sets.newHashSet();
	
	private RDFDiff(Set<Statement> initialStmts, Set<Statement> finalStmts) {
		for (Statement stmt : initialStmts) {
			if (!finalStmts.contains(stmt)) {
				removals.add(stmt);
			}
		}

		for (Statement stmt : finalStmts) {
			if (!initialStmts.contains(stmt)) {
				additions.add(stmt);
			}
		}
	}
	
	public Iterable<Statement> apply(Iterable<Statement> stmts) {
		Set<Statement> result = Sets.newHashSet(stmts);
		result.removeAll(removals);
		result.addAll(additions);
		return result;
	}
	
	
	public static RDFDiff compute(Iterable<Statement> initialStmts, Iterable<Statement> finalStmts) {
		return new RDFDiff(createSet(initialStmts), createSet(finalStmts));
	}
	
	@SuppressWarnings("unchecked")
    private static Set<Statement> createSet(Iterable<Statement> stmts) {
		return (stmts instanceof Set) ? (Set) stmts : Sets.newHashSet(stmts);
	}
}
