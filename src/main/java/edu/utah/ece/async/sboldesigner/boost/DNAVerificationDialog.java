package edu.utah.ece.async.sboldesigner.boost;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.sbolstandard.core2.SBOLDocument;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;
import gov.doe.jgi.boost.client.constants.BOOSTConstantsArrayList;
import gov.doe.jgi.boost.client.utils.FileUtils;

import javax.swing.JComboBox;

public class DNAVerificationDialog extends JDialog implements ActionListener {
	
	public String dnaVerificationJobUUID = null;
	private Component parent;
	private SBOLDocument currentDesign;
	private String sequencePatternsFilename = null;
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	JButton chooseFileButton = new JButton("Choose File");
	JComboBox<String> vendorComboBox = new JComboBox<>(new String[] {" Thermo Fisher (Life Technalogies)", 
	        " SGI-DNA"," GEN9", " DOE Joint Genome Institute (JGI)", " IDT"});
	
	public DNAVerificationDialog(Component parent, SBOLDocument currentDesign) {
		super(JOptionPane.getFrameForComponent(parent), "DNA Verification", true);
		this.currentDesign = currentDesign;
		this.parent = parent;

		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);
		submitButton.addActionListener(this);
		chooseFileButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);

		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
		
		JPanel mainPanel = new JPanel();
		uiDNAVerifivation(mainPanel);
		
		JLabel infoLabel = new JLabel("Please make choice for given parameters");
		
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
			new AvailableOperationsDialog(parent, currentDesign);
			return;
		} else if (e.getSource() == chooseFileButton) {
			this.sequencePatternsFilename = FileUtils.SelectedFilePath("sequencePatterns");
			System.out.println(sequencePatternsFilename);
			
		}
		if (e.getSource() == submitButton) {
			setVisible(false);
			int vendorIndex = vendorComboBox.getSelectedIndex();
			if (this.sequencePatternsFilename != null && !this.sequencePatternsFilename.trim().isEmpty()) {
				setVisible(false);
				this.dnaVerificationJobUUID = BOOSTOperations.dnaVerification(currentDesign, BOOSTConstantsArrayList.vendorList.get(vendorIndex),
						sequencePatternsFilename);
				return;
			} else {
				this.dnaVerificationJobUUID = BOOSTOperations.dnaVerification(currentDesign, BOOSTConstantsArrayList.vendorList.get(vendorIndex),
						null);
			}
		}
	}
	
	protected void uiDNAVerifivation(JPanel mainPanel) {
		
		JLabel vendorLabel = new JLabel("Select vendor of your choice (Synthesis Constraints):");
		JLabel uploadFileLabel = new JLabel("Upload a file for Sequence Patterns:");
		
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanelLayout.setHorizontalGroup(
			mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addGap(30)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(vendorLabel)
						.addComponent(uploadFileLabel))
					.addGap(73)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(chooseFileButton)
						.addComponent(vendorComboBox, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(53, Short.MAX_VALUE))
		);
		mainPanelLayout.setVerticalGroup(
			mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
					.addGap(28)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(vendorLabel)
						.addComponent(vendorComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(35)
					.addGroup(mainPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(uploadFileLabel)
						.addComponent(chooseFileButton))
					.addContainerGap(39, Short.MAX_VALUE))
		);
		mainPanel.setLayout(mainPanelLayout);
	}
}
