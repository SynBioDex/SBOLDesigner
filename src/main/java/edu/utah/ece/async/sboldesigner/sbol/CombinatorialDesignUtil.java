package edu.utah.ece.async.sboldesigner.sbol;

import java.net.URI;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JOptionPane;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OperatorType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.StrategyType;
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
				"Select a combinatorial derivation to sample or enumerate", "Create Combinatorial Design",
				JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		if (selection == null) {
			return null;
		}

		CombinatorialDerivation derivation = selection.derivation;
		HashSet<ComponentDefinition> enumeration = enumerate(doc, selection.derivation);

		if (!derivation.isSetStrategy()) {
			int choice = JOptionPane.showOptionDialog(null,
					"The strategy property is not set.  Would you like to enumerate or sample?",
					"Combinatorial Design Strategy", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					StrategyType.values(), StrategyType.values()[0]);
			if (choice == JOptionPane.CLOSED_OPTION) {
				return null;
			}

			derivation.setStrategy(StrategyType.values()[choice]);
		}

		SBOLDocument generated = new SBOLDocument();

		if (derivation.getStrategy() == StrategyType.SAMPLE) {
			ComponentDefinition[] a = enumeration.toArray(new ComponentDefinition[0]);
			doc.createRecursiveCopy(generated, a[ThreadLocalRandom.current().nextInt(a.length)]);
		} else if (derivation.getStrategy() == StrategyType.ENUMERATE) {
			for (ComponentDefinition CD : enumeration) {
				doc.createRecursiveCopy(generated, CD);
			}
		} else {
			throw new IllegalArgumentException();
		}

		return generated;
	}

	private static ComponentDefinition createTemplateCopy(SBOLDocument doc, CombinatorialDerivation derivation)
			throws SBOLValidationException {
		ComponentDefinition template = derivation.getTemplate();

		String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, template.getDisplayId() + "_GeneratedInstance",
				template.getVersion(), "CD", doc);
		ComponentDefinition copy = (ComponentDefinition) doc.createCopy(template, uniqueId, template.getVersion());
		copy.addWasDerivedFrom(template.getIdentity());

		copy.clearSequenceAnnotations();

		return copy;
	}

	private static HashSet<ComponentDefinition> enumerate(SBOLDocument doc, CombinatorialDerivation derivation)
			throws SBOLValidationException {
		HashSet<ComponentDefinition> parents = new HashSet<>();
		parents.add(createTemplateCopy(doc, derivation));

		for (VariableComponent vc : derivation.getVariableComponents()) {
			HashSet<ComponentDefinition> newParents = new HashSet<>();

			for (ComponentDefinition parent : parents) {
				for (HashSet<ComponentDefinition> children : group(collectVariants(doc, vc), vc.getOperator())) {
					// create copy of parent
					String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, parent.getDisplayId(),
							parent.getVersion(), "CD", doc);
					ComponentDefinition newParent = (ComponentDefinition) doc.createCopy(parent, uniqueId, "1");

					// add children
					ComponentDefinition template = derivation.getTemplate();
					addChildren(template, template.getComponent(vc.getVariableURI()), newParent, children);

					// add to newParents
					newParents.add(newParent);
				}
			}

			parents = newParents;
		}

		return parents;
	}

	private static void addChildren(ComponentDefinition originalTemplate, Component originalComponent,
			ComponentDefinition newParent, HashSet<ComponentDefinition> children) throws SBOLValidationException {
		Component newComponent = newParent.getComponent(originalComponent.getDisplayId());

		if (children.isEmpty()) {
			removeConstraintReferences(newParent, newComponent);
			newParent.removeComponent(newComponent);
			return;
		}

		boolean first = true;
		for (ComponentDefinition child : children) {
			if (first) {
				// take over the definition of newParent's version of the
				// original component
				newComponent.setDefinition(child.getIdentity());
				first = false;
			} else {
				// create a new component
				String uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null, child.getDisplayId() + "_Component",
						"1", "Component", null);
				Component link = newParent.createComponent(uniqueId, AccessType.PUBLIC, child.getIdentity());
				link.addWasDerivedFrom(originalComponent.getIdentity());

				// create a new 'prev precedes link' constraint
				Component oldPrev = getBeforeComponent(originalTemplate, originalComponent);
				if (oldPrev != null) {
					Component newPrev = newParent.getComponent(oldPrev.getDisplayId());
					if (newPrev != null) {
						uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null,
								newParent.getDisplayId() + "_SequenceConstraint", null, "SequenceConstraint", null);
						newParent.createSequenceConstraint(uniqueId, RestrictionType.PRECEDES, newPrev.getIdentity(),
								link.getIdentity());
					}
				}

				// create a new 'link precedes next' constraint
				Component oldNext = getAfterComponent(originalTemplate, originalComponent);
				if (oldNext != null) {
					Component newNext = newParent.getComponent(oldNext.getDisplayId());
					if (newNext != null) {
						uniqueId = SBOLUtils.getUniqueDisplayId(newParent, null,
								newParent.getDisplayId() + "_SequenceConstraint", null, "SequenceConstraint", null);
						newParent.createSequenceConstraint(uniqueId, RestrictionType.PRECEDES, link.getIdentity(),
								newNext.getIdentity());
					}
				}
			}
		}
	}

	private static void removeConstraintReferences(ComponentDefinition newParent, Component newComponent) {
		for (SequenceConstraint sc : newParent.getSequenceConstraints()) {
			if (sc.getSubject().equals(newComponent) || sc.getObject().equals(newComponent)) {
				newParent.removeSequenceConstraint(sc);
			}
		}
	}

	private static Component getBeforeComponent(ComponentDefinition template, Component component) {
		for (SequenceConstraint sc : template.getSequenceConstraints()) {
			if (sc.getRestriction().equals(RestrictionType.PRECEDES) && sc.getObject().equals(component)) {
				return sc.getSubject();
			}
		}
		return null;
	}

	private static Component getAfterComponent(ComponentDefinition template, Component component) {
		for (SequenceConstraint sc : template.getSequenceConstraints()) {
			if (sc.getRestriction().equals(RestrictionType.PRECEDES) && sc.getSubject().equals(component)) {
				return sc.getObject();
			}
		}
		return null;
	}

	private static HashSet<HashSet<ComponentDefinition>> group(HashSet<ComponentDefinition> variants,
			OperatorType operator) {
		HashSet<HashSet<ComponentDefinition>> groups = new HashSet<>();

		for (ComponentDefinition CD : variants) {
			HashSet<ComponentDefinition> group = new HashSet<>();
			group.add(CD);
			groups.add(group);
		}

		if (operator == OperatorType.ONE) {
			return groups;
		}

		if (operator == OperatorType.ZEROORONE) {
			groups.add(new HashSet<>());
			return groups;
		}

		groups.clear();
		generateCombinations(groups, variants.toArray(new ComponentDefinition[0]), 0, new HashSet<>());
		if (operator == OperatorType.ONEORMORE) {
			return groups;
		}

		if (operator == OperatorType.ZEROORMORE) {
			groups.add(new HashSet<>());
			return groups;
		}

		throw new IllegalArgumentException(operator.toString() + " operator not supported");
	}

	/**
	 * Generates all combinations except the empty set.
	 */
	private static void generateCombinations(HashSet<HashSet<ComponentDefinition>> groups,
			ComponentDefinition[] variants, int i, HashSet<ComponentDefinition> set) {
		if (i == variants.length) {
			if (!set.isEmpty()) {
				groups.add(set);
			}
			return;
		}

		HashSet<ComponentDefinition> no = new HashSet<>(set);
		generateCombinations(groups, variants, i + 1, no);

		HashSet<ComponentDefinition> yes = new HashSet<>(set);
		yes.add(variants[i]);
		generateCombinations(groups, variants, i + 1, yes);
	}

	private static HashSet<ComponentDefinition> collectVariants(SBOLDocument doc, VariableComponent vc)
			throws SBOLValidationException {
		HashSet<ComponentDefinition> variants = new HashSet<>();

		// add all variants
		variants.addAll(vc.getVariants());

		// add all variants from variantCollections
		for (Collection c : vc.getVariantCollections()) {
			for (TopLevel tl : c.getMembers()) {
				if (tl instanceof ComponentDefinition) {
					variants.add((ComponentDefinition) tl);
				}
			}
		}

		// add all variants from variantDerivations
		for (CombinatorialDerivation derivation : vc.getVariantDerivations()) {
			variants.addAll(enumerate(doc, derivation));
		}

		return variants;
	}
}
