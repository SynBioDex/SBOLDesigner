package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

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
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SearchCriteria;
import org.synbiohub.frontend.SearchQuery;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.CharSequences;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.SynBioHubFrontends;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

public class UploadNewDialog extends JDialog implements ActionListener, DocumentListener {
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
			"Overwrite Submission", "Merge and Prevent if member of collection exists",
			"Merge and Replace if member of collection exists" });
	private final JTextField submissionId = new JTextField("");
	private final JTextField version = new JTextField("");
	private final JTextField name = new JTextField("");
	private final JTextField description = new JTextField("");
	private final JTextField citations = new JTextField("");

	public UploadNewDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;

		// Remove objects that should already be found in this registry
		// for (TopLevel topLevel : this.toBeUploaded.getTopLevels()) {
		// String identity = topLevel.getIdentity().toString();
		// String registryPrefix = registry.getUriPrefix();
		// if ((!registryPrefix.equals("") &&
		// identity.startsWith(registryPrefix))
		// || (registryPrefix.equals("") &&
		// identity.startsWith(registry.getLocation()))) {
		// try {
		// this.toBeUploaded.removeTopLevel(topLevel);
		// } catch (SBOLValidationException e) {
		// e.printStackTrace();
		// }
		// }
		// }

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

		enableUpload();

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private JPanel initMainPanel() {
		submissionId.getDocument().addDocumentListener(this);
		version.getDocument().addDocumentListener(this);
		name.getDocument().addDocumentListener(this);
		description.getDocument().addDocumentListener(this);
		citations.getDocument().addDocumentListener(this);

		FormBuilder builder = new FormBuilder();
		builder.add("", new JLabel(" "));
		builder.add("Submission ID *", submissionId);
		builder.add("Version *", version);
		builder.add("Name *", name);
		builder.add("Description *", description);
		builder.add("Options (if existing)", options);
		JPanel panel = builder.build();
		panel.setAlignmentX(LEFT_ALIGNMENT);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(panel);

		return mainPanel;
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
				setVisible(false);
				return;
			} catch (SynBioHubException e1) {
				MessageDialog.showMessage(parent, "Uploading failed", Arrays.asList(e1.getMessage().split("\"|,")));
				toBeUploaded.clearRegistries();
			}
		}
	}

	private void uploadDesign() throws SynBioHubException {
		SynBioHubFrontends frontends = new SynBioHubFrontends();
		if (!frontends.hasFrontend(registry.getLocation())) {
			JOptionPane.showMessageDialog(parent,
					"Please login to " + registry.getLocation() + " in the Registry preferences menu.");
			return;
		}
		SynBioHubFrontend frontend = frontends.getFrontend(registry.getLocation());

		String option = Integer.toString(options.getSelectedIndex());
		frontend.submit(submissionId.getText(), version.getText(), name.getText(), description.getText(),
				citations.getText(), "", option, toBeUploaded);
		JOptionPane.showMessageDialog(parent, "Upload successful!");
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
				&& !name.getText().equals("") && !description.getText().equals("");
		uploadButton.setEnabled(shouldEnable);
	}
}
