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
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
import edu.utah.ece.async.sboldesigner.versioning.PersonInfo;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

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

	private final JLabel info = new JLabel(
			"Submission form for uploading to SynBioHub.  The options specify what actions to take for duplicate designs.  (*) indicates a required field");
	private final JButton uploadButton = new JButton("Upload");
	private final JButton cancelButton = new JButton("Cancel");
	private final JComboBox<String> options = new JComboBox<>(new String[] { "Prevent Submission",
			"Overwrite Submission", "Merge and Prevent if member of collection exists",
			"Merge and Replace if member of collection exists" });
	private final JTextField username = new JTextField("");
	private final JPasswordField password = new JPasswordField("");
	private final JTextField submissionId = new JTextField("");
	private final JTextField version = new JTextField("");
	private final JTextField name = new JTextField("");
	private final JTextField description = new JTextField("");
	private final JTextField citations = new JTextField("");
	private JList<IdentifiedMetadata> collections = null;

	public UploadDialog(final Component parent, Registry registry, SBOLDocument toBeUploaded) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;

		// Remove objects that should already be found in this registry
//		for (TopLevel topLevel : this.toBeUploaded.getTopLevels()) {
//			String identity = topLevel.getIdentity().toString();
//			String registryPrefix = registry.getUriPrefix();
//			if ((!registryPrefix.equals("") && identity.startsWith(registryPrefix))
//					|| (registryPrefix.equals("") && identity.startsWith(registry.getLocation()))) {
//				try {
//					this.toBeUploaded.removeTopLevel(topLevel);
//				} catch (SBOLValidationException e) {
//					e.printStackTrace();
//				}
//			}
//		}

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

		FormBuilder builder = new FormBuilder();
		builder.add("Username *", username);
		builder.add("Password *", password);
		builder.add("", new JLabel(" "));
		builder.add("Submission ID *", submissionId);
		builder.add("Version *", version);
		builder.add("Name *", name);
		builder.add("Description *", description);
		builder.add("Options (if existing)", options);
		JPanel panel = builder.build();
		panel.setAlignmentX(LEFT_ALIGNMENT);

		// setup collections
		collections = new JList<IdentifiedMetadata>(setupListModel());
		collections.setCellRenderer(new MyListCellRenderer());
		collections.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		collections.setLayoutOrientation(JList.VERTICAL);
		collections.setVisibleRowCount(5);
		JScrollPane collectionsScroller = new JScrollPane(collections);
		collectionsScroller.setPreferredSize(new Dimension(50, 200));
		collectionsScroller.setAlignmentX(LEFT_ALIGNMENT);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(panel);
		mainPanel.add(new JLabel("Collections:"));
		mainPanel.add(collectionsScroller);

		return mainPanel;
	}

	private class MyListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setOpaque(isSelected); // Highlight only when selected
			label.setText(((IdentifiedMetadata) value).getName());
			return label;
		}
	}

	private ListModel<IdentifiedMetadata> setupListModel() {
		DefaultListModel<IdentifiedMetadata> model = new DefaultListModel<IdentifiedMetadata>();

		SynBioHubFrontend stack = toBeUploaded.addRegistry(registry.getLocation(), registry.getUriPrefix());
		SearchQuery query = new SearchQuery();
		SearchCriteria crit = new SearchCriteria();
		crit.setKey("objectType");
		crit.setValue("Collection");
		query.addCriteria(crit);
		query.setLimit(10000);
		query.setOffset(0);
		List<IdentifiedMetadata> results;
		try {
			results = stack.search(query);
		} catch (SynBioHubException e) {
			return model;
		}

		if (results.size() == 0) {
			return model;
		}

		for (IdentifiedMetadata collection : results) {
			model.addElement(collection);
		}
		return model;
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
				citations.getText(), getSelectedCollections(collections), option, toBeUploaded);
	}

	private String getSelectedCollections(JList<IdentifiedMetadata> col) {
		StringBuilder result = new StringBuilder();
		for (IdentifiedMetadata collection : col.getSelectedValuesList()) {
			result.append(collection.getUri());
			result.append(",");
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
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
}
