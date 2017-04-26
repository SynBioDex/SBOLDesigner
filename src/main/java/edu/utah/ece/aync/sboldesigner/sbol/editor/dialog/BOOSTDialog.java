/**
 * 
 */
package edu.utah.ece.aync.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.sbolstandard.core2.SBOLDocument;

import com.google.protobuf.util.JsonFormat;

import edu.utah.ece.aync.sboldesigner.swing.FormBuilder;

import com.clarkparsia.sbol.editor.dialog.boostformat.Juggle;

/**
 * @author Michael Zhang
 *
 */
public class BOOSTDialog extends JDialog implements ActionListener, DocumentListener {

	private static final String TITLE = "BOOST Optimization";

	private Component parent;
	private File output;
	private SBOLDocument doc;
	private HttpClient client = HttpClients.createDefault();
	private static final String BOOST_REST_URL = "https://boost.jgi.doe.gov/rest";
	private static final String LOGIN_RESOURCE = BOOST_REST_URL + "/auth/login";
	private static final String REVERSE_TRANSLATE_RESOURCE = BOOST_REST_URL + "/juggler/juggle";
	private static final String CODON_JUGGLE_RESOURCE = BOOST_REST_URL + "/juggle/juggle";

	private final JButton optimizeButton = new JButton("Optimize Design");
	private final JButton cancelButton = new JButton("Cancel");
	// TODO add BOOST logo image
	private final JLabel info = new JLabel(
			"Please visit https://boost.jgi.doe.gov/boost.html to create an account. Note this feature requires an internet connection.");
	private final JTextField username = new JTextField();
	private final JPasswordField password = new JPasswordField();

	public BOOSTDialog(Component parent, File output, SBOLDocument doc) {
		super(JOptionPane.getFrameForComponent(parent), TITLE, true);
		this.parent = parent;
		this.output = output;
		this.doc = doc;

		username.getDocument().addDocumentListener(this);
		password.getDocument().addDocumentListener(this);
		password.setEchoChar('*');
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
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void optimizeDesign() throws ClientProtocolException, IOException {
		////////////////////////////////
		// example of Protocol Buffer using src/main/proto/juggle.proto
		Juggle.Request req = Juggle.Request.newBuilder()
				.setModifications(Juggle.Modification.newBuilder().setGeneticCode("STANDARD")
						.setHostName("Arabidopsis thaliana").setStrategy("Balanced"))
				.setOutput(Juggle.FileFormat.newBuilder().setFormat("GENBANK"))
				.setSequences(Juggle.Sequence.newBuilder().setText(">protein_sequence\nMFLIMVSPTAY").addType("PROTEIN"))
				.build();

		String jsonData = JsonFormat.printer().includingDefaultValueFields().print(req);

		Juggle.Request.parseFrom(req.toByteArray());

		System.out.println(req.toString());

		Juggle.Request.Builder builder = Juggle.Request.newBuilder();

		JsonFormat.parser().ignoringUnknownFields().merge(jsonData, builder);

		builder.getModifications();

		//////////////////////////////////////

		String userjwt = login(username.getText(), new String(password.getPassword()));

		// reverse translate
		HttpPost request = new HttpPost(REVERSE_TRANSLATE_RESOURCE);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// TODO unsure how parameters are encoded
		request.setEntity(new UrlEncodedFormEntity(params));
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return;
		}
		InputStream is = entity.getContent();
		String reply = inputStreamToString(is);
		// TODO unsure how reply is encoded
	}

	private String login(String username, String password) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(LOGIN_RESOURCE);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		request.setEntity(new UrlEncodedFormEntity(params));
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");

		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null;
		}
		InputStream is = entity.getContent();
		// TODO userjwt is an "unsupported media type" error
		String userjwt = inputStreamToString(is);
		is.close();
		return userjwt;
	}

	private String inputStreamToString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		return writer.toString();
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
