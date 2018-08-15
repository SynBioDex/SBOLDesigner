package edu.utah.ece.async.sboldesigner.boost;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;

public class AvailableOperationsDialog extends JDialog implements ActionListener{

	private Component parent;
	private ButtonGroup taskButtonGroup;
	private SBOLDocument currentDesign;
	
	private JRadioButton codonJugglingBtn = new JRadioButton("Codon-Juggling of protein coding DNA sequences");
	private JRadioButton dnaVerificationBtn = new JRadioButton("Verification of DNA Sequences against synthesis constraints");
	private JRadioButton sequenceModificationBtn = new JRadioButton("Modification of protein coding sequences (\"CDS\") for efficient synthesis");
	private JRadioButton sequencePartitionBtn = new JRadioButton("Partition of large DNA sequences into synthesizable building blocks");
	private JButton submitButton = new JButton("Submit");
	private JButton cancelButton = new JButton("Cancel");
	
	
	public AvailableOperationsDialog(Component parent, SBOLDocument currentDesign) {
		super(JOptionPane.getFrameForComponent(parent), "Available BOOST Tasks ", true);
		this.parent = parent;
		this.currentDesign = currentDesign;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		codonJugglingBtn.addActionListener(this);
		dnaVerificationBtn.addActionListener(this);
		sequenceModificationBtn.addActionListener(this);
		sequencePartitionBtn.addActionListener(this);
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		taskButtonGroup = new ButtonGroup();
		taskButtonGroup.add(codonJugglingBtn);
		taskButtonGroup.add(dnaVerificationBtn);
		taskButtonGroup.add(sequenceModificationBtn);
		taskButtonGroup.add(sequencePartitionBtn);
		
		this.codonJugglingBtn.setActionCommand("codonJuggle");
		this.dnaVerificationBtn.setActionCommand("dnaVerification");
		this.sequenceModificationBtn.setActionCommand("polishing");
		this.sequencePartitionBtn.setActionCommand("partition");
		
		
		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel taskPanel = DialogUtils.buildDecisionArea(1); // 1 for Y_AXIS alignment
		taskPanel.add(codonJugglingBtn);
		taskPanel.add(dnaVerificationBtn);
		taskPanel.add(sequenceModificationBtn);
		taskPanel.add(sequencePartitionBtn);
		taskPanel.setAlignmentX(LEFT_ALIGNMENT);
		

		JLabel infoLabel = new JLabel("Please select the operaton(s) you want to perform with your genatic constructs");

		Container contentPane = getContentPane();
		DialogUtils.setUI(contentPane, infoLabel, taskPanel, buttonPane);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		} else if (e.getSource() == submitButton) {
			setVisible(false);
			
			String taskSelected =  this.taskButtonGroup.getSelection().getActionCommand();
			if (taskSelected == "codonJuggle") {
				setVisible(false);
				new CodonJugglingDialog(parent, currentDesign);
				return;
			} else if (taskSelected == "dnaVerification") {
				setVisible(false);
				new DNAVerificationDialog(parent, currentDesign);
				return;
			} else if (taskSelected == "polishing") {
				setVisible(false);
				new CodonPolishingDialog(parent, currentDesign);
				return;
			} else if (taskSelected == "partition") {
                setVisible(false);
                new DNAPartition(parent, currentDesign);
                return;
			}  
		return;
		}
	}
}
