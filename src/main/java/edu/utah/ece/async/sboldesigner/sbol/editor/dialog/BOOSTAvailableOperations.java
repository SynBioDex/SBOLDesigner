package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public class BOOSTAvailableOperations extends JDialog implements ActionListener{

	private Component parent;
	
	private JRadioButton reverseTranslationBtn = new JRadioButton("Reverse-Translation of protein to DNA sequences");
	private JRadioButton codonJugglingBtn = new JRadioButton("Codon-Juggling of protein coding DNA sequences");
	private JRadioButton dnaVerificationBtn = new JRadioButton("Verification of DNA Sequences against synthesis constraints");
	private JRadioButton sequenceModificationBtn = new JRadioButton("Modification of protein coding sequences (\"CDS\") for efficient synthesis");
	private JRadioButton sequencePartitionBtn = new JRadioButton("Partition of large DNA sequences into synthesizable building blocks");
	
	private JButton submitButton = new JButton("Submit");
	private JButton cancelButton = new JButton("Cancel");
	
	
	public BOOSTAvailableOperations(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), "Available BOOST Tasks ", true);
		this.parent = parent;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		reverseTranslationBtn.addActionListener(this);
		codonJugglingBtn.addActionListener(this);
		dnaVerificationBtn.addActionListener(this);
		sequenceModificationBtn.addActionListener(this);
		sequencePartitionBtn.addActionListener(this);
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		mainPanel.add(reverseTranslationBtn);
		mainPanel.add(codonJugglingBtn);
		mainPanel.add(dnaVerificationBtn);
		mainPanel.add(sequenceModificationBtn);
		mainPanel.add(sequencePartitionBtn);
		mainPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel infoLabel = new JLabel(
				"Please select the operaton(s) you want to perform with your genatic constructs");
		
		Container contentPane = getContentPane();
		contentPane.add(infoLabel, BorderLayout.PAGE_START);
		contentPane.add(mainPanel, BorderLayout.LINE_START);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO: Handle available tasks of BOOST 	
	}
}
