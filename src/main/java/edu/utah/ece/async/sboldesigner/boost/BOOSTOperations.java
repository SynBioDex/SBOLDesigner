package edu.utah.ece.async.sboldesigner.boost;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLDesign;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import gov.doe.jgi.boost.client.BOOSTClient;
import gov.doe.jgi.boost.client.utils.JsonResponseParser;
import gov.doe.jgi.boost.enums.FileFormat;
import gov.doe.jgi.boost.enums.Strategy;
import gov.doe.jgi.boost.enums.Vendor;
import gov.doe.jgi.boost.exception.BOOSTBackEndException;
import gov.doe.jgi.boost.exception.BOOSTClientException;

public class BOOSTOperations {

	static BOOSTClient client = new BOOSTClient(new BOOSTPreferences().getBOOSTToken());
	
	static String userNamespace = SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString();
	private static String targetNamespace = "https://boost.jgi.doe.gov/";
	

	public static void codonJuggling(SBOLDocument currentDesign, boolean annotation, Strategy strategy, String host) {
		String codonJuggleJobUUID = null;
			try {
				codonJuggleJobUUID = client.codonJuggle(
						currentDesign,            // input sequences
						targetNamespace,
						annotation,               // exclusively 5'-3' coding sequences
						strategy,                 // codon selection strategy
						host,                     // predefined host
						FileFormat.SBOL);
			} catch (JSONException | SBOLConversionException | BOOSTClientException | 
					       BOOSTBackEndException | IOException e) {
				
				e.printStackTrace();
			}

		if (codonJuggleJobUUID != null) {
			checkJobReport(codonJuggleJobUUID);
		}
	}

	public static void dnaVerification(SBOLDocument currentDesign, Vendor vendor, String sequencePatterns) {
		String dnaVarificationJobUUID = null;
			try {
				dnaVarificationJobUUID = client.dnaVarification(
						currentDesign,             // input sequence
						targetNamespace,
						vendor,                    // vendor
						sequencePatterns);
			} catch (JSONException | SBOLConversionException | BOOSTClientException | 
					BOOSTBackEndException | IOException e) {
				
				e.printStackTrace();
			}  
			
		if (dnaVarificationJobUUID != null) {
			checkJobReport(dnaVarificationJobUUID);
		}
	}

	public static void polishing(SBOLDocument currentDesign, boolean annotation, Vendor vendor, Strategy strategy, String host) {
		String polishDNAJobUUID = null;

			try {
				polishDNAJobUUID = client.polish(
						currentDesign,             // input sequence
						targetNamespace,
						annotation,                // encoding sequences support sequence feature annotations
						vendor,                    // vendor
						strategy,                  // codon selection strategy
						FileFormat.SBOL,           // output format
						host);
			} catch (JSONException | UnsupportedEncodingException | SBOLConversionException | 
					BOOSTClientException | BOOSTBackEndException e) {
		
				e.printStackTrace();
			}       
			
		if (polishDNAJobUUID != null) {
			checkJobReport(polishDNAJobUUID);
		}
	}

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
			e.printStackTrace();
		}
		 
		// output of the job report (which is a JSON object)
		String response = JsonResponseParser.parseCodonJuggleResponse(jobReport);
		// convert String into InputStream
		InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));

		try {
			// extracting the returned SBOL string into an SBOLDocument
			SBOLDocument modifiedDocument = SBOLReader.read(stream);
			String boostUri = "https://boost.jgi.doe.gov/";
			
			Set<ComponentDefinition> componentDef = modifiedDocument.getRootComponentDefinitions();
			for(ComponentDefinition componentDefination : componentDef) {
				//each root CD, you should look for whether there is a wasDerivedFrom link (CD.getWasDerivedFroms()) 
				//to another one of the roots
					   
			}	
		} catch (SBOLValidationException | IOException | SBOLConversionException e) {
			e.printStackTrace();
		}
		System.out.println(response);
	}	
}
