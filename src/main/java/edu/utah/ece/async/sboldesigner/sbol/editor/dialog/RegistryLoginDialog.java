package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

public class RegistryLoginDialog extends JDialog implements ActionListener {
	private String backendUrl;
	private String uriPrefix;
	private Component parent;
	private SynBioHubFrontend frontend = null;

	private final JButton loginButton = new JButton("Login");
	private final JButton cancelButton = new JButton("Cancel");
	private final JTextField username = new JTextField("");
	private final JPasswordField password = new JPasswordField("");

	public RegistryLoginDialog(Component parent, String backendUrl, String uriPrefix) {
		super(JOptionPane.getFrameForComponent(parent), "Login to " + backendUrl, true);
		this.backendUrl = backendUrl;
		this.uriPrefix = uriPrefix;
		this.parent = parent;

		PersonInfo userInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
		String email = userInfo == null || userInfo.getEmail() == null ? null : userInfo.getEmail().getLocalName();
		username.setText(email);
		password.setEchoChar('*');

		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		loginButton.addActionListener(this);
		getRootPane().setDefaultButton(loginButton);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(loginButton);

		FormBuilder builder = new FormBuilder();
		builder.add("Username", username);
		builder.add("Password", password);
		JPanel mainPanel = builder.build();
		mainPanel.setAlignmentX(LEFT_ALIGNMENT);

		JLabel infoLabel = new JLabel(
				"Login to SynBioHub account.  This enables you to upload parts and access your private constructs.");

		Container contentPane = getContentPane();
		contentPane.add(infoLabel, BorderLayout.PAGE_START);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public SynBioHubFrontend getSynBioHubFrontend() {
		return frontend;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}

		if (e.getSource() == loginButton) {
			frontend = new SynBioHubFrontend(backendUrl, uriPrefix);
			try {
				frontend.login(username.getText(), new String(password.getPassword()));
				JOptionPane.showMessageDialog(parent, "Login successful!");
			} catch (SynBioHubException e1) {
				MessageDialog.showMessage(parent, "Login failed", Arrays.asList(e1.getMessage().split("\"|,")));
				frontend = null;
				e1.printStackTrace();
			}
			setVisible(false);
			return;
		}
	}
}
