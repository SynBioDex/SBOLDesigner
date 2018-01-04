package edu.utah.ece.async.sboldesigner.sbol;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import org.sbolstandard.core2.VariableComponent;

public class CombinatorialDesignUtil {

	private static Derivation[] getDerivations(SBOLDocument doc) {
		Derivation[] derivations = new Derivation[doc.getCombinatorialDerivations().size()];

		int i = 0;
		for (CombinatorialDerivation derivation : doc.getCombinatorialDerivations()) {
			Derivation d = new Derivation(derivation);
			derivations[i] = d;
			i++;
		}

		return derivations;
	}

	private static class Derivation {
		CombinatorialDerivation derivation;

		public Derivation(CombinatorialDerivation derivation) {
			this.derivation = derivation;
		}

		@Override
		public String toString() {
			return derivation.getDisplayId();
		}
	}

	public static SBOLDocument createCombinatorialDesign(SBOLDocument doc) throws SBOLValidationException {
		if (doc.getCombinatorialDerivations().isEmpty()) {
			JOptionPane.showMessageDialog(null, "There are no combinatorial designs in this document.");
			return null;
		}

		Derivation[] options = getDerivations(doc);

		Derivation selection = (Derivation) JOptionPane.showInputDialog(null,
				"Select a combinatorial derivation to enumerate", "Create Combinatorial Design",
				JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		if (selection == null) {
			return null;
		}

		SBOLDocument generated = new SBOLDocument();
		for (ComponentDefinition CD : enumerate(doc, selection.derivation)) {
			doc.createRecursiveCopy(generated, CD);
		}

		return generated;
	}

	private static ComponentDefinition createTemplateCopy(SBOLDocument doc, ComponentDefinition template)
			throws SBOLValidationException {
		ComponentDefinition root = doc.createComponentDefinition(template.getDisplayId() + "_GeneratedInstance", "1",
				template.getTypes());
		root.setRoles(template.getRoles());
		root.addWasDerivedFrom(template.getIdentity());
		return root;
	}

	private static HashSet<ComponentDefinition> enumerate(SBOLDocument doc, CombinatorialDerivation derivation)
			throws SBOLValidationException {
		HashSet<ComponentDefinition> CDs = new HashSet<>();
		CDs.add(createTemplateCopy(doc, derivation.getTemplate()));

		for (VariableComponent vc : derivation.getVariableComponents()) {
			HashSet<ComponentDefinition> newCDs = new HashSet<>();

			for (ComponentDefinition parent : CDs) {
				// create copy of parent from CDs and add child, add to newCDs
				// TODO assuming operator is always 1
				for (ComponentDefinition child : gatherVariants(doc, vc)) {
					String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, parent.getDisplayId(),
							parent.getVersion(), "CD", doc);
					ComponentDefinition newCD = (ComponentDefinition) doc.createCopy(parent, uniqueId, "1");

					uniqueId = SBOLUtils.getUniqueDisplayId(newCD, null, child.getDisplayId() + "_Component", "1",
							"Component", doc);
					Component link = newCD.createComponent(uniqueId, AccessType.PUBLIC, child.getIdentity());
					link.addWasDerivedFrom(vc.getVariableURI());

					newCDs.add(newCD);
				}
			}

			CDs = newCDs;
		}

		return CDs;
	}

	private static HashSet<ComponentDefinition> gatherVariants(SBOLDocument doc, VariableComponent vc)
			throws SBOLValidationException {
		HashSet<ComponentDefinition> CDs = new HashSet<>();

		// add all variants
		CDs.addAll(vc.getVariants());

		// add all variants from variantCollections
		for (Collection c : vc.getVariantCollections()) {
			for (TopLevel tl : c.getMembers()) {
				if (tl instanceof ComponentDefinition) {
					CDs.add((ComponentDefinition) tl);
				}
			}
		}

		// add all variants from variantDerivations
		for (CombinatorialDerivation derivation : vc.getVariantDerivations()) {
			CDs.addAll(enumerate(doc, derivation));
		}

		return CDs;
	}
}
