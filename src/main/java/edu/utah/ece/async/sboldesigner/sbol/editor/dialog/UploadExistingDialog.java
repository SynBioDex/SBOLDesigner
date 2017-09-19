package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.synbiohub.frontend.IdentifiedMetadata;
import org.synbiohub.frontend.SearchCriteria;
import org.synbiohub.frontend.SearchQuery;
import org.synbiohub.frontend.SynBioHubException;
import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.sboldesigner.sbol.CharSequences;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SynBioHubFrontends;

public class UploadExistingDialog extends JDialog implements ActionListener, ListSelectionListener {
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

	private final JLabel info = new JLabel("Select an existing collection(s) to upload the design into.");
	private final JButton uploadButton = new JButton("Upload");
	private final JButton cancelButton = new JButton("Cancel");
	private JList<IdentifiedMetadata> collections = null;

	public UploadExistingDialog(final Component parent, Registry registry, SBOLDocument uploadDoc) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(registry), true);
		this.parent = parent;
		this.registry = registry;
		this.toBeUploaded = toBeUploaded;

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

		// setup collections
		collections = new JList<IdentifiedMetadata>(setupListModel());
		collections.addListSelectionListener(this);
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
		mainPanel.add(new JLabel("Collections:"));
		mainPanel.add(collectionsScroller);

		Container contentPane = getContentPane();
		contentPane.add(info, BorderLayout.PAGE_START);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
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
		SynBioHubFrontends frontends = new SynBioHubFrontends();
		SynBioHubFrontend frontend = null;
		if (frontends.hasFrontend(registry.getLocation())) {
			frontend = frontends.getFrontend(registry.getLocation());
		} else {
			frontend = toBeUploaded.addRegistry(registry.getLocation(), registry.getUriPrefix());
		}

		SearchQuery query = new SearchQuery();
		SearchCriteria crit = new SearchCriteria();
		crit.setKey("objectType");
		crit.setValue("Collection");
		query.addCriteria(crit);
		query.setLimit(10000);
		query.setOffset(0);
		List<IdentifiedMetadata> results;
		DefaultListModel<IdentifiedMetadata> model = new DefaultListModel<IdentifiedMetadata>();
		try {
			results = frontend.search(query);
		} catch (SynBioHubException e) {
			return model;
		}

		if (results.size() == 0) {
			return model;
		}

		for (IdentifiedMetadata collection : results) {
			// don't add collections that have "/public" in the URI.
			if (!collection.getUri().contains("/public/")) {
				model.addElement(collection);
			}
		}
		return model;
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

		IdentifiedMetadata selectedCollection = collections.getSelectedValue();
		
		// TODO how should I call submit?
		// "2" should be changed to option selected, "2" for merge/prevent, "3" for merge/overwrite
		frontend.submit(selectedCollection.getDisplayId().replace("_collection",""), selectedCollection.getVersion(), 
				selectedCollection.getName(), selectedCollection.getDescription(), "", 
				"", "2", toBeUploaded);
		JOptionPane.showMessageDialog(parent, "Upload successful!");
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		uploadButton.setEnabled(!getSelectedCollections(collections).equals(""));
	}
}
