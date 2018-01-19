package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.namespace.QName;

import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;

import edu.utah.ece.async.sboldesigner.sbol.CharSequenceUtil;
import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;

public class AnnotationEditor extends JDialog implements ActionListener {
	private static final String TITLE = "Annotations: ";

	private ComponentDefinition CD;
	private final JButton closeButton = new JButton("Close");
	private final JButton addButton = new JButton("Add");
	private final JButton editButton = new JButton("Edit");
	private final JButton removeButton = new JButton("Remove");
	private JTable table;
	private JLabel tableLabel;
	private JScrollPane scroller;

	private static String title(Identified comp) {
		String title = comp.getDisplayId();
		if (title == null) {
			title = comp.getName();
		}
		if (title == null) {
			URI uri = comp.getIdentity();
			title = (uri == null) ? null : uri.toString();
		}

		return (title == null) ? "" : CharSequenceUtil.shorten(title, 20).toString();
	}

	public AnnotationEditor(final Component parent, ComponentDefinition CD) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(CD), true);
		this.CD = CD;

		addButton.addActionListener(this);
		addButton.setEnabled(true);
		editButton.addActionListener(this);
		editButton.setEnabled(false);
		removeButton.addActionListener(this);
		removeButton.setEnabled(false);
		closeButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		closeButton.addActionListener(this);
		getRootPane().setDefaultButton(closeButton);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(addButton);
		buttonPane.add(editButton);
		buttonPane.add(removeButton);
		buttonPane.add(Box.createHorizontalStrut(100));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(closeButton);

		JPanel tablePane = initMainPanel();

		Container contentPane = getContentPane();
		contentPane.add(tablePane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
		((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		pack();
		setLocationRelativeTo(parent);
		this.setVisible(true);
	}

	protected JPanel initMainPanel() {
		AnnotationTableModel tableModel = new AnnotationTableModel(CD.getAnnotations());

		JPanel panel = createTablePanel(tableModel, "Annotation count (" + tableModel.getRowCount() + ")");

		table = (JTable) panel.getClientProperty("table");
		tableLabel = (JLabel) panel.getClientProperty("label");
		scroller = (JScrollPane) panel.getClientProperty("scroller");

		return panel;
	}

	protected JPanel createTablePanel(AbstractListTableModel<?> tableModel, String title) {
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				editButton.setEnabled(table.getSelectedRow() >= 0);
				removeButton.setEnabled(table.getSelectedRow() >= 0);
			}
		});

		setWidthAsPercentages(table, tableModel.getWidths());

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				handleTableClick(e);
			}
		});

		JScrollPane tableScroller = new JScrollPane(table);
		tableScroller.setPreferredSize(new Dimension(850, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JLabel tableLabel = new JLabel(title);
		tableLabel.setLabelFor(table);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		tablePane.add(tableLabel);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);

		tablePane.putClientProperty("table", table);
		tablePane.putClientProperty("scroller", tableScroller);
		tablePane.putClientProperty("label", tableLabel);

		return tablePane;
	}

	protected static void setWidthAsPercentages(JTable table, double... percentages) {
		final double factor = 10000;

		TableColumnModel model = table.getColumnModel();
		for (int columnIndex = 0; columnIndex < percentages.length; columnIndex++) {
			TableColumn column = model.getColumn(columnIndex);
			column.setPreferredWidth((int) (percentages[columnIndex] * factor));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
			return;
		}

		if (e.getSource() == addButton) {
			new AnnotationEditDialog(null, null, CD).getInput();
			updateTable();
		}

		if (e.getSource() == editButton) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			Annotation a = ((AnnotationTableModel) table.getModel()).getElement(row);
			Annotation result = new AnnotationEditDialog(null, a, CD).getInput();

			if (result != null) {
				// A new annotation was added that isn't a
				CD.removeAnnotation(a);
			}

			updateTable();
		}

		if (e.getSource() == removeButton) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			Annotation a = ((AnnotationTableModel) table.getModel()).getElement(row);
			CD.removeAnnotation(a);
			updateTable();
		}
	}

	private void updateTable() {
		AnnotationTableModel tableModel = new AnnotationTableModel(CD.getAnnotations());
		table.setModel(tableModel);
		setWidthAsPercentages(table, tableModel.getWidths());
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);
	}

	protected void handleTableClick(MouseEvent e) {
		// TODO potentially supported nested annotations and allow focus in
		if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			Annotation a = ((AnnotationTableModel) table.getModel()).getElement(row);

			if (a.isURIValue()) {
				// clicked on an URI
				try {
					Desktop.getDesktop().browse(a.getURIValue());
				} catch (IOException e1) {
					MessageDialog.showMessage(null, "The URI could not be opened: ", e1.getMessage());
				}
			}
		}
	}
}
