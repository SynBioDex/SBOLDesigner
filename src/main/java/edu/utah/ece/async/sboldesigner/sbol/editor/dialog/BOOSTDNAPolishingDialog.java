package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
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
import javax.swing.KeyStroke;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle.ComponentPlacement;

public class BOOSTDNAPolishingDialog extends JDialog implements ActionListener{

	private Component parent;
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	
	public BOOSTDNAPolishingDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), "DNA Polishing", true);
		this.parent = parent;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);
		submitButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel mainPanel = new JPanel();
		uiMainPanel(mainPanel);
		JLabel infoLabel = new JLabel("Please make choice for given parameters");
		infoLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
	
		
		Container contentPane = getContentPane();
		contentPane.add(infoLabel, BorderLayout.PAGE_START);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
		
	
	
	private void uiMainPanel(JPanel mainPanel) {
        JLabel strategyLabel = new JLabel("Select Codon Selection Strategy:");
        JLabel vendorLabel = new JLabel("Select Vendor of your Choice:");
        JLabel predefinedLabel = new JLabel("Predefined Codon Uses table:");
        JLabel annotationLabel = new JLabel("Encoding Sequence support annotation feature:");

		JComboBox<String> strategyComboBox = new JComboBox<>(new String[] {"Random", "Balenced",
				"Least Different", "Mostly Used", "Balenced2Random", "Relexed Weight"});
		
		JComboBox<String> vendorComboBox = new JComboBox<>(new String[] {"Integrated DNA Technalogies",
				"Thermo Fisher (Life Technalogies)", "SGI-DNA", "DOE Joint Genome Institute (JGI)",
				"Twist Bioscience (non- clonal)", "Twist Bioscience (clonal)"});
		
		JComboBox<String> annotationComboBox = new JComboBox<>(new String[] {"Yes", "No"});
		JComboBox<String> predefinedComboBox = new JComboBox<>(new String[] {"Bacillus Subtilis",
			    "Arabidapsis thaliana", "Escherichia coli", "Saccharamyces cere"});
		
		GroupLayout groupLayout = new GroupLayout(mainPanel);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(26)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(strategyLabel)
						.addComponent(vendorLabel)
						.addComponent(predefinedLabel)
						.addComponent(annotationLabel))
					.addPreferredGap(ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(annotationComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(predefinedComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(vendorComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(strategyComboBox, 0, 198, Short.MAX_VALUE))
					.addContainerGap(72, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(23)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(strategyLabel)
						.addComponent(strategyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(31)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(vendorLabel)
						.addComponent(vendorComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(29)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(predefinedLabel)
						.addComponent(predefinedComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(36)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(annotationLabel)
						.addComponent(annotationComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(134, Short.MAX_VALUE))
		);
		mainPanel.setLayout(groupLayout);
		
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stu
		
	}
}