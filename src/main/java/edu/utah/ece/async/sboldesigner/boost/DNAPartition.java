package edu.utah.ece.async.sboldesigner.boost;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.sbolstandard.core2.SBOLDocument;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;

public class DNAPartition extends JDialog implements ActionListener{
    
	private Component parent;
	private SBOLDocument currentDesign;
	
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	
	private final JPanel contentPanel = new JPanel();
	private JCheckBox fivePrimeLabel = new JCheckBox("Enter 5-prime-vector overlap");
	private JCheckBox threePrimeLabel = new JCheckBox("Enter 3-prime-vector overlap ");
	
	private JLabel buildingBlockLabel = new JLabel("Enter values of Building Block Length [bp]:");
	private JLabel minBuildingBlockLabel = new JLabel("Min Len");
	private JLabel maxBuildingBlockLabel = new JLabel("Max Len:");
	
	private JLabel overlapSequenceLabel = new JLabel("  Enter sequence Overlap Length [bp]");
	private JLabel minSeqOverlapLabel = new JLabel("Min Len");
	private JLabel optSeqOverlapLabel = new JLabel("Opt Len");
	private JLabel maxSeqOverlapLabel = new JLabel("Max Len");
	
	private JLabel gdOverlapLabel = new JLabel(" Enter Overlap GC [%] ");
	private JLabel minGCLabel = new JLabel("Min Overlap");
	private JLabel optGCLabel = new JLabel("Opt Overlap");
	private JLabel maxGCLabel = new JLabel("Max Overlap");
	
	private JLabel primerLabel = new JLabel(" Enter Primer Length:");
	private JLabel minPrimerLenLabel = new JLabel("Min Len");
	private JLabel optPrimerLenLabel = new JLabel("Opt Len");
	private JLabel maxPrimerLenLabel = new JLabel("Max Len");
	
	
	private JTextField textFieldFivePrimeVecor;
	private JTextField textFieldThreePrimeVecor;
	private JTextField textFieldMinBuildingLen;
	private JTextField textFieldMaxBuildingLen;
	private JTextField textFieldMinSeqOverlap;
	private JTextField textFieldOptSeqOverlap;
	private JTextField textFieldMaxSeqOverlap;
	private JTextField textFieldMinGC;
	private JTextField textFieldOptGC;
	private JTextField textFieldMaxGC;
	private JTextField textFieldMinPrimerLen;
	private JTextField textFieldOptPrimerLen;
	private JTextField textFieldMaxPrimerLen;

	public DNAPartition(Component parent, SBOLDocument currentDesign) {
		super(JOptionPane.getFrameForComponent(parent), "Codon Juggling", true);
		this.parent = parent;
		this.currentDesign = currentDesign;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);
		submitButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		mainPanelUI();
		pack();
		setVisible(true);
	}


	/**
	 * Create the dialog.
	 */
	public void mainPanelUI() {
		setBounds(100, 100, 678, 492);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		JLabel mTextLabel = new JLabel("Please provide the appropiate parameters for "
				+ "partition of your DNA sequence:");
	
		
		textFieldFivePrimeVecor = new JTextField();
		textFieldFivePrimeVecor.setColumns(10);
		
		textFieldThreePrimeVecor = new JTextField();
		textFieldThreePrimeVecor.setColumns(10);
	
		
		textFieldMinBuildingLen = new JTextField();
		textFieldMinBuildingLen.setColumns(10);
		
		textFieldMaxBuildingLen = new JTextField();
		textFieldMaxBuildingLen.setColumns(10);
		
		textFieldMinSeqOverlap = new JTextField();
		textFieldMinSeqOverlap.setColumns(10);
		
		textFieldOptSeqOverlap = new JTextField();
		textFieldOptSeqOverlap.setColumns(10);
		
		textFieldMaxSeqOverlap = new JTextField();
		textFieldMaxSeqOverlap.setColumns(10);
		
		textFieldMinGC = new JTextField();
		textFieldMinGC.setColumns(10);
		
		textFieldOptGC = new JTextField();
		textFieldOptGC.setColumns(10);
		
		textFieldMaxGC = new JTextField();
		textFieldMaxGC.setColumns(10);

		
		textFieldMinPrimerLen = new JTextField();
		textFieldMinPrimerLen.setColumns(10);
		
		textFieldOptPrimerLen = new JTextField();
		textFieldOptPrimerLen.setColumns(10);
		
		textFieldMaxPrimerLen = new JTextField();
		textFieldMaxPrimerLen.setColumns(10);
		GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
		contentPanelLayout.setHorizontalGroup(
			contentPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(contentPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(mTextLabel)
						.addGroup(contentPanelLayout.createSequentialGroup()
							.addGroup(contentPanelLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(buildingBlockLabel)
								.addComponent(fivePrimeLabel)
								.addComponent(threePrimeLabel)
								.addComponent(overlapSequenceLabel)
								.addComponent(gdOverlapLabel)
								.addComponent(primerLabel))
							.addGap(43)
							.addGroup(contentPanelLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(textFieldFivePrimeVecor, Alignment.LEADING)
								.addComponent(textFieldThreePrimeVecor, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
								.addGroup(contentPanelLayout.createSequentialGroup()
									.addGroup(contentPanelLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(textFieldMinBuildingLen, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
										.addComponent(minBuildingBlockLabel)
										.addComponent(minSeqOverlapLabel)
										.addComponent(textFieldMinSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(minGCLabel)
										.addComponent(textFieldMinGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(minPrimerLenLabel)
										.addComponent(textFieldMinPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
									.addGroup(contentPanelLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(textFieldOptPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(optPrimerLenLabel)
										.addComponent(textFieldOptGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(optGCLabel)
										.addComponent(textFieldOptSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(optSeqOverlapLabel)
										.addComponent(maxBuildingBlockLabel)
										.addComponent(textFieldMaxBuildingLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))))
					.addPreferredGap(ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(maxSeqOverlapLabel)
						.addComponent(textFieldMaxSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(maxGCLabel)
						.addComponent(textFieldMaxGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(maxPrimerLenLabel)
						.addComponent(textFieldMaxPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(41))
		);
		contentPanelLayout.setVerticalGroup(
			contentPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(contentPanelLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(mTextLabel)
					.addGap(18)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(fivePrimeLabel)
						.addComponent(textFieldFivePrimeVecor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(threePrimeLabel)
						.addComponent(textFieldThreePrimeVecor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(buildingBlockLabel)
						.addComponent(minBuildingBlockLabel)
						.addComponent(maxBuildingBlockLabel))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldMinBuildingLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldMaxBuildingLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(minSeqOverlapLabel)
						.addComponent(optSeqOverlapLabel)
						.addComponent(maxSeqOverlapLabel)
						.addComponent(overlapSequenceLabel))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldMinSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldOptSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldMaxSeqOverlap, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(minGCLabel)
						.addComponent(optGCLabel)
						.addComponent(maxGCLabel)
						.addComponent(gdOverlapLabel, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldMinGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldOptGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldMaxGC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(minPrimerLenLabel)
						.addComponent(optPrimerLenLabel)
						.addComponent(maxPrimerLenLabel)
						.addComponent(primerLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(contentPanelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldMinPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldOptPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(textFieldMaxPrimerLen, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(66, Short.MAX_VALUE))
		);
		contentPanel.setLayout(contentPanelLayout);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("cancel");
				buttonPane.add(cancelButton);
				getRootPane().setDefaultButton(cancelButton);
			}
			{
				JButton submitButton = new JButton("Submit");
				submitButton.setActionCommand("submit");
				buttonPane.add(submitButton);
			}
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
