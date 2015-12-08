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

import org.sbolstandard.core.SBOLVisitor;

/**
 * @author Evren Sirin
 */
public class SublimeSequencingData extends SublimeSBOLObjectImpl {
	private String displayId;
	private String orderNumber;
	private String dataFile;
	private Date date; 

	public SublimeSequencingData() {
	}

	@Override
	public void accept(SBOLVisitor visitor) {
		if (visitor instanceof SublimeSBOLVisitor) {
			((SublimeSBOLVisitor) visitor).visit(this);
		}
	}
	
	public String getOrderNumber() {
    	return orderNumber;
    }

	public void setOrderNumber(String orderNumber) {
    	this.orderNumber = orderNumber;
    }

	public String getDataFile() {
    	return dataFile;
    }

	public void setDataFile(String dataFile) {
    	this.dataFile = dataFile;
    }

	public Date getDate() {
    	return date;
    }

	public void setDate(Date date) {
    	this.date = date;
    }

	public String getDisplayId() {
		return displayId;
	}

	public void setDisplayId(String displayId) {
		this.displayId = displayId;
	}
}