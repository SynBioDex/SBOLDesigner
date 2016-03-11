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
import org.sbolstandard.core.SBOLVisitor;

/**
 * Class to represent sequence variant information in the SBOL Object model.
 * 
 * @author Evren Sirin
 */
public class SublimeSequenceVariant extends SublimeSBOLObjectImpl {
	private DnaComponent component;
	private String name;
	private Integer bioStart;
	private Integer bioEnd;
	private URI type;
	private Boolean ambiguous;
	
	public SublimeSequenceVariant() {		
	}

	@Override
    public void accept(SBOLVisitor visitor) {
	    if (visitor instanceof SublimeSBOLVisitor) {
	    	((SublimeSBOLVisitor) visitor).visit(this);
	    }
    }

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public DnaComponent getComponent() {
    	return component;
    }

	public void setComponent(DnaComponent component) {
    	this.component = component;
    }

	/**
	 * 
	 */
	public Integer getBioStart() {
		return bioStart;
	}

	/**
	 * 
	 */
	public void setBioStart(Integer value) {
		this.bioStart = value;
	}

	/**
	 * 
	 */
	public Integer getBioEnd() {
		return bioEnd;
	}

	/**
	 * 
	 */
	public void setBioEnd(Integer value) {
		this.bioEnd = value;
	}

	/**
	 * 
	 */
	public URI getType() {
		return type;
	}

	/**
	 * 
	 */
	public void setType(URI value) {
		this.type = value;
	}

	public Boolean isAmbiguous() {
    	return ambiguous;
    }

	public void setAmbiguous(Boolean ambiguous) {
    	this.ambiguous = ambiguous;
    }
}