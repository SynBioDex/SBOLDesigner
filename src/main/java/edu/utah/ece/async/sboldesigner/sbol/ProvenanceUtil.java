package edu.utah.ece.async.sboldesigner.sbol;

import org.sbolstandard.core2.GenericTopLevel;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.sbolstandard.core2.Activity;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Association;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

public class ProvenanceUtil {

	/*
	 * Adds an SBOLDesignerActivity -> SBOLDesignerAgent to the wasDerivedFrom
	 * of every TopLevel. If an SBOLDesignerActivity already exists, updates the
	 * end time.
	 */
	public static void createProvenance(SBOLDocument doc, ComponentDefinition root) throws SBOLValidationException {
		// Create the activity
		Activity activity = doc.createActivity(root.getDisplayId() + "_Activity", "1");
		activity.setEndedAtTime(DateTime.now());

		// Set the creator
		String creator = SBOLEditorPreferences.INSTANCE.getUserInfo().getName();
		activity.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), creator);

		// Create the qualifiedAssociation
		URI designerURI = URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/2.2");
		Association association = activity.createAssociation("Association", designerURI);
		association.addRole(URI.create("http://sbols.org/v2#sequenceEditor"));

		// Link with all TopLevels
		for (TopLevel tl : doc.getTopLevels()) {
			boolean exists = false;

			// Update existing Activities
			for (URI uri : tl.getWasGeneratedBys()) {
				TopLevel generatedBy = doc.getTopLevel(uri);
				if (generatedBy != null && generatedBy.getDisplayId().contains("SBOLDesignerActivity")
						&& generatedBy instanceof Activity) {
					((Activity) generatedBy).setEndedAtTime(activity.getEndedAtTime());
					exists = true;
				}
			}

			// Attach if there is no existing Activity
			if (!exists) {
				tl.addWasGeneratedBy(activity.getIdentity());
			}
		}
	}

	/*
	 * The old version of createProvenance.
	 */
	private void addSBOLDesignerAnnotation(ComponentDefinition cd, SBOLDocument design) throws SBOLValidationException {
		// get/create SBOLDesigner agent
		URI designerURI = URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/2.2");
		// unused because designerURI will be dereferenced on SynBioHub
		// designerURI = createSBOLDesignerAgent().getIdentity();

		// get/create the activity
		URI activityURI = URI
				.create(design.getDefaultURIprefix() + cd.getDisplayId() + "_SBOLDesigner" + "/" + cd.getVersion());
		GenericTopLevel oldActivity = design.getGenericTopLevel(activityURI);

		if (oldActivity != null) {
			design.removeGenericTopLevel(oldActivity);
		}

		GenericTopLevel partActivity = design.createGenericTopLevel(design.getDefaultURIprefix(),
				cd.getDisplayId() + "_SBOLDesigner", cd.getVersion(),
				new QName("http://www.w3.org/ns/prov#", "Activity", "prov"));

		String creator = SBOLEditorPreferences.INSTANCE.getUserInfo().getName();
		partActivity.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), creator);

		partActivity.createAnnotation(new QName("http://www.w3.org/ns/prov#", "endedAtTime", "prov"),
				ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));

		// create the qualified usage annotation
		Annotation agentAnnotation = new Annotation(new QName("http://www.w3.org/ns/prov#", "agent", "prov"),
				designerURI);

		partActivity.createAnnotation(new QName("http://www.w3.org/ns/prov#", "qualifiedAssociation", "prov"),
				new QName("http://www.w3.org/ns/prov#", "Association", "prov"),
				URI.create(partActivity.getIdentity().toString() + "/association"),
				new ArrayList<Annotation>(Arrays.asList(agentAnnotation)));

		// link the cd/part to partActivity
		Annotation prev = null;
		for (Annotation a : cd.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("wasGeneratedBy") && a.isURIValue()
					&& a.getURIValue().equals(designerURI)) {
				prev = a;
			}
		}

		if (prev != null) {
			cd.removeAnnotation(prev);
		}

		cd.createAnnotation(new QName("http://www.w3.org/ns/prov#", "wasGeneratedBy", "prov"),
				partActivity.getIdentity());
	}

	/*
	 * The reference implementation for generating the SBOLDesigner Agent.
	 */
	private GenericTopLevel createSBOLDesignerAgent(SBOLDocument design) throws SBOLValidationException {
		GenericTopLevel designerAgent = design
				.getGenericTopLevel(URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/2.2"));

		if (designerAgent == null) {
			designerAgent = design.createGenericTopLevel("http://www.async.ece.utah.edu", "SBOLDesigner", "2.2",
					new QName("http://www.w3.org/ns/prov#", "Agent", "prov"));
			designerAgent.setName("SBOLDesigner CAD Tool");
			designerAgent.setDescription(
					"SBOLDesigner is a simple, biologist-friendly CAD software tool for creating and manipulating the sequences of genetic constructs using the Synthetic Biology Open Language (SBOL) 2.0 data model. Throughout the design process, SBOL Visual symbols, a system of schematic glyphs, provide standardized visualizations of individual parts. SBOLDesigner completes a workflow for users of genetic design automation tools. It combines a simple user interface with the power of the SBOL standard and serves as a launchpad for more detailed designs involving simulations and experiments. Some new features in SBOLDesigner are SynBioHub integration, local repositories, importing of parts/sequences from existing files, import and export of GenBank and FASTA files, extended role ontology support, the ability to partially open designs with multiple root ComponentDefinitions, backward compatibility with SBOL 1.1, and versioning.");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Michael Zhang");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Chris Myers");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Michal Galdzicki");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Bryan Bartley");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Sean Sleight");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Evren Sirin");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"John Gennari");
		}

		return designerAgent;
	}

}
