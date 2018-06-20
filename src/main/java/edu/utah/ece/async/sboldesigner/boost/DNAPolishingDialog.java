package edu.utah.ece.async.sboldesigner.boost;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;

public class DNAPolishingDialog extends JDialog implements ActionListener{

	private Component parent;
	private String filePath;
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	JComboBox<String> strategyComboBox = new JComboBox<>(new String[] {"Random", "Balenced",
			"Least Different", "Mostly Used", "Balenced2Random", "Relexed Weight"});
	
	JComboBox<String> vendorComboBox = new JComboBox<>(new String[] {" Thermo Fisher (Life Technalogies)", 
	        " SGI-DNA"," GEN9", " DOE Joint Genome Institute (JGI)", " IDT"});
	JComboBox<String> annotationComboBox = new JComboBox<>(new String[] {"Yes", "No"});
	JComboBox<String> predefinedComboBox = new JComboBox<>(new String[] {"Bacillus subtilis",
		    "Arabidapsis thaliana", "Escherichia coli", "Saccharomyces cerevisiae"});
	
	public DNAPolishingDialog(Component parent, String filePath) {
		super(JOptionPane.getFrameForComponent(parent), "DNA Modification", true);
		this.parent = parent;
		this.filePath = filePath;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);
		submitButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel mainPanel = new JPanel();
		uiMainPanel(mainPanel);
		JLabel infoLabel = new JLabel("Please make choice for given parameters");
		infoLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
	
		Container contentPane = getContentPane();
		DialogUtils.setUI(contentPane, infoLabel, mainPanel, buttonPane);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
		
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}else if(e.getSource() == submitButton) {
			setVisible(false);
			int strategyIndex = strategyComboBox.getSelectedIndex();
			int vendorIndex = vendorComboBox.getSelectedIndex();
			int annotationIndex = annotationComboBox.getSelectedIndex();
			String host =String.valueOf(predefinedComboBox.getSelectedItem());
			System.out.println(host);
			BOOSTOperations.polishing(filePath, EnumInArrayList.annotation[annotationIndex],
					EnumInArrayList.vendorList.get(vendorIndex), 
					EnumInArrayList.strategyList.get(strategyIndex), host);
			return;
		}		
	}
	
	
	private void uiMainPanel(JPanel mainPanel) {
        JLabel strategyLabel = new JLabel("Select Codon Selection Strategy:");
        JLabel vendorLabel = new JLabel("Select Vendor of your Choice:");
        JLabel predefinedLabel = new JLabel("Predefined Codon Uses table:");
        JLabel annotationLabel = new JLabel("Encoding Sequence support annotation feature:");
		
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
}