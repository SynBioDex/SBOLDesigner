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

package com.clarkparsia.geneious;

import com.clarkparsia.sbol.CharSequences;

/**
 * Sequence variant types as defined in <a href="http://www.hgvs.org/mutnomen/recs.html#general">here</a>
 * 
 * @author Evren Sirin
 *
 */
public enum SequenceVariantType {
	DELETION, DUPLICATION, INSERTION, INVERSION, SUBSTITUTION;
	
    public static final String[] NAMES;
    
    static {
    	NAMES = new String[SequenceVariantType.values().length];
    	int index= 0;
    	for (SequenceVariantType sequenceVariant : SequenceVariantType.values()) {
    		NAMES[index++] = sequenceVariant.toString();
		}
    }
	
	public String symbol() {
		return name().substring(0, 3).toLowerCase();
	}
	
	public String toString() {
		return CharSequences.toTitleCase(name());
	} 
	
	public static SequenceVariantType find(String name) {
		try {
	        return valueOf(name.toUpperCase());
        }
        catch (IllegalArgumentException e) {
	        return null;
        }
	}
}