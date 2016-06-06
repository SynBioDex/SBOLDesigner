package com.clarkparsia.sbol.editor.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.TopLevel;

import com.clarkparsia.sbol.CharSequences;
import com.clarkparsia.sbol.editor.Registries;
import com.clarkparsia.sbol.editor.Registry;
import com.clarkparsia.sbol.editor.sparql.SPARQLEndpoint;
import com.clarkparsia.swing.AbstractListTableModel;
import com.clarkparsia.swing.ComboBoxRenderer;
import com.clarkparsia.swing.FormBuilder;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Michael Zhang
 */
public abstract class InputDialog<T> extends JDialog {
	private final ActionListener actionListener = new DialogActionListener();

	private JButton cancelButton, selectButton;

	private FormBuilder builder = new FormBuilder();

	protected boolean canceled = true;

	protected InputDialog(final Component parent, String title) {
		super(JOptionPane.getFrameForComponent(parent), title, true);
	}

	protected String initMessage() {
		return null;
	}

	protected JComponent initMainPanel() {
		return null;
	}

	protected void initFormPanel(FormBuilder builder) {
	}

	protected void initFinished() {
	}

	private void initGUI() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		buttonPanel.add(Box.createHorizontalStrut(200));
		buttonPanel.add(Box.createHorizontalGlue());

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(actionListener);
		cancelButton.registerKeyboardAction(actionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		buttonPanel.add(cancelButton);

		selectButton = new JButton("OK");
		selectButton.addActionListener(actionListener);
		selectButton.setEnabled(false);
		getRootPane().setDefaultButton(selectButton);
		buttonPanel.add(selectButton);

		initFormPanel(builder);

		JComponent formPanel = builder.build();
		formPanel.setAlignmentX(LEFT_ALIGNMENT);

		Box topPanel = Box.createVerticalBox();
		String message = initMessage();
		if (message != null) {
			JPanel messageArea = new JPanel();
			messageArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
					BorderFactory.createEtchedBorder()));
			messageArea.setAlignmentX(LEFT_ALIGNMENT);
			messageArea.add(new JLabel("<html>" + message.replace("\n", "<br>") + "</html>"));
			topPanel.add(messageArea);
		}
		topPanel.add(formPanel);

		JComponent mainPanel = initMainPanel();

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		contentPane.add(topPanel, BorderLayout.NORTH);
		if (mainPanel != null) {
			contentPane.add(mainPanel, BorderLayout.CENTER);
		}
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);

		initFinished();

		pack();
		setLocationRelativeTo(getOwner());
	}

	protected boolean validateInput() {
		return true;
	}

	protected abstract T getSelection();

	/**
	 * Returns an SBOLDocument containing whatever data is selected
	 */
	public T getInput() {
		initGUI();
		try {
			setVisible(true);
			if (canceled) {
				return null;
			}
			return getSelection();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getParent(), Throwables.getRootCause(e).getMessage(), "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	protected void setSelectAllowed(boolean allow) {
		selectButton.setEnabled(allow);
	}

	protected JPanel createTablePanel(AbstractListTableModel<?> tableModel, String title) {
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				setSelectAllowed(table.getSelectedRow() >= 0);
			}
		});

		setWidthAsPercentages(table, tableModel.getWidths());

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
					canceled = false;
					setVisible(false);
				}
			}
		});

		JScrollPane tableScroller = new JScrollPane(table);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JLabel tableLabel = new JLabel(title);
		tableLabel.setLabelFor(table);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		tablePane.add(tableLabel);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);

		tablePane.putClientProperty("table", table);
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

	private class DialogActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == cancelButton) {
				canceled = true;
				setVisible(false);
			} else if (source == selectButton) {
				canceled = false;
				setVisible(false);
			}
		}
	}
}
