package edu.utah.ece.async.sboldesigner.boost;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import gov.doe.jgi.boost.client.BOOSTClient;
import gov.doe.jgi.boost.enums.FileFormat;
import gov.doe.jgi.boost.enums.Strategy;
import gov.doe.jgi.boost.enums.Vendor;
import gov.doe.jgi.boost.exception.BOOSTBackEndException;
import gov.doe.jgi.boost.exception.BOOSTClientException;

public class BOOSTOperations {
	
   private final String selectedFilePath;
   private BOOSTClient client = null;

	public BOOSTOperations(String boostToken, String filePath) {
		this.selectedFilePath = filePath;	
		this.client = new BOOSTClient(boostToken);
		
		try {
			boostTasks();
		} catch (BOOSTClientException | BOOSTBackEndException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void boostTasks() throws BOOSTClientException, BOOSTBackEndException, IOException {
		// get the predefined hosts
				JSONObject jsonPredefinedHosts = client.getPredefinedHosts();
				try {
				System.out.println(jsonPredefinedHosts.toString(4));
				}catch(NullPointerException e) {
					System.out.println(e.getMessage() + " Error in josnPeredeefinedHosts");
				}
				
				// we store all submitted jobs in a hash-set
				Set<String> jobUUIDs = new HashSet<String>();
				

				// codon juggle
				String codonJuggleJobUUID = client.codonJuggle(
						selectedFilePath,			  // input sequences 
						true,						  // exclusively 5'-3' coding sequences 
						Strategy.MostlyUsed,		  // codon selection strategy
						"Saccharomyces cerevisiae",   // predefined host
						FileFormat.SBOL);		  // output format
				if(null != codonJuggleJobUUID) {
					jobUUIDs.add(codonJuggleJobUUID);
					System.out.println("Data for codon Juggling :" + codonJuggleJobUUID );
				}
		
		    	// verify against DNA synthesis constraints and sequence patterns
//				String dnaVarificationJobUUID = client.dnaVarification(
//						selectedFilePath,           // input sequence
//						Vendor.GEN9,                  // vendor
//						"./data/patterns.fasta");     // sequence patterns
//				if (null != dnaVarificationJobUUID) {
//					jobUUIDs.add(dnaVarificationJobUUID);
//					System.out.println("Data for DNA Verification :" + dnaVarificationJobUUID);
//				}
		
				// polish the given DNA
				String polishDNAJobUUID = client.polish(
						selectedFilePath,           // input sequence
						true,                         // encoding sequences support sequence feature annotations
						Vendor.JGI,                   // vendor
						Strategy.Balanced2Random,     // codon selection strategy
						FileFormat.SBOL,              // output format
						"Saccharomyces cerevisiae");  // // predefined host
				if (null != polishDNAJobUUID) {
					jobUUIDs.add(polishDNAJobUUID);
					System.out.println("Data for DNA Polish :" + polishDNAJobUUID);
				}
				
				// partitioning of DNA
				String partitiongDNAJobUUID = client.partition(
						selectedFilePath,           // input sequence
						"aaacccgggttt",               // 5-prime-vector-overlap
						"tttgggcccaaa",               // 3-prime-vector-overlap
						Integer.toString(15),         // min-BB-length
						Integer.toString(3000),       // max-BB-length
						Double.toString(4.0),         // minimum overlap GC
						Double.toString(40.0),        // optimum overlap GC
						Double.toString(62.0),        // maximum overlap GC
						Integer.toString(5),          // minimum overlap length
						Integer.toString(25),         // optimum overlap length
						Integer.toString(30),         // maximum overlap length
						Integer.toString(20),		  // min. primer length
						Integer.toString(40),         // max. primer length
						Integer.toString(60));        // max. primer Tm
				if (null != partitiongDNAJobUUID) {
					jobUUIDs.add(partitiongDNAJobUUID);
					System.out.println("Data for Partation :" + partitiongDNAJobUUID);
				}
				
				
						
				// for all jobs, we check their status
				for(String jobUUID : jobUUIDs) {
					
					JSONObject jobReport = null;
					while(null == (jobReport = client.getJobReport(jobUUID))) {
						
						// if the job isn't finished, then we wet some seconds
						// and check again
						System.out.println("Job " + jobUUID + " is not finished yet.");
						
						try {
							Thread.sleep(5000);
						} catch(Exception e) {}
					}
					
					// output of the job report (which is a JSON object)
					System.out.println(jobReport);
				}
	}	
}
