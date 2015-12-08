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

package com.clarkparsia.sbol;

import java.util.List;

import org.sbolstandard.core.Collection;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core.SBOLVisitable;
import org.sbolstandard.core.SequenceAnnotation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SBOLObjectFinder<T extends SBOLObject> extends SublimeSBOLBaseVisitor {
	public static Iterable<SBOLObject> findObjects(final SBOLVisitable visitable, final Predicate<SBOLObject> condition) {
		SBOLObjectFinder<SBOLObject> finder = new SBOLObjectFinder<SBOLObject>(condition, SBOLObject.class);
		visitable.accept(finder);
		return finder.result;
	}
	
	public static <T extends SBOLObject> Iterable<T> findObjects(final SBOLVisitable visitable, final Predicate<T> condition, Class<T> cls) {
		SBOLObjectFinder<T> finder = new SBOLObjectFinder<T>(condition, cls);
		visitable.accept(finder);
		return finder.result;
	}
	
	public static <T extends SBOLObject> T findObject(final SBOLVisitable visitable, final Predicate<T> condition, Class<T> cls) {
		return Iterables.getOnlyElement(findObjects(visitable, condition, cls), null);
	}
	
	private final Class<T> cls;
	private final List<T> result = Lists.newArrayList();
	private final Predicate<T> condition;
	
	private SBOLObjectFinder(final Predicate<T> condition, final Class<T> cls) {
		this.condition = condition;
		this.cls = cls;
	}
	
    private void process(SBOLObject obj) {
		if (cls.isInstance(obj)) {
			T t = cls.cast(obj);
		    if (condition.apply(t)) {
		    	result.add(t);
		    }
		}
	}

	@Override
    public void visit(Collection coll) {
		process(coll);
	    super.visit(coll);
    }

	@Override
    public void visit(DnaComponent comp) {
	    process(comp);
	    super.visit(comp);
    }

	@Override
    public void visit(DnaSequence seq) {
	    process(seq);
	    super.visit(seq);
    }

	@Override
    public void visit(SequenceAnnotation ann) {
	    process(ann);
	    super.visit(ann);
    }

	@Override
    public void visit(SublimeSequenceVariant variant) {
		process(variant);
	    super.visit(variant);
    }

	@Override
    public void visit(SublimeSequenceAnalysis analysis) {
		process(analysis);
	    super.visit(analysis);
    }

	@Override
    public void visit(SublimeSequencingData data) {
		process(data);
	    super.visit(data);
    }	
}
