package edu.utah.ece.async.sboldesigner.boost;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;

public class DNAVerificationResultDialog extends JDialog implements ActionListener {
	
	private Component parent;
	private JPanel mainPanel = new JPanel();
	private final JButton cancelButton = new JButton("Cancel");
	private final JButton okButton = new JButton("Ok");
	
	public DNAVerificationResultDialog(Component parent, JSONObject jobReport) throws JsonParseException, JsonMappingException, IOException {
		super(JOptionPane.getFrameForComponent(parent), "DNA Verification Violations", true);
		this.parent = parent;
		
		if(jobReport.length() == 0) {
			String noViolationMsg = "This design sequence does not have any violation for the selected Vendor";
			JOptionPane.showMessageDialog(parent, noViolationMsg);
			
		}else {
			
			cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			        JComponent.WHEN_IN_FOCUSED_WINDOW);
	        cancelButton.addActionListener(this);
	        okButton.addActionListener(this);
	        getRootPane().setDefaultButton(okButton);
	        
			JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment
			buttonPane.add(cancelButton);
			buttonPane.add(okButton);
			
		    JTextArea textArea = new JTextArea (16, 58);
		    textArea.setEditable ( false ); // set textArea non-editable
		    JScrollPane scroll = new JScrollPane (textArea);
		    scroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		    mainPanel.add (scroll);  //add textArea in to main panel
			mainPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			JLabel infoLabel = new JLabel("The Codon sequence violates for the following parameters:");
			ObjectMapper mapper = new ObjectMapper();
			Object jsonObject = mapper.readValue(jobReport.toString(), Object.class);
			String formattedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
		    textArea.setText(formattedJson);
		    
		    Container contentPane = getContentPane();
			DialogUtils.setUI(contentPane, infoLabel, mainPanel, buttonPane);
			pack();
			setLocationRelativeTo(parent);
			setVisible(true);
		}
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}else if(e.getSource() == okButton) {
			setVisible(false);
			return;
		}
		
	}
}
