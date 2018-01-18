package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessageDialog {

	public static void showMessage(Component parentComponent, String title, List<String> messages) {
		StringBuilder sb = new StringBuilder();
		for (String message : messages) {
			sb.append(message);
			sb.append("\n");
		}
		showMessage(parentComponent, title, sb.toString());
	}

	public static void showMessage(Component parentComponent, String title, String message) {
		JTextArea jta = new JTextArea();
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		JScrollPane jsp = new JScrollPane(jta) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(580, 320);
			}
		};
		JOptionPane.showMessageDialog(parentComponent, jsp, title, JOptionPane.ERROR_MESSAGE);
	}
}
