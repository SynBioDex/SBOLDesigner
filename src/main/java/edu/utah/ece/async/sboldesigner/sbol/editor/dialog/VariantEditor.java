package edu.utah.ece.async.sboldesigner.sbol.editor.dialog;

import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.OperatorType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.StrategyType;
import org.sbolstandard.core2.VariableComponent;

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
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.utah.ece.async.sboldesigner.sbol.CharSequenceUtil;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public class VariantEditor extends JDialog implements ActionListener {
	private static final String TITLE = "Combinatorial Design Variants: ";

	private ComponentDefinition derivationCD;
	private ComponentDefinition variableCD;
	private SBOLDocument design;
	private Component parent;
	private final JComboBox<OperatorType> operatorSelection = new JComboBox<>(OperatorType.values());
	private final JComboBox<Strategy> strategySelection = new JComboBox<>(getStrategies());
	private final JButton addButton = new JButton("Add Variant");
	private final JButton removeButton = new JButton("Remove Variant");
	private final JButton newButton = new JButton("Add new Combinatorial Derivation");
	private final JButton closeButton = new JButton("Save");
	private JTable table;
	private JLabel tableLabel;
	private JScrollPane scroller;

	private class Strategy {
		StrategyType type;

		public Strategy(StrategyType type) {
			this.type = type;
		}

		@Override
		public String toString() {
			if (type == null) {
				return "None";
			}

			return type.toString();
		}
	}

	private Strategy[] getStrategies() {
		StrategyType[] values = StrategyType.values();

		Strategy[] a = new Strategy[values.length + 1];
		a[0] = new Strategy(null);

		for (int i = 0; i < values.length; i++) {
			a[i + 1] = new Strategy(values[i]);
		}

		return a;
	}

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

	public VariantEditor(Component parent, ComponentDefinition derivationCD, ComponentDefinition variableCD,
			SBOLDocument design) {
		super(JOptionPane.getFrameForComponent(parent), TITLE + title(variableCD), true);
		this.derivationCD = derivationCD;
		this.variableCD = variableCD;
		this.parent = parent;
		this.design = design;

		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
		if (derivation != null) {
			this.setTitle("Chosen combinatorial derivation: " + title(derivation));
		}

		try {
			operatorSelection.setSelectedItem(getOperator());
			operatorSelection.addActionListener(this);

			strategySelection
					.setSelectedIndex(getStrategy() == null ? 0 : getStrategy() == StrategyType.ENUMERATE ? 1 : 2);
			strategySelection.addActionListener(this);

			FormBuilder builder = new FormBuilder();
			builder.add("Variant operator", operatorSelection);
			builder.add("Derivation strategy", strategySelection);
			JPanel optionPane = builder.build();

			addButton.addActionListener(this);
			addButton.setEnabled(true);
			removeButton.addActionListener(this);
			removeButton.setEnabled(false);
			newButton.addActionListener(this);
			newButton.setEnabled(true);
			closeButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			closeButton.addActionListener(this);
			getRootPane().setDefaultButton(closeButton);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPane.add(addButton);
			buttonPane.add(removeButton);
			buttonPane.add(newButton);
			buttonPane.add(Box.createHorizontalStrut(100));
			buttonPane.add(Box.createHorizontalGlue());
			buttonPane.add(closeButton);

			JPanel tablePane = initMainPanel();

			Container contentPane = getContentPane();
			contentPane.add(optionPane, BorderLayout.PAGE_START);
			contentPane.add(tablePane, BorderLayout.CENTER);
			contentPane.add(buttonPane, BorderLayout.PAGE_END);
			((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			pack();
			setLocationRelativeTo(parent);
			this.setVisible(true);
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		}
	}

	private StrategyType getStrategy() {
		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
		if (derivation == null || !derivation.isSetStrategy()) {
			return null;
		}

		return derivation.getStrategy();
	}

	private OperatorType getOperator() throws SBOLValidationException {
		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return OperatorType.ONE;
		}

		return variable.getOperator();
	}

	protected JPanel initMainPanel() throws SBOLValidationException {
		ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(getVariants());

		JPanel panel = createTablePanel(tableModel, "Variant count (" + tableModel.getRowCount() + ")");

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

	private void handleTableClick(MouseEvent e) {
		if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			ComponentDefinition variant = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);

			try {
				Desktop.getDesktop().browse(variant.getIdentity());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(parent, "The URI could not be opened: " + e1.getMessage());
			}
		}
	}

	private CombinatorialDerivation chosenDerivation = null;

	private CombinatorialDerivation getCombinatorialDerivation(ComponentDefinition derivationCD) {
		if (chosenDerivation == null) {
			chosenDerivation = CombinatorialDerivationInputDialog.pickCombinatorialDerivation(design, derivationCD);
		}

		return chosenDerivation;
	}

	private VariableComponent getVariableComponent(OperatorType operator) throws Exception {
		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
		if (derivation == null) {
			derivation = createCombinatorialDerivation(derivationCD);
		}

		org.sbolstandard.core2.Component link = getComponentLink(derivationCD, variableCD);
		if (link == null) {
			throw new Exception("derivationCD does not have a component to variableCD");
		}

		VariableComponent variable = getVariableComponent(derivation, link);
		if (variable == null) {
			variable = createVariableComponent(derivation, operator, link);
		}

		return variable;
	}

	private VariableComponent getVariableComponent() throws SBOLValidationException {
		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
		if (derivation == null) {
			return null;
		}

		org.sbolstandard.core2.Component link = getComponentLink(derivationCD, variableCD);
		if (link == null) {
			return null;
		}

		VariableComponent variable = getVariableComponent(derivation, link);
		if (variable == null) {
			return null;
		}

		return variable;
	}

	private VariableComponent getVariableComponent(CombinatorialDerivation derivation,
			org.sbolstandard.core2.Component link) {
		for (VariableComponent variable : derivation.getVariableComponents()) {
			if (variable.getVariable().equals(link)) {
				return variable;
			}
		}

		return null;
	}

	private org.sbolstandard.core2.Component getComponentLink(ComponentDefinition derivationCD,
			ComponentDefinition variableCD) {
		for (org.sbolstandard.core2.Component link : derivationCD.getComponents()) {
			if (link.getDefinitionURI().equals(variableCD.getIdentity())) {
				return link;
			}
		}

		return null;
	}

	private List<ComponentDefinition> getVariants() throws SBOLValidationException {
		ArrayList<ComponentDefinition> variants = new ArrayList<>();

		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return variants;
		}

		for (URI cd : variable.getVariantURIs()) {
			variants.add(design.getComponentDefinition(cd));
		}

		return variants;
	}

	private void addVariant(ComponentDefinition variant) throws Exception {
		VariableComponent variable = getVariableComponent((OperatorType) operatorSelection.getSelectedItem());
		variable.addVariant(variant.getIdentity());
	}

	private VariableComponent createVariableComponent(CombinatorialDerivation derivation, OperatorType operator,
			org.sbolstandard.core2.Component link) throws SBOLValidationException {
		String uniqueId = SBOLUtils.getUniqueDisplayId(null, derivation, link.getDisplayId() + "_VariableComponent",
				null, "VariableComponent", design);
		return derivation.createVariableComponent(uniqueId, operator, link);
	}

	private CombinatorialDerivation createCombinatorialDerivation(ComponentDefinition derivationCD)
			throws SBOLValidationException {
		return createCombinatorialDerivation(derivationCD, derivationCD.getDisplayId() + "_CombinatorialDerivation");
	}

	private CombinatorialDerivation createCombinatorialDerivation(ComponentDefinition derivationCD, String displayId)
			throws SBOLValidationException {
		String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, displayId, derivationCD.getVersion(),
				"CombinatorialDerivation", design);
		CombinatorialDerivation derivation = design.createCombinatorialDerivation(uniqueId, derivationCD.getVersion(),
				derivationCD.getIdentity());

		StrategyType strategy = ((Strategy) strategySelection.getSelectedItem()).type;
		if (strategy != null) {
			derivation.setStrategy(strategy);
		}

		addAsNestedDerivation(derivationCD, derivation);
		addNestedDerivations(variableCD);

		chosenDerivation = derivation;

		return derivation;
	}

	private void addAsNestedDerivation(ComponentDefinition derivationCD, CombinatorialDerivation derivation)
			throws SBOLValidationException {
		for (ComponentDefinition parentCD : design.getComponentDefinitions()) {
			for (org.sbolstandard.core2.Component link : parentCD.getComponents()) {
				if (link.getDefinition().equals(derivationCD)) {
					CombinatorialDerivation parentDerivation = getCombinatorialDerivation(parentCD);

					if (parentDerivation != null) {
						VariableComponent variable = getVariableComponent(parentDerivation, link);

						if (variable == null) {
							variable = createVariableComponent(parentDerivation, OperatorType.ONE, link);
						}

						if (!variable.getVariantDerivations().contains(derivation)) {
							variable.addVariantDerivation(derivation.getIdentity());
						}
					}
				}
			}
		}
	}

	private void addNestedDerivations(ComponentDefinition variableCD) throws SBOLValidationException {
		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return;
		}

		for (CombinatorialDerivation childDerivation : design.getCombinatorialDerivations()) {
			if (childDerivation.getTemplate().equals(variableCD)
					&& !variable.getVariantDerivations().contains(childDerivation)) {
				variable.addVariantDerivation(childDerivation.getIdentity());
			}
		}
	}

	private void removeVariant(ComponentDefinition variant) throws Exception {
		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return;
		}

		variable.removeVariant(variant);

		if (variable.getVariants().isEmpty()) {
			CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
			derivation.removeVariableComponent(variable);

			if (derivation.getVariableComponents().isEmpty()) {
				design.removeCombinatorialDerivation(derivation);
				chosenDerivation = null;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == operatorSelection) {
				setOperator((OperatorType) operatorSelection.getSelectedItem());
				return;
			}

			if (e.getSource() == strategySelection) {
				setStrategy(((Strategy) strategySelection.getSelectedItem()).type);
				return;
			}

			if (e.getSource() == closeButton) {
				setVisible(false);
				return;
			}

			if (e.getSource() == addButton) {
				ComponentDefinitionBox root = new ComponentDefinitionBox();
				SBOLDocument selection = new RegistryInputDialog(parent, root, Parts.forIdentified(variableCD),
						SBOLUtils.Types.DNA, null, design).getInput();

				if (selection == null) {
					return;
				}

				SBOLUtils.insertTopLevels(selection, design);
				addVariant(root.cd);

				updateTable();
				return;
			}

			if (e.getSource() == removeButton) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				ComponentDefinition variant = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
				removeVariant(variant);
				updateTable();
				return;
			}

			if (e.getSource() == newButton) {
				addCombinatorialDerivation();
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void setStrategy(StrategyType strategy) throws SBOLValidationException {
		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);

		if (derivation == null) {
			derivation = createCombinatorialDerivation(derivationCD);
		}

		if (strategy == null) {
			derivation.unsetStrategy();
		} else {
			derivation.setStrategy(strategy);
		}
	}

	private void setOperator(OperatorType operator) throws Exception {
		VariableComponent variable = getVariableComponent(operator);
		variable.setOperator(operator);
	}

	private void addCombinatorialDerivation() throws SBOLValidationException {
		String displayId = JOptionPane.showInputDialog("What would you like the displayId to be?");
		if (displayId == null) {
			return;
		}

		CombinatorialDerivation newDerivation = createCombinatorialDerivation(derivationCD, displayId);

		JOptionPane.showMessageDialog(parent,
				"A new CombinatorialDerivation was created: " + newDerivation.getDisplayId());

		setVisible(false);
	}

	private void updateTable() throws SBOLValidationException {
		ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(getVariants());
		table.setModel(tableModel);
		setWidthAsPercentages(table, tableModel.getWidths());
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);
	}

}
