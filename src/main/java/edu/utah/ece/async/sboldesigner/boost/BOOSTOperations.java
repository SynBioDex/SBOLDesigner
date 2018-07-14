package edu.utah.ece.async.sboldesigner.boost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import gov.doe.jgi.boost.client.BOOSTClient;
import gov.doe.jgi.boost.client.utils.DocumentConversionUtils;
import gov.doe.jgi.boost.client.utils.JsonResponseParser;
import gov.doe.jgi.boost.enums.FileFormat;
import gov.doe.jgi.boost.enums.Strategy;
import gov.doe.jgi.boost.enums.Vendor;
import gov.doe.jgi.boost.exception.BOOSTBackEndException;
import gov.doe.jgi.boost.exception.BOOSTClientException;

public class BOOSTOperations {

	static BOOSTClient client = new BOOSTClient(new BOOSTPreferences().getBOOSTToken());
	
	static String targetNamespace = SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString();
	
	public static void codonJuggling(SBOLDocument currentDesign, boolean annotation, Strategy strategy, String host) {
		String codonJuggleJobUUID = null;
		JSONObject jobReport = null;
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
			jobReport = checkJobReport(codonJuggleJobUUID);
			String response = JsonResponseParser.parseCodonJuggleResponse(jobReport);
			try {
				SBOLDocument modifiedDocument = DocumentConversionUtils.stringToSBOLDocument(response);
				// fetch root ComponentDefination of modifiedDocument
				Set<ComponentDefinition> componentDef = modifiedDocument.getRootComponentDefinitions();
				for (ComponentDefinition componentDefination : componentDef) {
					URI rootUri = componentDefination.getIdentity();
					  System.out.println(rootUri);
				}
			} catch (SBOLValidationException | IOException | SBOLConversionException e) {
				e.printStackTrace();
			}
		}	
	}

	public static void dnaVerification(SBOLDocument currentDesign, Vendor vendor, String sequencePatterns) {
		String dnaVarificationJobUUID = null;
		JSONObject jobReport = null;
			try {
				dnaVarificationJobUUID = client.dnaVerification(
						currentDesign, 
						targetNamespace,
						vendor, 
						sequencePatterns);
				
			} catch (JSONException | SBOLConversionException | BOOSTClientException | 
					BOOSTBackEndException | IOException e) {
				
				e.printStackTrace();
			}  
			
		if (dnaVarificationJobUUID != null) {
			jobReport =  checkJobReport(dnaVarificationJobUUID);
			String response = jobReport.toString();
			System.out.println(response);
		}
	}

	public static void polishing(SBOLDocument currentDesign, boolean annotation, Vendor vendor, Strategy strategy, String host) {
		String polishDNAJobUUID = null;
		JSONObject jobReport = null;
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
			jobReport = checkJobReport(polishDNAJobUUID);
		}
	}

	static JSONObject checkJobReport(String jobUUID) {
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
		return jobReport; 
	}
}
