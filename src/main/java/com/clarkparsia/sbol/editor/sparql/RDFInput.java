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

package com.clarkparsia.sbol.editor.sparql;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import com.google.common.base.Throwables;

public abstract class RDFInput {
	private RDFInput() {		
	}
	
	public boolean isFile() {
		return false;
	}
	
	public File getFile() {
		throw new IllegalStateException("Not a file source");
	}
	
	public boolean isStream() {
		return false;
	}
	
	public InputStream getStream() {
		throw new IllegalStateException("Not a stream source");
	}
	
	public boolean isStatements() {
		return false;
	}
	
	public Iterable<? extends Statement> getStatements() {
		throw new IllegalStateException("Not a statements source");
	}
	
	public RDFFormat getFormat() {
		return RDFFormat.RDFXML;
	}
	
	public static RDFInput forFile(final File file) {
		return new RDFInput() {
			@Override
            public boolean isFile() {
	            return true;
            }

			@Override
            public File getFile() {
	            return file;
            }
			
			public RDFFormat getFormat() {
				return Rio.getParserFormatForFileName(file.getName());
			}			
		};
	}
	
	public static RDFInput forBytes(final byte[] bytes) {
		return forStream(new ByteArrayInputStream(bytes));
	}
	
	public static RDFInput forStream(final InputStream stream) {
		return new RDFInput() {
			@Override
            public boolean isStream() {
	            return true;
            }

			@Override
            public InputStream getStream() {
	            return stream;
            }			
		};
	}
	
	public static RDFInput forURL(final URL url) {
		return new RDFInput() {
			@Override
            public boolean isStream() {
	            return true;
            }

			@Override
            public InputStream getStream() {
	            try {
	                return url.openStream();
                }
                catch (IOException e) {
	                throw Throwables.propagate(e);
                }
            }	
			
			public RDFFormat getFormat() {
				return Rio.getParserFormatForFileName(url.getFile());
			}		
		};
	}
		
	public static RDFInput forStatements(Statement... statements) {
		return forStatements(Arrays.asList(statements));
	}
	
	public static RDFInput forStatements(final Iterable<? extends Statement> statements) {
		return new RDFInput() {
			@Override
            public boolean isStatements() {
	            return true;
            }

			@Override
            public Iterable<? extends Statement> getStatements() {
	            return statements;
            }			
		};
	}
}
