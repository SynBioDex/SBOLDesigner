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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLObject;
import org.sbolstandard.core.SBOLValidationException;
import org.sbolstandard.core.SBOLVisitor;

/**
 * Utility class to write the contents of an SBOL document in a text-based, human-readable format. This format is used
 * for information purposes and not intended to be an exchange syntax.
 * 
 * @author Evren Sirin
 */
public class SBOLTextWriter extends SBOLAbstractWriter {
	public SBOLTextWriter() {
		super(false);
	}

	@Override
    protected SBOLVisitor createWriter(OutputStream out) {
	    return new Writer(out);
    }

	/**
	 * The actual writer implementation that writes output as indented plain text.
	 *  
	 * @author Evren Sirin
	 */
	protected static class Writer extends SBOLAbstractWriterVisitor {
		protected static URI URI = FACTORY.createURI("urn:dummy#uri");
		
		protected static List<URI> INLINED_PROPS = Arrays.asList(SBOLVocabulary.dnaSequence, SBOLVocabulary.component);
		
		private static final String INDENT = "   ";

		private final PrintStream out;
		private String indent = "";

		public Writer(OutputStream out) {
			this.out = (out instanceof PrintStream) ? (PrintStream) out : new PrintStream(out);
		}
		
		@Override
		public void visit(SBOLDocument doc) {
			startBlock("SBOLDocument");
			super.visit(doc);
			endBlock();
		}

		@Override
        protected void startSubj(SBOLObject obj, URI type) {
			startBlock(type.getLocalName());
			
			if (obj.getURI() == null) {
				throw new SBOLValidationException("Missing URI for: " + obj);
			}
			
			URI newSubj = FACTORY.createURI(obj.getURI().toString());
			
			subjList.push(subj);
			subj = newSubj;
			
			write(subj, URI, subj);
        }

		@Override
        protected void endSubj() {
			indent = indent.substring(0, indent.length() - INDENT.length());
			out.print(indent);
			out.println("]");
        }

		@Override
        protected void startProp(URI property) {
	        super.startProp(property);
	        if (!isInlined(property)) {
	        	startBlock(property.getLocalName() + ":");
	        }
        }

		@Override
        protected void endProp() {
			if (!isInlined(prop)) {
	        	endBlock();
	        }
			
	        super.endProp();
        }
		
		protected boolean isInlined(URI prop) {
			return INLINED_PROPS.contains(prop);
		}

		protected void startBlock(String name) {
			out.print(indent);
			out.print(name);
			out.println(" [");
			indent += INDENT;
		}

		protected void endBlock() {
			indent = indent.substring(0, indent.length() - INDENT.length());
			out.print(indent);
			out.println("]");
		}

		@Override
        protected void write(Resource subj, URI pred, Value obj) {
			out.print(indent);
			out.print(pred.getLocalName());
			out.print(": ");
			out.println(obj.stringValue());
        }	
	}
}
	
