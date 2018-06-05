package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

public class BOOSTReverseTranslation extends JDialog implements ActionListener {
	
	private JRadioButton enterTableButton = new JRadioButton("Enter Codon Usage Table");
	private JRadioButton uploadTableButton = new JRadioButton("Upload Codon Usage Table");
	private JRadioButton predefinedHostButton = new JRadioButton("Predefined Codon Usage Table");
	private JButton submitButton = new JButton("Submit");
	private JButton cancelButton = new JButton("Cancel");

	public BOOSTReverseTranslation(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), "Reverse Translation", true);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		BOOSTReverseTranslation dialog = new BOOSTReverseTranslation();
		dialog.setVisible(true);
		
	}

	/**
	 * Create the dialog.
	 */
	public BOOSTReverseTranslation() {
		setBounds(100, 100, 639, 390);
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		JLabel headingLabel = new JLabel("Please make choice for given parameters\r\n");
		
		JLabel codonSelectionStrategyLabel = new JLabel("Codon Selection Strategy");
		codonSelectionStrategyLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		
		JLabel strategyLabel = new JLabel("Select Codon Selection Strategy:\r\n");
		
		String[] strategyList = new String[] {"Random", "Balanced", "Mostly Used", "Least Different"};
		JComboBox<String> strategyComboBox = new JComboBox<>(strategyList);

		ButtonGroup strategyButtonGruop = new ButtonGroup();
		strategyButtonGruop.add(enterTableButton);
		strategyButtonGruop.add(uploadTableButton);
		strategyButtonGruop.add(predefinedHostButton);
		
		enterTableButton.addActionListener(this);
		uploadTableButton.addActionListener(this);
		predefinedHostButton.addActionListener(this);
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		enterTableButton.setActionCommand("on");
		
		JPanel codonTablePlaceholder = new JPanel();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(357)
							.addComponent(cancelButton)
							.addGap(18)
							.addComponent(submitButton))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(strategyLabel)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(enterTableButton)
									.addGap(18)
									.addComponent(uploadTableButton)
									.addGap(18)
									.addComponent(predefinedHostButton))
								.addComponent(strategyComboBox, 0, 531, Short.MAX_VALUE)
								.addComponent(headingLabel)
								.addComponent(codonTablePlaceholder, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE))))
					.addGap(74))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(205)
					.addComponent(codonSelectionStrategyLabel)
					.addContainerGap(260, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addComponent(headingLabel)
					.addGap(18)
					.addComponent(codonSelectionStrategyLabel)
					.addGap(11)
					.addComponent(strategyLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(strategyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(enterTableButton)
						.addComponent(uploadTableButton)
						.addComponent(predefinedHostButton))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(codonTablePlaceholder, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(submitButton)
						.addComponent(cancelButton))
					.addContainerGap(14, Short.MAX_VALUE))
		);
		getContentPane().setLayout(groupLayout);	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO: Handle action
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}else if(e.getSource() == submitButton) {
			
		}else if(e.getSource() == enterTableButton) {
			
		}else if(e.getSource() == uploadTableButton) {
			
		}else if(e.getSource() == predefinedHostButton) {
			
		}	
	}
}
