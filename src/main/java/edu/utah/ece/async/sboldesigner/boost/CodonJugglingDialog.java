package edu.utah.ece.async.sboldesigner.boost;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;
import gov.doe.jgi.boost.client.constants.BOOSTConstantsArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.sbolstandard.core2.SBOLDocument;

public class CodonJugglingDialog extends JDialog implements ActionListener{
	
	private Component parent;
	private SBOLDocument currentDesign;
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	JComboBox<String> strategyComboBox = new JComboBox<>(new String[] {"Random", 
			"Balanced", "Mostly Used", "Least Different"});
    JComboBox<String> annotationComboBox = new JComboBox<>(new String[] {"Yes", "No"});
    JComboBox<String> hostComboBox = new JComboBox<>(new String[] {"Bacillus subtilis",
		    "Arabidapsis thaliana", "Escherichia coli", "Saccharomyces cerevisiae"});


	public CodonJugglingDialog(Component parent, SBOLDocument currentDesign) {
		super(JOptionPane.getFrameForComponent(parent), "Codon Juggling", true);
		this.parent = parent;
		this.currentDesign = currentDesign;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);
		submitButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
			
		JPanel mainPanel = new JPanel();
		mainPanelUI(mainPanel);

		JLabel infoLabel = new JLabel("  Please make choice for given parameters");
		infoLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		
		Container contentPane = getContentPane();
		DialogUtils.setUI(contentPane, infoLabel, mainPanel, buttonPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);	
	}


	@Override
	public void actionPerformed(ActionEvent arg) {
		if (arg.getSource() == cancelButton) {
			setVisible(false);
			new AvailableOperationsDialog(parent, currentDesign);
			return;
		}else if(arg.getSource() == submitButton) {
			setVisible(false);
			int strategyIndex = strategyComboBox.getSelectedIndex();
			int annotationIndex = annotationComboBox.getSelectedIndex();
			String host =String.valueOf(hostComboBox.getSelectedItem());
			System.out.println(host);
			BOOSTOperations.codonJuggling(currentDesign,BOOSTConstantsArrayList.annotation[annotationIndex],
					BOOSTConstantsArrayList.strategyList.get(strategyIndex), host);
			return;
		}	
	}
	
	void mainPanelUI(JPanel mainPanel) {
		JLabel strategyLabel = new JLabel("Select Codon Selection Strategy:");
		JLabel selectCodonLabel = new JLabel("Select Predefined Codon Usage Table:");
		JLabel annotationLabel = new JLabel("exclusively 5'-3' coding sequences?");
		
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanelLayout.setHorizontalGroup(
			mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addGap(27)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(strategyLabel)
						.addComponent(selectCodonLabel)
						.addComponent(annotationLabel))
					.addGap(50)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(annotationComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(hostComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(strategyComboBox, 0, 177, Short.MAX_VALUE))
					.addGap(34))
		);
		mainPanelLayout.setVerticalGroup(
			mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addGap(30)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(strategyLabel)
						.addComponent(strategyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(29)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(selectCodonLabel)
						.addComponent(hostComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(30)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(annotationLabel)
						.addComponent(annotationComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(53, Short.MAX_VALUE))
		);
		mainPanel.setLayout(mainPanelLayout);
	}
}
