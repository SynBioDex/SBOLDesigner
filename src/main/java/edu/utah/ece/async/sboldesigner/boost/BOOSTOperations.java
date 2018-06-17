package edu.utah.ece.async.sboldesigner.boost;

import java.io.IOException;
import org.json.JSONObject;

import gov.doe.jgi.boost.client.BOOSTClient;
import gov.doe.jgi.boost.client.utils.ParseJsonResponse;
import gov.doe.jgi.boost.enums.FileFormat;
import gov.doe.jgi.boost.enums.Strategy;
import gov.doe.jgi.boost.enums.Vendor;
import gov.doe.jgi.boost.exception.BOOSTBackEndException;
import gov.doe.jgi.boost.exception.BOOSTClientException;

public class BOOSTOperations {

	static BOOSTClient client = new BOOSTClient(new BOOSTPreferences().getBOOSTToken());

	public static void codonJuggling(String filePath, boolean annotation, Strategy strategy, String host) {
		String codonJuggleJobUUID = null;
		try {
			codonJuggleJobUUID = client.codonJuggle(
					filePath,                 // input sequences
					annotation,               // exclusively 5'-3' coding sequences
					strategy,                 // codon selection strategy
					host,                     // predefined host
					FileFormat.SBOL);
		} catch (BOOSTClientException | BOOSTBackEndException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (codonJuggleJobUUID != null) {
			checkJobReport(codonJuggleJobUUID);
		}
	}

	public static void dnaVerification(String filePath, Vendor vendor, String sequencePatterns) {
		String dnaVarificationJobUUID = null;
		try {
			dnaVarificationJobUUID = client.dnaVarification(
					filePath,                  // input sequence
					vendor,                    // vendor
					sequencePatterns);         // sequence patterns
		} catch (BOOSTClientException | BOOSTBackEndException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dnaVarificationJobUUID != null) {
			checkJobReport(dnaVarificationJobUUID);
		}
	}

	public static void polishing(String filePath, boolean annotation, Vendor vendor, Strategy strategy, String host) {
		String polishDNAJobUUID = null;
		try {
			polishDNAJobUUID = client.polish(
					filePath,                  // input sequence
					annotation,                // encoding sequences support sequence feature annotations
					vendor,                    // vendor
					strategy,                  // codon selection strategy
					FileFormat.SBOL,           // output format
					host);                     // predefined host
		} catch (BOOSTClientException | BOOSTBackEndException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (polishDNAJobUUID != null) {
			checkJobReport(polishDNAJobUUID);
		}
	}


	// // partitioning of DNA
	// String partitiongDNAJobUUID = client.partition(
	// selectedFilePath, // input sequence
	// "aaacccgggttt", // 5-prime-vector-overlap
	// "tttgggcccaaa", // 3-prime-vector-overlap
	// Integer.toString(15), // min-BB-length
	// Integer.toString(3000), // max-BB-length
	// Double.toString(4.0), // minimum overlap GC
	// Double.toString(40.0), // optimum overlap GC
	// Double.toString(62.0), // maximum overlap GC
	// Integer.toString(5), // minimum overlap length
	// Integer.toString(25), // optimum overlap length
	// Integer.toString(30), // maximum overlap length
	// Integer.toString(20), // min. primer length
	// Integer.toString(40), // max. primer length
	// Integer.toString(60)); // max. primer Tm
	// if (null != partitiongDNAJobUUID) {
	// jobUUIDs.add(partitiongDNAJobUUID);
	// System.out.println("Data for Partation :" + partitiongDNAJobUUID);
	// }

	static void checkJobReport(String jobUUID) {
		JSONObject jobReport = null;
		try {
			while (null == (jobReport = client.getJobReport(jobUUID))) {

				// if the job isn't finished, then we wet some seconds
				// and check again
				System.out.println("Job " + jobUUID + " is not finished yet.");
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
				}
			}
		} catch (BOOSTClientException | BOOSTBackEndException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// output of the job report (which is a JSON object)
		String resopnse = ParseJsonResponse.parseCodonJuggleResponse(jobReport);
		System.out.println(resopnse);
	}
}
