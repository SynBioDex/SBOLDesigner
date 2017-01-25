package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.sbolstack.frontend.StackException;
import org.sbolstack.frontend.StackFrontend;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.swing.FormBuilder;
import com.clarkparsia.versioning.PersonInfo;

public class UploadDialog extends JDialog implements ActionListener, DocumentListener {
	private static final String TITLE = "Upload Design: ";

	private static String title(Registry registry) {
		String title = "";
		if (registry.getName() != null) {
			title = title + registry.getName();
		} else if (registry.getLocation() != null) {
			title = title + registry.getLocation();
		}
		return CharSequences.shorten(title, 20).toString();
	}

	private Component parent;
	private Registry registry;
	private SBOLDocument toBeUploaded;

	// TODO add infos
	private final JLabel whatDoesThisDialogDo = new JLabel("TODO");
	private final JLabel whatDoTheOptionsMean = new JLabel("TODO");
	private final JButton uploadButton = new JButton("Upload");
	private final JButton cancelButton = new JButton("Cancel");
	// TODO these descriptions should change
	private final JRadioButton prevent = new JRadioButton("Prevent if submission exists");
	private final JRadioButton overwrite = new JRadioButton("Overwrite entire submission");
	private final JRadioButton mergePrevent = new JRadioButton("Merge and prevent if existing objects");
	private final JRadioButton mergeReplace = new JRadioButton("Merge and replace if existing objects");
	private final JTextField username = new JTextField("");
	private final JPasswordField password = new JPasswordField("");
	private final JTextField submissionId = new JTextField("");
	private final JTextField version = new JTextField("");
	private final JTextField name = new JTextField("");
	private final JTextField description = new JTextField("");
	private final JTextField citations = new JTextField("");
	private final JTextField keywords = new JTextField("");

	public UploadDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;

		// set default values
		PersonInfo info = SBOLEditorPreferences.INSTANCE.getUserInfo();
		String email = info == null || info.getEmail() == null ? null : info.getEmail().getLocalName();
		String uri = info == null ? null : info.getURI().stringValue();
		if (email == null || email.equals("") || uri == null) {
			JOptionPane.showMessageDialog(parent,
					"Make sure your email and URI/namespace are both set and valid in preferences.", "Upload failed",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		username.setText(email);
		password.setEchoChar('*');
		ComponentDefinition root = toBeUploaded.getRootComponentDefinitions().iterator().next();
		submissionId.setText(root.getDisplayId());
		version.setText("1");
		name.setText(root.isSetName() ? root.getName() : root.getDisplayId());
		description.setText(root.isSetDescription() ? root.getDescription() : "");

		// layout
		JPanel optionPanel = new JPanel();
		ButtonGroup options = new ButtonGroup();
		optionPanel.add(prevent);
		optionPanel.add(overwrite);
		optionPanel.add(mergePrevent);
		optionPanel.add(mergeReplace);
		options.add(prevent);
		options.add(overwrite);
		options.add(mergePrevent);
		options.add(mergeReplace);
		prevent.setSelected(true);

		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		uploadButton.addActionListener(this);
		uploadButton.setEnabled(false);
		getRootPane().setDefaultButton(uploadButton);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(uploadButton);

		JPanel panel = initMainPanel();

		Container contentPane = getContentPane();
		contentPane.add(panel, BorderLayout.PAGE_START);
		contentPane.add(optionPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel initMainPanel() {
		username.getDocument().addDocumentListener(this);
		password.getDocument().addDocumentListener(this);
		submissionId.getDocument().addDocumentListener(this);
		version.getDocument().addDocumentListener(this);
		name.getDocument().addDocumentListener(this);
		description.getDocument().addDocumentListener(this);
		citations.getDocument().addDocumentListener(this);
		keywords.getDocument().addDocumentListener(this);

		FormBuilder builder = new FormBuilder();
		builder.add("Username", username);
		builder.add("Password", password);
		builder.add("", new JLabel(" "));
		builder.add("Submission ID", submissionId);
		builder.add("Version", version);
		builder.add("Name", name);
		builder.add("Description", description);
		builder.add("Citations", citations);
		builder.add("Keywords", keywords);
		JPanel panel = builder.build();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}

		if (e.getSource() == uploadButton) {
			try {
				uploadDesign();
				JOptionPane.showMessageDialog(parent, "Upload successful!");
				setVisible(false);
				return;
			} catch (StackException e1) {
				MessageDialog.showMessage(parent, "Uploading failed", Arrays.asList(e1.getMessage().split("\"|,")));
				toBeUploaded.clearRegistries();
				e1.printStackTrace();
			}
		}
	}

	private void uploadDesign() throws StackException {
		StackFrontend stack = toBeUploaded.addRegistry(registry.getLocation());

		stack.login(username.getText(), new String(password.getPassword()));

		String option;
		if (prevent.isSelected()) {
			option = "0";
		} else if (overwrite.isSelected()) {
			option = "1";
		} else if (mergePrevent.isSelected()) {
			option = "2";
		} else if (mergeReplace.isSelected()) {
			// TODO replace with 3 when stack frontend is updated
			option = "2";
		} else {
			// default
			option = "0";
		}

		stack.submit(submissionId.getText(), version.getText(), name.getText(), description.getText(),
				citations.getText(), keywords.getText(), option, toBeUploaded);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		uploadButton.setEnabled(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		uploadButton.setEnabled(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		uploadButton.setEnabled(true);
	}

}
