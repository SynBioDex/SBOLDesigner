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

import java.util.Date;
import java.util.List;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLVisitor;

import com.google.common.collect.Lists;

/**
 * @author Evren Sirin
 */
public class SublimeSequenceAnalysis extends SublimeSBOLObjectImpl implements SBOLRootObject {
	private DnaComponent component;
	private String conclusion;
	private Date date;
	private List<SublimeSequenceVariant> variants = Lists.newArrayList();
	private List<SublimeSequencingData> data = Lists.newArrayList();
	
	public SublimeSequenceAnalysis() {		
	}

	@Override
    public void accept(SBOLVisitor visitor) {
	    if (visitor instanceof SublimeSBOLVisitor) {
	    	((SublimeSBOLVisitor) visitor).visit(this);
	    }
    }

	public DnaComponent getComponent() {
    	return component;
    }

	public void setComponent(DnaComponent component) {
    	this.component = component;
    }

	public String getConclusion() {
    	return conclusion;
    }

	public void setConclusion(String conclusion) {
    	this.conclusion = conclusion;
    }

	public Date getDate() {
    	return date;
    }

	public void setDate(Date date) {
    	this.date = date;
    }

	public List<SublimeSequenceVariant> getVariants() {
		return variants;
	}

	public void addVariant(SublimeSequenceVariant variant) {
		getVariants().add(variant);
	}

	public void removeVariant(SublimeSequenceVariant variant) {
		getVariants().remove(variant);
	}
	
	public List<SublimeSequencingData> getSequencingData() {
		return data;
	}
	
	public void addSequencingData(SublimeSequencingData data) {
		getSequencingData().add(data);
	}
	
	public void removeSequencingData(SublimeSequencingData data) {
		getSequencingData().remove(data);
	}
}