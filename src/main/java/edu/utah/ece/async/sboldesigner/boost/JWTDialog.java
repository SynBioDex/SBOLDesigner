package edu.utah.ece.async.sboldesigner.boost;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.DialogUtils;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public class JWTDialog extends JDialog implements ActionListener{
	
	private Component parent;
	
	private final JTextField token = new JTextField("");
	private final JButton submitButton = new JButton("Submit");
	private final JButton cancelButton = new JButton("Cancel");
	
	public JWTDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), "Authentication by BOOST JWT Token", true);
		this.parent = parent;
		
		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		submitButton.addActionListener(this);
		cancelButton.addActionListener(this);
		getRootPane().setDefaultButton(submitButton);
		
		JPanel buttonPane = DialogUtils.buildDecisionArea(0); // 0 for LINE_AXIS alignment 
		buttonPane.add(cancelButton);
		buttonPane.add(submitButton);
	
		FormBuilder builder = new FormBuilder();
		builder.add("JWT Token: ", token);
		
		JPanel mainPanel = builder.build();
		mainPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel infoLabel = new JLabel(
				"Please provide your BOOST JWT token in order to authenticate your account");
		
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
			String mJWT = token.getText();
			//TODO: Pass this token for BOOST API Call
		}	
	}
}
