/**
 * 
 */
package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.sbolstandard.core2.SBOLDocument;

import com.clarkparsia.swing.FormBuilder;

import gov.doe.jgi.boost.client.BOOSTClient;
import gov.doe.jgi.boost.enums.FileFormat;
import gov.doe.jgi.boost.enums.Strategy;
import gov.doe.jgi.boost.exception.BOOSTClientException;

/**
 * @author Michael Zhang
 *
 */
public class BOOSTDialog extends JDialog implements ActionListener, DocumentListener {

	private static final String TITLE = "BOOST Optimization";

	private Component parent;
	private File output;
	private SBOLDocument doc;

	private final JButton optimizeButton = new JButton("Optimize Design");
	private final JButton cancelButton = new JButton("Cancel");
	private final JLabel info = new JLabel(
			"Please visit https://boost.jgi.doe.gov/boost.html to create an account. Note this feature requires an internet connection.");
	private final JTextField username = new JTextField();
	private final JTextField password = new JTextField();

	public BOOSTDialog(Component parent, File output, SBOLDocument doc) {
		super(JOptionPane.getFrameForComponent(parent), TITLE, true);
		this.parent = parent;
		this.output = output;
		this.doc = doc;

		username.getDocument().addDocumentListener(this);
		password.getDocument().addDocumentListener(this);
		FormBuilder builder = new FormBuilder();
		builder.add("", info);
		builder.add("", new JLabel(" "));
		builder.add("Username", username);
		builder.add("Password", password);
		JPanel mainPanel = builder.build();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(this);

		optimizeButton.addActionListener(this);
		optimizeButton.setEnabled(false);
		getRootPane().setDefaultButton(optimizeButton);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(optimizeButton);

		Container contentPane = getContentPane();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(this.parent);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			setVisible(false);
			return;
		}

		if (e.getSource() == optimizeButton) {
			try {
				optimizeDesign();
			} catch (BOOSTClientException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void optimizeDesign() throws BOOSTClientException, IOException {
		BOOSTClient client = new BOOSTClient(username.getText(), password.getText());

		// TODO various options/features should be incorporated
		client.reverseTranslate(null, Strategy.MostlyUsed, null, FileFormat.SBOL);
		client.codonJuggle(null, true, Strategy.MostlyUsed, null, FileFormat.SBOL);
		// TODO where is the output?
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		optimizeButton.setEnabled(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		optimizeButton.setEnabled(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		optimizeButton.setEnabled(true);
	}

}
