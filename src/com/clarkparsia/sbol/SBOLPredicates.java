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

import java.net.URI;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLObject;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;

/**
 * 
 * @author Evren Sirin
 */
public class SBOLPredicates {
	public static <T extends SBOLObject> Predicate<T> uri(final URI uri) {
		return new Predicate<T>() {
			@Override
            public boolean apply(SBOLObject obj) {
	            return Objects.equal(obj.getURI(), uri);
            }
		};
	}
	
	public static Predicate<DnaComponent> displayId(final String displayId) {
		return new Predicate<DnaComponent>() {
			@Override
            public boolean apply(DnaComponent comp) {
	            return Objects.equal(comp.getDisplayId(), displayId);
            }
		};
	}
}
