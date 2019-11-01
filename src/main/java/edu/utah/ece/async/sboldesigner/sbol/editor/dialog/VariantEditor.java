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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.Collection;
import org.sbolstandard.core2.CombinatorialDerivation;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.OperatorType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.StrategyType;
import org.sbolstandard.core2.TopLevel;
import org.sbolstandard.core2.VariableComponent;

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
	private final JButton removeButton = new JButton("Remove");
	private final JButton newButton = new JButton("Add new Combinatorial Derivation");
	private final JButton saveButton = new JButton("Save");
	private final JTextField displayId = new JTextField();
	private final JTextField name = new JTextField();
	private final JTextField description = new JTextField();
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

		try {
			CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
			if (derivation == null) {
				setVisible(false);
				return;
			}

			operatorSelection.setSelectedItem(getOperator());
			operatorSelection.addActionListener(this);

			strategySelection
					.setSelectedIndex(getStrategy() == null ? 0 : getStrategy() == StrategyType.ENUMERATE ? 1 : 2);
			strategySelection.addActionListener(this);

			displayId.setText(derivation.getDisplayId());
			name.setText(derivation.getName());
			description.setText(derivation.getDescription());
			displayId.setEditable(false);

			FormBuilder builder = new FormBuilder();
			builder.add("Variant operator", operatorSelection);
			builder.add("Derivation strategy", strategySelection);
			builder.add("Derivation display ID", displayId);
			builder.add("Derivation name", name);
			builder.add("Derivation description", description);
			JPanel optionPane = builder.build();

			addButton.addActionListener(this);
			addButton.setEnabled(true);
			removeButton.addActionListener(this);
			removeButton.setEnabled(false);
			newButton.addActionListener(this);
			newButton.setEnabled(true);
			saveButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
			saveButton.addActionListener(this);
			getRootPane().setDefaultButton(saveButton);

			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			buttonPane.add(addButton);
			buttonPane.add(removeButton);
			buttonPane.add(newButton);
			buttonPane.add(Box.createHorizontalStrut(100));
			buttonPane.add(Box.createHorizontalGlue());
			buttonPane.add(saveButton);

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

	private StrategyType getStrategy() throws SBOLValidationException {
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
		//ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(getVariants());
		TopLevelTableModel tableModel = new TopLevelTableModel(getVariantsCollectionsAndDerivations());
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
				MessageDialog.showMessage(parent, "The URI could not be opened: ", e1.getMessage());
			}
		}
	}

	private CombinatorialDerivation chosenDerivation = null;

	private CombinatorialDerivation getCombinatorialDerivation(ComponentDefinition derivationCD)
			throws SBOLValidationException {
		if (chosenDerivation == null) {
			chosenDerivation = CombinatorialDerivationInputDialog.pickCombinatorialDerivation(parent, design, derivationCD);
			//chosenDerivation = new ComboDerivDesign();
			if (chosenDerivation == null) {
				String id = JOptionPane.showInputDialog("What would you like to call this combinatorial derivation?",
						derivationCD.isSetDisplayId() ? derivationCD.getDisplayId() + "_CombinatorialDerivation" : "");
				if (id == null) {
					return null;
				}
				chosenDerivation = createCombinatorialDerivation(derivationCD, id);
			}
		}

		return chosenDerivation;
	}

	private VariableComponent getVariableComponent(OperatorType operator) throws Exception {
		CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
		if (derivation == null) {
			return null;
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
	
	private List<TopLevel> getVariantsCollectionsAndDerivations() throws SBOLValidationException {
		ArrayList<TopLevel> variants = new ArrayList<>();

		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return variants;
		}

		//Get variants
		for (URI cd : variable.getVariantURIs()) {
			variants.add(design.getComponentDefinition(cd));
		}
		
		//Get variantCollections
		for (URI col : variable.getVariantCollectionURIs()) {
			variants.add(design.getCollection(col));
		}
		
		//Get combinatoralDerivations
		for (URI der : variable.getVariantDerivationURIs()) {
			variants.add(design.getCombinatorialDerivation(der));
		}

		return variants;
	}

	private void addVariant(ComponentDefinition variant) throws Exception {
		VariableComponent variable = getVariableComponent((OperatorType) operatorSelection.getSelectedItem());
		variable.addVariant(variant.getIdentity());
	}
	
	private void addCollection(Collection collection) throws Exception {
		VariableComponent variable = getVariableComponent((OperatorType) operatorSelection.getSelectedItem());
		variable.addVariantCollection(collection.getIdentity());
	}
	
	private void addDerivation(CombinatorialDerivation derivation) throws Exception {
		VariableComponent variable = getVariableComponent((OperatorType) operatorSelection.getSelectedItem());
		variable.addVariantDerivation(derivation.getIdentity());
	}

	private VariableComponent createVariableComponent(CombinatorialDerivation derivation, OperatorType operator,
			org.sbolstandard.core2.Component link) throws SBOLValidationException {
		String uniqueId = SBOLUtils.getUniqueDisplayId(null, derivation, link.getDisplayId() + "_VariableComponent",
				null, "VariableComponent", design);
		return derivation.createVariableComponent(uniqueId, operator, link.getIdentity());
	}

	private CombinatorialDerivation createCombinatorialDerivation(ComponentDefinition derivationCD, String displayId)
			throws SBOLValidationException {
		String uniqueId = SBOLUtils.getUniqueDisplayId(null, null, displayId, derivationCD.getVersion(),
				"CombinatorialDerivation", design);
		CombinatorialDerivation derivation = design.createCombinatorialDerivation(uniqueId, "1",
				derivationCD.getIdentity());

		StrategyType strategy = ((Strategy) strategySelection.getSelectedItem()).type;
		if (strategy != null) {
			derivation.setStrategy(strategy);
		}

		addAsNestedDerivation(derivationCD, derivation);
		addNestedDerivations(variableCD);

		this.displayId.setText(displayId);

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

	private void removeVariant(TopLevel top) throws Exception {
		VariableComponent variable = getVariableComponent();
		if (variable == null) {
			return;
		}
		if(top instanceof ComponentDefinition)
		{
			ComponentDefinition variant = (ComponentDefinition) top;
			variable.removeVariant(variant);

			// TODO: pull into a function and call on all three types of removes
			if (variable.getVariants().isEmpty()) { // TODO: should check variantCollections / variantDerivations
				// TODO: this call should not create when it cannot find
				// also need to see why it cannot find it
				CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
				if (derivation == null) {
					return;
				}

				derivation.removeVariableComponent(variable);

//				if (derivation.getVariableComponents().isEmpty()) {
//					design.removeCombinatorialDerivation(derivation);
//					chosenDerivation = null;
//				}
			}
		}else if (top instanceof CombinatorialDerivation)
		{
			variable.removeVariantDerivation((CombinatorialDerivation)top);
			// TODO: remove variableComponent if last variant
		}else if(top instanceof Collection)
		{
			variable.removeVariantCollection((Collection) top);
			// TODO: remove variableComponent if last variant
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

			if (e.getSource() == saveButton) {
				if (chosenDerivation != null) {
					if (!name.getText().equals("")) {
						chosenDerivation.setName(name.getText());
					}
					if (!description.getText().equals("")) {
						chosenDerivation.setDescription(description.getText());
					}
				}

				setVisible(false);
				return;
			}

			if (e.getSource() == addButton) {
				ComponentDefinitionBox root = new ComponentDefinitionBox();
				RegistryInputDialog dialog = new RegistryInputDialog(parent, root, Parts.forIdentified(variableCD),
						SBOLUtils.Types.DNA, null, null);
				dialog.allowCollectionSelection();
				dialog.setObjectType("Variant");
				SBOLDocument selection = dialog.getInput();

				if (selection == null) {
					return;
				}

				SBOLUtils.insertTopLevels(selection, design);
				Collection col;
				if(selection.getCombinatorialDerivations().isEmpty() && selection.getComponentDefinitions().isEmpty() && !selection.getCollections().isEmpty())
				{
					col = selection.getCollections().iterator().next();
					boolean cont = true;
					while(cont)
					{
						cont = false;
						for(Collection c : selection.getCollections())
						{
							if(col != c)
							{
								if(c.containsMember(col.getIdentity()))
								{
									col = c;
									cont = true;
								}
							}
						}
					}
					addCollection(col);
				}else if(!selection.getCombinatorialDerivations().isEmpty()){
					addDerivation(selection.getCombinatorialDerivations().iterator().next());
				}else {
					addVariant(root.cd);
				}

				updateTable();
				return;
			}

			if (e.getSource() == removeButton) {
				int row = table.convertRowIndexToModel(table.getSelectedRow());
				TopLevelTableModel model = (TopLevelTableModel) table.getModel();
				TopLevel top = model.getElement(row);
				removeVariant(top);
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

	private void setStrategy(StrategyType strategy) {
		try {
			CombinatorialDerivation derivation = getCombinatorialDerivation(derivationCD);
			if (derivation == null) {
				return;
			}

			if (strategy == null) {
				derivation.unsetStrategy();
			} else {
				derivation.setStrategy(strategy);
			}
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(parent, "Strategy cannot be enumerate when operators are *orMore");
			e.printStackTrace();
			setVisible(false);
		}
	}

	private void setOperator(OperatorType operator) {
		try {
			VariableComponent variable = getVariableComponent(operator);
			variable.setOperator(operator);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Operator cannot be *orMore when strategy is enumerate");
			e.printStackTrace();
			setVisible(false);
		}
	}

	private void addCombinatorialDerivation() throws SBOLValidationException {
		String displayId = JOptionPane.showInputDialog("What would you like the Display ID to be?");
		if (displayId == null) {
			return;
		}

		CombinatorialDerivation newDerivation = createCombinatorialDerivation(derivationCD, displayId);

		JOptionPane.showMessageDialog(parent,
				"A new CombinatorialDerivation was created: " + newDerivation.getDisplayId());

		setVisible(false);
	}

	private void updateTable() throws SBOLValidationException {
		TopLevelTableModel tableModel = new TopLevelTableModel(getVariantsCollectionsAndDerivations());
		table.setModel(tableModel);
		setWidthAsPercentages(table, tableModel.getWidths());
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);
	}

}
