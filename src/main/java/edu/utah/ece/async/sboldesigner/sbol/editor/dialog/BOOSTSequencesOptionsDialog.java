package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

public class BOOSTSequencesOptionsDialog extends JDialog implements ActionListener{
	
	private Component parent;
	
	private JRadioButton mDNA = new JRadioButton("DNA");
	private JRadioButton mRNA = new JRadioButton("RNA");
	private JRadioButton mProtein = new JRadioButton("Protein");
	private JButton submitButton = new JButton("Submit");
	private JButton cancelButton = new JButton("Cancel");
	
	public BOOSTSequencesOptionsDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), "Sequences Type", true);
		this.parent = parent;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		ButtonGroup taskGroup = new ButtonGroup();
		taskGroup.add(mDNA);
		taskGroup.add(mRNA);
		taskGroup.add(mProtein);
		
		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel taskPanel = DialogUtils.buildDecisionArea(1); // 1 for Y_AXIS alignment
		taskPanel.add(mDNA);
		taskPanel.add(mRNA);
		taskPanel.add(mProtein);
		taskPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel infoLabel = new JLabel("Please select genetic sequence type to perform BOOST Operation(s)");
		Container contentPane = getContentPane();
		DialogUtils.setUI(contentPane, infoLabel, taskPanel, buttonPane);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO: Handle actions
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}else if(e.getSource() == submitButton) {
			if(mDNA.isSelected()){
				new BOOSTAvailableOperations(parent, "selectedDNA");
				}else if(mRNA.isSelected()){
				//do what you want 
				  }else if(mProtein.isSelected()) {
					  new BOOSTAvailableOperations(parent, "selectedProtein");
				  }
	   }		
	}
}
