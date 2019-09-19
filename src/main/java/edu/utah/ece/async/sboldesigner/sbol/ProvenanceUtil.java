package edu.utah.ece.async.sboldesigner.sbol;

import java.io.File;
import java.io.IOException;
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
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.GenericTopLevel;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.TopLevel;
import org.sbolstandard.core2.Usage;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

public class ProvenanceUtil {
	/**
	 * Adds an SBOLDesignerActivity -> SBOLDesignerAgent to the wasDerivedFrom
	 * of every TopLevel that doesn't have an Activity. If an
	 * SBOLDesignerActivity already exists on the root, updates the end time.
	 */
	public static void createProvenance(SBOLDocument doc, ComponentDefinition root) throws SBOLValidationException {
		createProvenance(doc, root, null);
	}

	private static final URI SEQUENCE_EDITOR = URI.create("http://sbols.org/v2#sequenceEditor");

	/**
	 * Same as others, except usage will get added as a usage of the Activity.
	 */
	public static void createProvenance(SBOLDocument doc, ComponentDefinition root, Identified usage)
			throws SBOLValidationException {
		// Create or get the activity
		String activityId = root.getDisplayId() + "_SBOLDesignerActivity";
		Activity activity = null;
		for (Activity a : doc.getActivities()) {
			if (root.getWasGeneratedBys().contains(a.getIdentity()) && a.getDisplayId().equals(activityId)) {
				activity = a;
				break;
			}
		}

		if (activity == null) {
			activity = doc.createActivity(activityId, "1");
		}

		// Set the ended at time
		activity.setEndedAtTime(DateTime.now());

		// Set the usage
		if (usage != null) {
			String usageId = usage.getDisplayId() + "_Usage";
			if (activity.getUsage(usageId) == null) {
				Usage used = activity.createUsage(usageId, usage.getIdentity());
				used.addRole(SEQUENCE_EDITOR);
			}
		}

		// Set the creator
		String creator = SBOLEditorPreferences.INSTANCE.getUserInfo().getName();
		boolean hasCreator = false;
		for (Annotation a : activity.getAnnotations()) {
			if (a.getQName().getLocalPart().equals("creator") && a.isStringValue()
					&& a.getStringValue().equals(creator)) {
				hasCreator = true;
				break;
			}
		}
		if (!hasCreator) {
			activity.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"), creator);
		}

		// Create the qualifiedAssociation
		URI designerURI = URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/3.0");
		String designerPrefix = "https://synbiohub.org/public/SBOL_Software/SBOLDesigner/";
		boolean hasAssociation = false;
		for (Association a : activity.getAssociations()) {
			if (a.getAgentURI() != null && a.getAgentURI().toString().startsWith(designerPrefix)) {
				hasAssociation = true;
				break;
			}
		}
		if (!hasAssociation) {
			Association association = activity.createAssociation("Association", designerURI);
			association.addRole(SEQUENCE_EDITOR);
		}

		// Link with all TopLevels
		for (TopLevel tl : doc.getTopLevels()) {
			// check if in namespace
			if (SBOLUtils.notInNamespace(tl) || tl instanceof Activity || tl instanceof CombinatorialDerivation) {
				continue;
			}

			boolean hasActivity = false;

			// Check if hasActivity
			for (URI uri : tl.getWasGeneratedBys()) {
				TopLevel generatedBy = doc.getTopLevel(uri);
				if (generatedBy != null && generatedBy.getDisplayId().equals(activity.getDisplayId())
						&& generatedBy instanceof Activity) {
					hasActivity = true;
				}
			}

			// Attach if there is no existing Activity
			if (!hasActivity) {
				tl.addWasGeneratedBy(activity.getIdentity());
			}
		}
	}

	/*
	 * The reference implementation for generating the SBOLDesigner Agent.
	 */
	private static GenericTopLevel createSBOLDesignerAgent(SBOLDocument design) throws SBOLValidationException {
		GenericTopLevel designerAgent = design
				.getGenericTopLevel(URI.create("https://synbiohub.org/public/SBOL_Software/SBOLDesigner/3.0"));

		if (designerAgent == null) {
			designerAgent = design.createGenericTopLevel("http://www.async.ece.utah.edu", "SBOLDesigner", "3.0",
					new QName("http://www.w3.org/ns/prov#", "Agent", "prov"));
			designerAgent.setName("SBOLDesigner CAD Tool");
			designerAgent.setDescription(
					"SBOLDesigner is a simple, biologist-friendly CAD software tool for creating and manipulating the sequences of genetic constructs using the Synthetic Biology Open Language (SBOL) 2 data model. Throughout the design process, SBOL Visual symbols, a system of schematic glyphs, provide standardized visualizations of individual parts. SBOLDesigner completes a workflow for users of genetic design automation tools. It combines a simple user interface with the power of the SBOL standard and serves as a launchpad for more detailed designs involving simulations and experiments. Some new features in SBOLDesigner are SynBioHub integration, local repositories, importing of parts/sequences from existing files, import and export of GenBank and FASTA files, extended role ontology support, the ability to partially open designs with multiple root ComponentDefinitions, backward compatibility with SBOL 1.1, and versioning.");
			designerAgent.createAnnotation(new QName("http://purl.org/dc/elements/1.1/", "creator", "dc"),
					"Samuel Bridge");
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
	
	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException {
		SBOLDocument doc = new SBOLDocument();
		createSBOLDesignerAgent(doc);
		
		SBOLWriter.write(doc, new File("C:/Users/Michael/Desktop/SBOLDesignerAgent.xml"));
	}
}
