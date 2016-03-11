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

import java.io.ByteArrayOutputStream;

import org.sbolstandard.core.impl.SBOLObjectImpl;

/**
 * 
 * @author Evren Sirin
 */
public abstract class SublimeSBOLObjectImpl extends SBOLObjectImpl {
	private static final SBOLTextWriter WRITER = new SBOLTextWriter();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WRITER.write(this, out);
		return new String(out.toByteArray());
	}
}
