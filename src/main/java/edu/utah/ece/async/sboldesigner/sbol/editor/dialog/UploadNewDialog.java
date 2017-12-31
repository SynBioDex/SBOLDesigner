package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.CharSequenceUtil;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.SynBioHubFrontends;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

public class UploadNewDialog extends JDialog implements ActionListener, DocumentListener {
	private static final String TITLE = "Upload Design: ";

	private static String title(Registry registry) {
		String title = "";
		if (registry.getName() != null) {
			title = title + registry.getName();
		} else if (registry.getLocation() != null) {
			title = title + registry.getLocation();
		}
		return CharSequenceUtil.shorten(title, 20).toString();
	}

	private Component parent;
	private Registry registry;
	private SBOLDocument toBeUploaded;
	private File toBeUploadedFile;

	private final JTextArea info = new JTextArea(
			"SynBioHub organizes your uploads into collections. You can upload as many parts as you want into one collection, and then conveniently download them all together or share the collection as a whole with other people. If you've already made a collection, try again and select the Existing Collection option. If you haven't, fill out the form below to create a new one. If you're submitting the same thing twice because you've changed things, tick the overwrite box below to overwrite previous versions with what you're uploading now. If this is the first time you have made this submission, you can ignore this. (*) indicates a required field");
	private final JButton uploadButton = new JButton("Upload");
	private final JButton cancelButton = new JButton("Cancel");
	private final JTextField submissionId = new JTextField("");
	private final JTextField version = new JTextField("");
	private final JTextField name = new JTextField("");
	private final JTextField description = new JTextField("");
	private final JTextField citations = new JTextField("");
	private final JCheckBox overwrite = new JCheckBox("");

	public UploadNewDialog(final Component parent, Registry registry, File toBeUploadedFile) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		CreateUploadNewDialog(parent, registry, null, toBeUploadedFile);
	}

	public UploadNewDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		CreateUploadNewDialog(parent, registry, toBeUploaded, null);
	}
	
	private void CreateUploadNewDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded, 
			File toBeUploadedFile) {
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;
		this.toBeUploadedFile = toBeUploadedFile;

		this.setMinimumSize(new Dimension(800, 500));

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

		info.setWrapStyleWord(true);
		info.setLineWrap(true);
		info.setEditable(false);
		info.setOpaque(false);
		info.setHighlighter(null);
		if (toBeUploaded != null) {
			ComponentDefinition root = toBeUploaded.getRootComponentDefinitions().iterator().next();
			submissionId.setText(root.getDisplayId());
			name.setText(root.isSetName() ? root.getName() : root.getDisplayId());
			description.setText(root.isSetDescription() ? root.getDescription() : "");
		}
		version.setText("1");
		overwrite.setSelected(false);

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
		builder.add("Collection ID *", submissionId);
		builder.add("Collection Version *", version);
		builder.add("Collection Name *", name);
		builder.add("Collection Description *", description);
		builder.add("Citations (optional)", citations);
		builder.add("Overwrite", overwrite);
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
			} catch (SynBioHubException | IOException e1) {
				MessageDialog.showMessage(parent, "Uploading failed", Arrays.asList(e1.getMessage().split("\"|,")));
				if (toBeUploaded!=null) {
					toBeUploaded.clearRegistries();
				}
			}
		}
	}

	private void uploadDesign() throws SynBioHubException, IOException {
		SynBioHubFrontends frontends = new SynBioHubFrontends();
		if (!frontends.hasFrontend(registry.getLocation())) {
			JOptionPane.showMessageDialog(parent,
					"Please login to " + registry.getLocation() + " in the Registry preferences menu.");
			return;
		}
		SynBioHubFrontend frontend = frontends.getFrontend(registry.getLocation());

		if (toBeUploaded != null) {
			frontend.createCollection(submissionId.getText(), version.getText(), name.getText(), description.getText(),
					citations.getText(), overwrite.isSelected(), toBeUploaded);
		} else {
			frontend.createCollection(submissionId.getText(), version.getText(), name.getText(), description.getText(),
					citations.getText(), overwrite.isSelected(), toBeUploadedFile);
		}

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
