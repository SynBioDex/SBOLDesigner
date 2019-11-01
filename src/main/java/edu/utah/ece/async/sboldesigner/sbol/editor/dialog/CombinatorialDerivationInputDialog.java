package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

public class CombinatorialDerivationInputDialog {

	private Component parent;
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

	private static Derivation[] getDerivations(SBOLDocument doc, ComponentDefinition template) {
		ArrayList<Derivation> derivations = new ArrayList<>();

		for (CombinatorialDerivation derivation : doc.getCombinatorialDerivations()) {
			if (template == null || derivation.getTemplate().equals(template)) {
				Derivation d = new Derivation(derivation);
				derivations.add(d);
			}
		}
		
		//for(Collection )

		return derivations.toArray(new Derivation[0]);
	}

	public static CombinatorialDerivation pickCombinatorialDerivation(Component parent, SBOLDocument doc, ComponentDefinition template) {
		Derivation[] options = getDerivations(doc, template);

		if (options.length == 0) {
			return null;
		}

		if (options.length == 1) {
			return options[0].derivation;
		}

//		Derivation selection = (Derivation) JOptionPane.showInputDialog(null,
//				"Please select a combinatorial derivation.", "Pick Combinatorial Design", JOptionPane.DEFAULT_OPTION,
//				null, options, options[0]);
		SBOLDocument selection = new ComboDerivDialog(parent, doc, template).getInput();
		if (selection == null) {
			return null;
		}

		//return selection.derivation;
		return selection.getCombinatorialDerivations().iterator().next();
	}

}
