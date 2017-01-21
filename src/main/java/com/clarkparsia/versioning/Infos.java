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

package com.clarkparsia.versioning;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openrdf.model.URI;

import com.clarkparsia.versioning.sparql.Terms;
import com.google.common.base.Preconditions;

public class Infos {	
	public static PersonInfo forPerson(String uri) {
		return forPerson(Terms.uri(uri), "", null);
	}
	
	public static PersonInfo forPerson(URI uri) {
		return forPerson(uri, "", null);
	}
	
	public static PersonInfo forPerson(String uri, String name, String email) {
		return forPerson(Terms.uri(uri), name, Terms.uri("mailto:" + email));
	}
	
	public static PersonInfo forPerson(URI uri, String name, URI email) {
		return new ImmutablePersonInfo(uri, name, email);
	}
	
	public static ActionInfo forAction(String author, String msg) {
		return forAction(forPerson(author), msg);
	}
		
	public static ActionInfo forAction(URI author, String msg) {
		return forAction(forPerson(author), msg);
	}
	
	public static ActionInfo forAction(PersonInfo author, String msg) {
		return forAction(author, msg, GregorianCalendar.getInstance());
	}
	
	public static ActionInfo forAction(PersonInfo author, String msg, Calendar time) {
		return new ImmutableActionInfo(author, msg, time);
	}
	
	private static class ImmutablePersonInfo implements PersonInfo {
		private final URI uri;
		private final String name;
		private final URI email;

		public ImmutablePersonInfo(URI user, String name, URI email) {
			Preconditions.checkNotNull(user, "Person URI cannot be null");
			this.uri = user;
			this.name = name;
			this.email = email;
		}

		@Override
		public URI getURI() {
		    return uri;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public URI getEmail() {
			return email;
		}

		@Override
	    public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(name);
			if (email != null) {
				sb.append(" <").append(email.getLocalName()).append(">");
			}
		    return sb.toString();
	    }	
	}
	
	private static class ImmutableActionInfo implements ActionInfo {
		private final PersonInfo user;
		private final String message;
		private final Calendar time;

		public ImmutableActionInfo(PersonInfo user, String message, Calendar time) {
			this.user = user;
			this.message = message;
			this.time = time;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public Calendar getDate() {
			return time;
		}

		@Override
		public PersonInfo getAuthor() {
			return user;
		}

		@Override
	    public String toString() {
		    return "ActionInfo [message=" + message + ", time=" + time + ", user=" + user + "]";
	    }	
	}

}
