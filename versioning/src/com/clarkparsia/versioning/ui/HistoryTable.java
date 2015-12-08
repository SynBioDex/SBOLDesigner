/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.versioning.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.clarkparsia.versioning.PersonInfo;
import com.clarkparsia.versioning.Ref;
import com.clarkparsia.versioning.Revision;
import com.clarkparsia.versioning.Tag;

public class HistoryTable extends JTable {
	private static final long serialVersionUID = 1L;
	
	private static final int LANE_WIDTH = 30;
	
	private static final int ROW_HEIGHT = 30;

	private static final int CIRCLE_SIZE = 10;
	
	private enum ColumnSpec {
		ID(10, new IDRenderer()), 
		GRAPH(20, new GraphRenderer()), 
		MESSAGE(45, new MessageRenderer()), 
		AUTHOR(10, new AuthorRenderer()), 
		DATE(25, new CalendarRenderer());
		
		private final String header;
		private final float width;
		private final TableCellRenderer renderer;
		
		private ColumnSpec(float width, TableCellRenderer renderer) {
			String name = name();
			this.header = name.charAt(0) + name.substring(1).toLowerCase();
	        this.width = width;
	        this.renderer = renderer;
        }

		public TableColumn createColumn() {
			TableColumn column = new TableColumn(ordinal());   
			column.setHeaderValue(header);
			column.setPreferredWidth((int) (10000 * width));
			column.setCellRenderer(renderer);
			return column;
		}	

		private static TableColumnModel createModel() {
			final TableColumnModel columns = new DefaultTableColumnModel();

			for (ColumnSpec spec : ColumnSpec.values()) {
				columns.addColumn(spec.createColumn());
	        }
			
			return columns;
		}
	}

