package edu.utah.ece.async.sboldesigner.boost;

import java.awt.font.OpenType;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SelectedFilePath {

	public String selectedFilePath;
	
	public SelectedFilePath() {
		
		JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("SBOL file", "xml", "rdf", "sbol"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("GenBank", "gb", "gbk"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA", "fasta"));
		chooser.setAcceptAllFileFilterUsed(false);
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			   this.selectedFilePath =  chooser.getSelectedFile().toString();
			} else {
			  System.out.println("No Selection ");
			}
	}
	
	public String getSelectedFilePath() {
		return selectedFilePath;
	}
}
