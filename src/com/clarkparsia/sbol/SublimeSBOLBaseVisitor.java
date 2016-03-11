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

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.util.SBOLBaseVisitor;

/**
 * Base implements of extended Sub;ime visitor interface.
 * 
 * @author Evren Sirin
 */
public class SublimeSBOLBaseVisitor extends SBOLBaseVisitor implements SublimeSBOLVisitor {

	@Override
    public void visit(SublimeSequenceVariant variant) {
	    // nothing to do here
    }

	@Override
    public void visit(SublimeSequenceAnalysis analysis) {
		DnaComponent component = analysis.getComponent();
		if (component != null) {
			component.accept(this);
		}
		
	    for (SublimeSequencingData data : analysis.getSequencingData()) {
	        data.accept(this);
        }
	    
	    for (SublimeSequenceVariant variant : analysis.getVariants()) {
	    	variant.accept(this);
        }
    }

	@Override
    public void visit(SublimeSequencingData data) {
	    // TODO Auto-generated method stub	    
    }
}