	public HistoryTable(HistoryTableModel model) {
		super(model, ColumnSpec.createModel());
		
		setShowHorizontalLines(false);
		setShowVerticalLines(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowHeight(ROW_HEIGHT);
		setRowMargin(0);
	}

	@Override
	protected TableModel createDefaultDataModel() {
		return new HistoryTableModel(new HistoryList());
	}

	public static class HistoryTableModel extends AbstractTableModel {
		private HistoryList history;

		public HistoryTableModel(HistoryList list) {
			history = list;
		}
		
		public void setHistory(HistoryList list) {
			history = list;
			fireTableDataChanged();
		}
		
		@Override
		public int getColumnCount() {
			return ColumnSpec.values().length;
		}
		
		@Override
		public int getRowCount() {
			return history != null ? history.size() : 0;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return history.get(rowIndex);
		}

		@Override
        public Class<?> getColumnClass(int columnIndex) {
			return HistoryNode.class;
        }
	}
	
	private static class IDRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			HistoryNode node = (HistoryNode) value;
			String name = node.getRef().getURI().getLocalName();
			setToolTipText(name);
			return super.getTableCellRendererComponent(table, name.substring(0, 8), isSelected, hasFocus, row, column);			
		}
	};

	private static class GraphRenderer extends DefaultTableCellRenderer {
		private enum StrokeType {
			THIN(1), NORMAL(2), THICK(4);

			private final BasicStroke stroke;

			private StrokeType(float width) {
				stroke = new BasicStroke(width);
			}

			public BasicStroke getStroke() {
				return stroke;
			}
		}

		private transient Graphics2D g;

		private HistoryNode node;
		
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value,
		                final boolean isSelected, final boolean hasFocus, final int row, final int column) {			
			node = (HistoryNode) value;
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

		@Override
        protected void paintComponent(Graphics in) {
			if (in == null)
				return;

			g = (Graphics2D) in.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			try {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				if (node != null) {
					paintNode();
				}
			}
			finally {
				g.dispose();
				g = null;
			}
		}

		private void paintNode() {
			final int h = getHeight();
			final HistoryLane myLane = node.getLane();
			final int myLaneX = laneX(myLane);
			final Color myColor = myLane.getColor();

			int maxCenter = 0;
			for (final HistoryLane passingLane : node.getPassingLanes()) {
				final int cx = laneX(passingLane);
				final Color c = passingLane.getColor();
				drawLine(c, cx, 0, cx, h);
				maxCenter = Math.max(maxCenter, cx);
			}

			for (HistoryNode parent : node.getParents()) {
				final HistoryLane parentLane = parent.getLane();
				final Color parentColor = parentLane.getColor();
				final int parentX = laneX(parentLane);

				if (Math.abs(myLaneX - parentX) > LANE_WIDTH) {
					if (myLaneX < parentX) {
						final int ix = parentX - LANE_WIDTH / 2;
						drawLine(parentColor, myLaneX, h / 2, ix, h / 2);
						drawLine(parentColor, ix, h / 2, parentX, h);
					}
					else {
						final int ix = parentX + LANE_WIDTH / 2;
						drawLine(parentColor, myLaneX, h / 2, ix, h / 2);
						drawLine(parentColor, ix, h / 2, parentX, h);
					}
				}
				else {
					drawLine(parentColor, myLaneX, h / 2, parentX, h);
				}
				maxCenter = Math.max(maxCenter, parentX);
			}

			final int midX = myLaneX - CIRCLE_SIZE / 2 - 1;
			final int midY = (h - CIRCLE_SIZE) / 2;

			if (!node.getChildren().isEmpty()) {
				drawLine(myColor, myLaneX, 0, myLaneX, midY);
			}

			drawCircle(midX, midY);
		}

		private void drawLine(final Color color, int x1, int y1, int x2, int y2) {
			BasicStroke stroke = StrokeType.NORMAL.getStroke();
			int width = (int) stroke.getLineWidth();
			if (y1 == y2) {
				x1 -= width / 2;
				x2 -= width / 2;
			}
			else if (x1 == x2) {
				y1 -= width / 2;
				y2 -= width / 2;
			}

			g.setColor(color);
			g.setStroke(stroke);
			g.drawLine(x1, y1, x2, y2);
		}

		private void drawCircle(final int x, final int y) {
			Color outColor = Color.DARK_GRAY;
			Color inColor = (node.getRef() instanceof Revision) ? Color.LIGHT_GRAY : getBackground();
			g.setStroke(StrokeType.THIN.getStroke());
			g.setColor(inColor);
			g.fillOval(x, y, CIRCLE_SIZE, CIRCLE_SIZE);
			g.setColor(outColor);
			g.drawOval(x, y, CIRCLE_SIZE, CIRCLE_SIZE);
		}

		private int laneX(final HistoryLane lane) {
			return LANE_WIDTH * lane.getColumn() + LANE_WIDTH / 2;
		}	
	}
	
	private static class MessageRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			JPanel box = new JPanel();
			box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
			HistoryNode node = (HistoryNode) value;
			for (Ref tag : node.getTags()) {
				Color color = (tag instanceof Tag) ? Color.YELLOW : node.getLane().getColor();
				box.add(createLabel(tag, color));
				box.add(Box.createHorizontalStrut(3));
            }
			Ref ref = node.getRef();
			box.add(super.getTableCellRendererComponent(table, ref.getActionInfo().getMessage(), isSelected, hasFocus, row, column));
			if (isSelected)
				box.setBackground(table.getSelectionBackground());
			else
				box.setBackground(table.getBackground());
			return box;
		}
		
		private JLabel createLabel(Ref ref, Color background) {
			JLabel label = new JLabel(ref.getName());
			label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory
			                .createEmptyBorder(0, 4, 0, 4)));
			label.setOpaque(true);
			label.setBackground(background);
			return label;
		}
	};
	
	private static class AuthorRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			HistoryNode node = (HistoryNode) value;
			PersonInfo author = node.getRef().getActionInfo().getAuthor();
			setToolTipText(author.toString());
			return super.getTableCellRendererComponent(table, author, isSelected, hasFocus, row, column);
		}
	};
	
	private static class CalendarRenderer extends DefaultTableCellRenderer {
	    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			HistoryNode node = (HistoryNode) value;
			Date date = node.getRef().getActionInfo().getDate().getTime(); 
			return super.getTableCellRendererComponent(table, FORMAT.format(date), isSelected, hasFocus, row, column);
		}
	};
}
