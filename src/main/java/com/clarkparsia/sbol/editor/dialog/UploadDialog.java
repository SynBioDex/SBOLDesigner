package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SearchCriteria;
import org.synbiohub.frontend.SearchQuery;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.SBOLEditorPreferences;
import com.clarkparsia.swing.FormBuilder;
import com.clarkparsia.versioning.PersonInfo;

public class UploadDialog extends JDialog implements ActionListener, DocumentListener, MouseListener {
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

	private final JLabel info = new JLabel(
			"Submission form for uploading to SynBioHub.  The options specify what actions to take for duplicate designs.  (*) indicates a required field");
	private final JButton uploadButton = new JButton("Upload");
	private final JButton cancelButton = new JButton("Cancel");
	private final JComboBox<String> options = new JComboBox<>(new String[] { "Prevent Submission",
			"Overwrite Submission", "Merge and Prevent, if existing", "Merge and Replace, if existing" });
	private final JTextField username = new JTextField("");
	private final JPasswordField password = new JPasswordField("");
	private final JTextField submissionId = new JTextField("");
	private final JTextField version = new JTextField("");
	private final JTextField name = new JTextField("");
	private final JTextField description = new JTextField("");
	private final JTextField citations = new JTextField("");
	private final JTextField collections = new JTextField("");

	public UploadDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;

		// Remove objects that should already be found in this registry
		for (TopLevel topLevel : this.toBeUploaded.getTopLevels()) {
			String identity = topLevel.getIdentity().toString();
			String registryPrefix = registry.getUriPrefix();
			if ((!registryPrefix.equals("") && identity.startsWith(registryPrefix))
					|| (registryPrefix.equals("") && identity.startsWith(registry.getLocation()))) {
				try {
					this.toBeUploaded.removeTopLevel(topLevel);
				} catch (SBOLValidationException e) {
					e.printStackTrace();
				}
			}
		}

		// set default values
		PersonInfo userInfo = SBOLEditorPreferences.INSTANCE.getUserInfo();
		String email = userInfo == null || userInfo.getEmail() == null ? null : userInfo.getEmail().getLocalName();
		String uri = userInfo == null ? null : userInfo.getURI().stringValue();
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
		contentPane.add(info, BorderLayout.PAGE_START);
		contentPane.add(panel, BorderLayout.CENTER);
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
		collections.getDocument().addDocumentListener(this);
		collections.addMouseListener(this);

		FormBuilder builder = new FormBuilder();
		builder.add("Username *", username);
		builder.add("Password *", password);
		builder.add("", new JLabel(" "));
		builder.add("Submission ID *", submissionId);
		builder.add("Version *", version);
		builder.add("Name *", name);
		builder.add("Description *", description);
		builder.add("Citations", citations);
		builder.add("Collections", collections);
		builder.add("Options", options);
		JPanel panel = builder.build();
		panel.setAlignmentX(LEFT_ALIGNMENT);
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
			} catch (SynBioHubException e1) {
				MessageDialog.showMessage(parent, "Uploading failed", Arrays.asList(e1.getMessage().split("\"|,")));
				toBeUploaded.clearRegistries();
			}
		}
	}

	private void uploadDesign() throws SynBioHubException {
		SynBioHubFrontend stack = toBeUploaded.addRegistry(registry.getLocation(), registry.getUriPrefix());

		stack.login(username.getText(), new String(password.getPassword()));

		String option = Integer.toString(options.getSelectedIndex());

		stack.submit(submissionId.getText(), version.getText(), name.getText(), description.getText(),
				citations.getText(), collections.getText(), option, toBeUploaded);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		enableUpload();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		enableUpload();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		enableUpload();
	}

	private void enableUpload() {
		boolean shouldEnable = !submissionId.getText().equals("") && !version.getText().equals("")
				&& !name.getText().equals("") && !description.getText().equals("") && !username.getText().equals("")
				&& !password.getPassword().equals("");
		uploadButton.setEnabled(shouldEnable);
	}

	/*
	 * All mouse methods are for collections and collection selection
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		SynBioHubFrontend stack = toBeUploaded.addRegistry(registry.getLocation(), registry.getUriPrefix());

		try {
			stack.login(username.getText(), new String(password.getPassword()));
		} catch (SynBioHubException e1) {
			JOptionPane.showMessageDialog(parent,
					"Collection selection requires a valid username and password to be entered",
					"Collection selection failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			SearchQuery query = new SearchQuery();
			SearchCriteria crit = new SearchCriteria();
			// TODO what value to get all collections?
			crit.setKey("collection");
			crit.setValue("");
			query.addCriteria(crit);
			List<IdentifiedMetadata> results = stack.search(query);

			if (results.size() == 0) {
				return;
			}

			List<String> uris = new ArrayList<String>();
			results.forEach((metadata) -> uris.add(metadata.getUri()));

			Object[] options = uris.toArray();

			int result = JOptionPane.showOptionDialog(parent, "Select a collection", "Collection selection",
					JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

			if (result != JOptionPane.CLOSED_OPTION) {
				String text = collections.getText();

				if (text.equals("")) {
					text = (String) options[result];
				} else {
					text = text + "," + (String) options[result];
				}

				collections.setText(text);
			}
		} catch (SynBioHubException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
