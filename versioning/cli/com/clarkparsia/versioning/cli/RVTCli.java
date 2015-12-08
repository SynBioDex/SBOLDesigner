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

package com.clarkparsia.versioning.cli;

import io.airlift.command.Arguments;
import io.airlift.command.Cli;
import io.airlift.command.Cli.CliBuilder;
import io.airlift.command.Command;
import io.airlift.command.Help;
import io.airlift.command.Option;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.clarkparsia.sbol.editor.sparql.RDFInput;
import com.clarkparsia.sbol.editor.sparql.StardogEndpoint;
import com.clarkparsia.versioning.ActionInfo;
import com.clarkparsia.versioning.Branch;
import com.clarkparsia.versioning.Infos;
import com.clarkparsia.versioning.Listable;
import com.clarkparsia.versioning.RVT;
import com.clarkparsia.versioning.RVTFactory;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Repository;
import com.clarkparsia.versioning.Revision;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class RVTCli {
    public static void main(String... args) {
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("rvt")
                .withDescription("RDF version control")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class) 
                .withCommand(Init.class)
                .withCommand(Checkout.class)
                .withCommand(Commit.class)
                .withCommand(MergeBranch.class);

        builder.withGroup("repo")
                .withDescription("Manage set of repositories")
                .withDefaultCommand(Help.class)
                .withCommand(RepoList.class)
                .withCommand(RepoCreate.class);
        
        builder.withGroup("branch")
                .withDescription("Manage branches")
                .withDefaultCommand(BranchCreate.class)
                .withCommand(BranchList.class);

        Cli<Runnable> gitParser = builder.build();
        
    	try {
    		gitParser.parse(args).run();
        }
        catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());	            
        }
    }

	public abstract static class RVTCommand implements Runnable {
		protected File metadataFile = new File(".rvt/metadata.properties");
		protected Properties metadata = new Properties();
		protected RVT system;
		
		protected void init() throws Exception {
			if (!metadataFile.exists()) {
				throw new IllegalStateException("Not initialized");
			}
			
			InputStream in = new FileInputStream(metadataFile);
			metadata.load(in);
			in.close();
			
			//metadata.list(System.out);   
			
			String endpointURL = metadata.getProperty("endpoint");
			system = RVTFactory.get(new StardogEndpoint(endpointURL));
		}

		protected void writeMetadata() throws IOException {
			OutputStream out = new FileOutputStream(metadataFile);
			metadata.store(out, "RVT metadata");
	        out.close();
		}
		
		public void run() {
        	try {
        		init();
	            exec();
            }
            catch (Exception e) {
	            System.err.println("ERROR: " + e.getMessage());
	            if (e.getMessage() == null) {
	            	e.printStackTrace();
	            }
            }
		}
		
		protected File createFile(Repository repo, Branch branch, Revision revision, String formatName) throws Exception {
			RDFFormat format = RDFFormat.valueOf(formatName.toUpperCase());
        	Preconditions.checkNotNull(format, "Unrecognized RDF format type: " + formatName);
	    	File file = new File(repo.getName(), branch.getName() + "." + format.getDefaultFileExtension());
	    	Files.createParentDirs(file);
	    	
	    	if (revision != null) {
	        	RDFWriter writer = Rio.createWriter(format, new FileOutputStream(file));
	        	revision.checkout(writer);
	    	}
	    	else {
	    		Files.touch(file);
	    	}
	    	
	    	metadata.setProperty(repo.getName() + "." + branch.getName(), format.getName());
	    	writeMetadata();
	    	
	    	return file;
		}
		
		protected abstract void exec() throws Exception;
    }

    public abstract static class RVTCommandWithMessage extends RVTCommand {
		@Option(name = { "-m", "--message" }, description = "Message for the action")
		public String message;
		
		protected ActionInfo getActionInfo() {
			while (message == null) {
				Console console = System.console();
				console.format("Message: ");
				message = console.readLine().trim();
				if (message.length() == 0) {
					message = null;
				}
			}
			
			String user = metadata.getProperty("user");
			return Infos.forAction(user, message);
		}

    }
    
    @Command(name = "init", description = "Initializes a SPARQL endpoint to be the backend for RVT")
    public static class Init extends RVTCommand {
		@Option(name = { "-r", "--remote" }, description = "Initialize the remote endpoint")
		public boolean remote;
		
		@Option(name = { "-u", "--user" }, description = "URI to identify the user")
		public String user;
		
		@Option(name = { "-n", "--name" }, description = "Name of the the user", required = true)
		public String name;

		@Option(name = { "-e", "--email" }, description = "Email of the user", required = true)
		public String email;
		
		@Arguments(required = true, description = "URL for the SPARQL endpoint")
		public String endpointURL;
		
		protected void init() throws Exception {			
		}
		
        public void exec() throws Exception {
			if (metadataFile.exists()) {
				throw new IllegalStateException("Already initialized");
			}
			
        	StardogEndpoint endpoint = new StardogEndpoint(endpointURL);
        	if (remote) {
        		RVT rvt = RVTFactory.init(endpoint);
        		rvt.addPersonInfo(Infos.forPerson(user, name, email));
        	}
        	else {
        		RVTFactory.get(endpoint);
        	}
        	
        	metadataFile.getParentFile().mkdir();
        	metadata.setProperty("endpoint", endpointURL);
        	metadata.setProperty("user", user);
        	writeMetadata();
        }
    }

	@Command(name = "list", description = "Lists all the repositories")
	public static class RepoList extends RVTCommand {
		public void exec() throws Exception {
			for (Repository repo : system.repos().list()) {
				System.out.println(repo.getName());
			}
		}
	}

    @Command(name = "create", description = "Creates a new repo")
    public static class RepoCreate extends RVTCommandWithMessage {
        @Option(name = "format", description = "RDF format")
        public String format = RDFFormat.TURTLE.getName(); 
        
        @Arguments(description = "Name of the repo")
        public String name;        
        
        public void exec() throws Exception {
        	Repository repo = system.createRepo(name, getActionInfo());
        	Branch branch = repo.branches().get(Branch.MASTER);
    		
        	File file = createFile(repo, branch, null, format); 
        	System.out.println("Created the repo and empty file " + file);
        }
    }    

    @Command(name = "checkout", description = "Checks out data")
    public static class Checkout extends RVTCommand {
        @Option(name = "--format", description = "RDF format")
        public String format = RDFFormat.TURTLE.getName();      
        
        @Arguments(description = "Name of the repo optionally followed by branch", required = true)
        public List<String> names;        
        
        public void exec() throws Exception {
        	String repoName = names.get(0);        	
        	Repository repo = system.repos().get(repoName);
        	Preconditions.checkNotNull(repo, "Repository does not exist: " + repoName);
        	String branchName = (names.size() == 2) ? names.get(1) : Branch.MASTER;        	
        	Branch branch = repo.branches().get(branchName);     
        	Preconditions.checkNotNull(branch, "Branch does not exist: " + branchName);
    		Revision revision = branch.getHead();
        	
        	File file = createFile(repo, branch, revision, format);        	
        	System.out.println("Checked out " + file);
        }
    }

    @Command(name = "commit", description = "Commits data")
    public static class Commit extends RVTCommandWithMessage {
        @Arguments(description = "Name of the repo followed by optionally a branch name", required = true)
        public List<String> names;        
        
        public void exec() throws Exception {
        	String repoName = names.get(0);        	
        	Repository repo = system.repos().get(repoName);
        	if (names.size() == 2) {
        		String branchName = names.get(1);
        		String formatExt = RDFFormat.valueOf(metadata.getProperty(repoName + "." + branchName)).getDefaultFileExtension();
        		File file = new File(repoName, branchName + "." + formatExt);
        		Branch branch = repo.branches().get(branchName);        		
        		branch.commit(RDFInput.forFile(file), getActionInfo());
        	}
        	else {
        		String prefix = repoName + ".";
        		for (String key : metadata.stringPropertyNames()) {
	                if (key.startsWith(prefix)) {
	            		String formatExt = RDFFormat.valueOf(metadata.getProperty(key)).getDefaultFileExtension();
	            		File file = new File(key.replace('.', File.separatorChar) + "." + formatExt);
	                	String branchName = key.substring(prefix.length());
	            		Branch branch = repo.branches().get(branchName);        		
	            		branch.commit(RDFInput.forFile(file), getActionInfo());
	                }
                }
        	}
        }
    } 

    @Command(name = "create", description = "Create a branch")
    public static class BranchCreate extends RVTCommandWithMessage {
        @Arguments(description = "Name of the repo followed by optionally a branch name", required = true)
        public List<String> names;        
        
        public void exec() throws Exception {
        	int arg = 0;
        	String repoName = names.get(arg++);        	
        	Repository repo = system.repos().get(repoName);
    		String sourceBranchName = (names.size() == 2) ? Branch.MASTER : names.get(arg++);
    		String targetBranchName = names.get(arg);
    		
    		Branch sourceBranch = repo.branches().get(sourceBranchName);
    		Branch targetBranch = sourceBranch.getHead().branch(targetBranchName, getActionInfo());
    		
    		String formatName = metadata.getProperty(repoName + "." + sourceBranchName);
    		
    		File file = createFile(repo, targetBranch, targetBranch.getHead(), formatName);        	
        	System.out.println("Created branch and file " + file);
        		
        }
    } 

    @Command(name = "list", description = "Lists all the branches in a repository")
    public static class BranchList extends RVTCommand {
        @Arguments(description = "Name of the repo", title="repo", required = true)
        public String repoName;        
        
        public void exec() throws Exception {
        	Repository repo = system.repos().get(repoName);    	
        	printRefs(repo.branches().list(), false);
        }
    } 

    @Command(name = "merge", description = "Merges a branch")
    public static class MergeBranch extends RVTCommandWithMessage {
        @Arguments(description = "Name of the repo followed by the target branch name and source branch", required = true)
        public List<String> names;        
        
        public void exec() throws Exception {
        	Iterator<String> names = this.names.iterator();
        	String repoName = names.next();        	
        	Repository repo = system.repos().get(repoName);
    		String targetBranchName = names.next();

    		final Listable<Branch> branches = repo.branches();
    		Branch targetBranch = branches.get(targetBranchName);
    		
    		String sourceBranchName = names.next();
    		Branch sourceBranch = branches.get(sourceBranchName);
    		
    		Revision newHead = targetBranch.merge(sourceBranch.getHead(), getActionInfo());   
    		String formatName = metadata.getProperty(repoName + "." + targetBranchName);
    		
    		File file = createFile(repo, targetBranch, newHead, formatName);       
    		
        	System.out.println("Merged branches and updated file " + file);
        		
        }
    }
    
    private static void printRefs(Iterable<? extends Ref> refs, boolean verbose) {
		for (Ref ref : refs) {
			System.out.print(ref.getName());
			if (verbose) {
				System.out.print(": ");
				System.out.print(ref.getActionInfo().getMessage());
			}
			System.out.println();
		}
    	
    }
}
