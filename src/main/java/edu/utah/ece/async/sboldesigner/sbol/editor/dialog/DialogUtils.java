package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

public class DialogUtils {
	
	private static JTextField username;
	private static JTextField password;
	
	
	public DialogUtils(JTextField username, JPasswordField password) {
		DialogUtils.username = username;
		DialogUtils.password = password;
	}

	public static void setUserInfo() {
		PersonInfo userInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
		String email = userInfo == null || userInfo.getEmail() == null ? null : userInfo.getEmail().getLocalName();
		username.setText(email);
		((JPasswordField) password).setEchoChar('*');	
	}
	
	public static FormBuilder initBuilder() {
		FormBuilder builder = new FormBuilder();
		builder.add("Username", username);
		builder.add("Password", password);
		return builder;
	}
	

	public static void setUI(Container contentPane, JLabel infoLabel, JPanel mainPanel, JPanel buttonPane) {
		contentPane.add(infoLabel, BorderLayout.PAGE_START);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
	}
	
	public static JPanel buildDecisionArea(int alongLineAxis) {
		int itemsAlongLineAxis = alongLineAxis;
		JPanel buttonPane = new JPanel();
		switch(itemsAlongLineAxis) {
		case 0:
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			break;
			
		case 1:
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
			break;	
		}
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		return buttonPane;
	}
}
